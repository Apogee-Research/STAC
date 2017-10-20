package com.example.subspace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.SMTPServer;

public class SMTPHandler
    implements MessageHandlerFactory
{
    private static final Logger LOGGER =
        Logger.getLogger(SMTPHandler.class.getName());

    /**
     * Headers to retain when anonymizing a message.
     */
    private static final Set<String> HEADERS_TO_SAVE =
        new HashSet<String>();
    static
    {
        for (String header : new String[] {
                "Content-Language",
                "Content-Transfer-Encoding",
                "Content-Type",
                "Date",
                "In-Reply-To",
                "References",
                "Subject",
                })
        {
            HEADERS_TO_SAVE.add(header);
        }
    }

    protected class MyMessageHandler
        implements MessageHandler
    {
        /**
         * Too avoid leaking information, we can store a
         * RejectException to be thrown during the DATA stage, instead
         * of throwing it in an earlier stage.
         */
        protected RejectException reject;

        /**
         * The user who's sending us an email.
         */
        protected User user;

        /**
         * Map from alias to set of email addresses belong to each
         * alias.
         */
        protected Map<String, Set<String>> recipients;

        /**
         * For each alias, whether it's new or not.
         */
        protected Map<String, Boolean> isNewAlias;

        /**
         * Whether we've already added a new alias.
         */
        protected boolean addedNewAlias;

        /**
         * Message to send (anonymized).
         */
        protected MailMessage message;

        MyMessageHandler()
        {
            reject = null;

            user = null;

            recipients = new HashMap<String, Set<String>>();

            isNewAlias = new HashMap<String, Boolean>();

            addedNewAlias = false;

            message = null;
        }

        @Override
        public void from(
            String from)
            throws RejectException
        {
            // For avoiding timing side channels.
            RejectException discard;

            user = db.getUserByEmail(from);
            if (user == null)
            {
                user = InvalidUser.USER;
                reject = new RejectException();

                LOGGER.log(
                    Level.FINE,
                    "{0} rejecting mail from unknown email {1}",
                    new Object[] {
                        this,
                        from,
                        });
            }
            if (user != null)
            {
                discard = new RejectException();

                LOGGER.log(
                    Level.FINE,
                    "{0} accepting mail from {1}",
                    new Object[] {
                        this,
                        from,
                        });
            }
        }

        @Override
        public void recipient(
            String recipient)
            throws RejectException
        {
            String localPart = getLocalPart(recipient);
            if (localPart == null)
            {
                // The set of local names is not particularly
                // sensitive, so don't bother delaying the rejection.
                throw new RejectException();
            }

            String aliasName = null;
            Set<String> aliasEmails = null;
            boolean isNew = false;

            // This if-elseif-else block does not need to be resistant
            // to timing side channel attacks, because it's based
            // solely on relatively public information (newAliasNames
            // and aliasPrefix).
            if (newAliasNames.contains(localPart))
            {
                if (addedNewAlias)
                {
                    // We already added a new alias. This only occurs
                    // if the user specified multiple new-alias
                    // recipients. Since the already user knows the
                    // recipients they specified, no info is leaked by
                    // returning early.
                    return;
                }

                isNew = true;

                User neighbor = db.findNearestNeighbor(user);
                if (neighbor == null)
                {
                    // This isn't leaking anything interesting, so
                    // there's no need to delay this rejection.
                    throw new RejectException(
                        "You are alone in the universe.");
                }

                User[] aliasUsers = new User[] {
                    user,
                    neighbor,
                    };

                aliasName = db.registerAlias(
                    new UUIDAliasGenerator(),
                    aliasUsers);

                aliasEmails = new HashSet<String>();
                for (User u : aliasUsers)
                {
                    aliasEmails.add(u.getEmailAddress());
                }
            }
            else if (localPart.startsWith(aliasPrefix))
            {
                isNew = false;

                aliasName = localPart.substring(aliasPrefix.length());
                if (recipients.containsKey(aliasName))
                {
                    // We already added this alias. This only occurs
                    // if the user specified multiple of the same
                    // alias as recipients. Since the already user
                    // knows the recipients they specified, no info is
                    // leaked by returning early.
                    return;
                }

                // Check that the alias exists and that the sender is
                // a member of the alias. The amount of time taken by
                // these checks may be dependent on the results of the
                // checks.
                aliasEmails = db.getAliasMemberEmails(aliasName);
                if (aliasEmails == null)
                {
                    reject = new RejectException();
                }
                else if (
                    !aliasEmails.contains(user.getEmailAddress()))
                {
                    // User not authorized to send to this alias.
                    reject = new RejectException();
                }

                // Partially compensate for the above timing side
                // channel. Aliases are hard enough to guess (random
                // UUIDs) and low enough value that more a
                // comprehensive prevention of this side channel is
                // not justified.
                Thread.yield();
            }
            else
            {
                // Unknown recipient.
                reject = new RejectException();
            }

            if (reject != null)
            {
                // The message will be rejected before these variables
                // are used, so it's ok to put bad data in them in
                // order to avoid a timing side channel.
                recipients.put("invalid", new HashSet<String>());
                isNewAlias.put("invalid", true);

                LOGGER.log(
                    Level.FINE,
                    "{0} rejecting rcpt to {1}",
                    new Object[] {
                        this,
                        recipient,
                        });
            }
            if (reject == null)
            {
                // Match the HashSet<String> construction above.
                Set<String> discard = new HashSet<String>();

                recipients.put(aliasName, aliasEmails);
                isNewAlias.put(aliasName, isNew);

                LOGGER.log(
                    Level.FINE,
                    "{0} accepting rcpt to {1}",
                    new Object[] {
                        this,
                        recipient,
                        });
            }
        }

        @Override
        public void data(
            InputStream data)
            throws RejectException, TooMuchDataException, IOException
        {
            // At this point, we've processed all the sensitive user
            // information. There's still the message data to process,
            // but the other end of this connection already knows that
            // data, so leaking that to them is not a big deal.
            // Therefore, we can safely throw a stored
            // rejection now.
            if (reject != null)
            {
                throw reject;
            }

            Session session =
                Session.getDefaultInstance(config);

            try
            {
                message = new MailMessage(session, data);

                // Remove disallowed headers.
                Set<String> headersToRemove = new HashSet<String>();
                for (
                    Enumeration<Header> e = message.getAllHeaders();
                    e.hasMoreElements();
                    )
                {
                    String header = e.nextElement().getName();
                    if (!HEADERS_TO_SAVE.contains(header))
                    {
                        headersToRemove.add(header);
                    }
                }
                for (String header : headersToRemove)
                {
                    message.removeHeader(header);
                }

                // Edit the subject.
                if (subjectPrepend != null)
                {
                    String subject = message.getSubject();

                    boolean modifiedSubject = true;
                    if (subject == null)
                    {
                        subject = subjectPrepend + "(no subject)";
                    }
                    else if (!subject.contains(subjectPrepend))
                    {
                        subject = subjectPrepend + subject;
                    }
                    else
                    {
                        modifiedSubject = false;
                    }

                    if (modifiedSubject)
                    {
                        message.setSubject(subject);
                    }
                }
            }
            catch (MessagingException e)
            {
                message = null;

                throw new IOException(e);
            }
        }

        @Override
        public void done()
        {
            if (message == null)
            {
                return;
            }

            try
            {
                message.generateMessageID();
            }
            catch (MessagingException e)
            {
                LOGGER.log(
                    Level.WARNING,
                    "Unable to generate Message-ID, dropping message",
                    e);
                return;
            }

            for (
                Map.Entry<String, Set<String>> entry
                : recipients.entrySet())
            {
                final String aliasName = entry.getKey();
                final boolean isNew =
                    isNewAlias.get(aliasName).booleanValue();

                final String from = String.format(
                    "%s%s@%s",
                    aliasPrefix,
                    aliasName,
                    localCanonicalName);

                for (String recipient : entry.getValue())
                {
                    try
                    {
                        MailMessage messageOut = new MailMessage(message);

                        messageOut.addHeader("From", from);
                        messageOut.addHeader("To", recipient);

                        String prepend;
                        if (isNew)
                        {
                            if (
                                recipient.equals(
                                    user.getEmailAddress()))
                            {
                                prepend = config.getProperty(
                                    "subspace.message.new.initiator");
                            }
                            else
                            {
                                prepend = config.getProperty(
                                    "subspace.message.new.other");
                            }
                        }
                        else
                        {
                            if (
                                recipient.equals(
                                    user.getEmailAddress()))
                            {
                                prepend = config.getProperty(
                                    "subspace.message.existing.sender");
                            }
                            else
                            {
                                prepend = config.getProperty(
                                    "subspace.message.existing.other");
                            }
                        }

                        if (prepend != null)
                        {
                            MimeBodyPart prependPart =
                                new MimeBodyPart();
                            prependPart.setText(prepend);
                            prependPart.setDisposition(Part.INLINE);

                            MimeBodyPart contentPart =
                                new MimeBodyPart();
                            contentPart.setContent(
                                messageOut.getContent(),
                                messageOut.getContentType());
                            contentPart.setDisposition(Part.INLINE);

                            Multipart content = new MimeMultipart();
                            content.addBodyPart(prependPart);
                            content.addBodyPart(contentPart);

                            messageOut.setContent(content);
                        }

                        Transport.send(messageOut);
                    }
                    catch (IOException|MessagingException e)
                    {
                        LOGGER.log(
                            Level.INFO,
                            "Unable to send message",
                            e);
                    }
                }
            }
        }
    }

    protected Config config;

    protected Database db;

    /**
     * Set of hostnames for which we should accept mail.
     */
    protected final Set<String> localNames;

    /**
     * Canonical hostname from which we should send mail.
     */
    protected final String localCanonicalName;

    /**
     * Set of local parts (from an email address) that initiate a new
     * conversation.
     */
    protected final Set<String> newAliasNames;

    /**
     * Prefix that all aliases start with.
     */
    protected final String aliasPrefix;

    /**
     * String to prepend to message subjects.
     */
    protected final String subjectPrepend;

    public SMTPHandler(
        Config config,
        Database db)
        throws RuntimeException
    {
        this.config = config;
        this.db = db;

        localNames = config.getSetProperty("subspace.mailLocalName");
        if (localNames.isEmpty())
        {
            throw new RuntimeException(
                "Please configure subspace.mailLocalName");
        }

        String tmpLocalCanonicalName =
            config.getProperty("subspace.mailCanonicalName");
        if (tmpLocalCanonicalName != null)
        {
            localCanonicalName = tmpLocalCanonicalName;
        }
        else
        {
            if (localNames.size() == 1)
            {
                localCanonicalName = localNames.iterator().next();
            }
            else
            {
                throw new RuntimeException(
                    "Please configure subspace.mailCanonicalName");
            }
        }

        newAliasNames =
            config.getSetProperty("subspace.newAliasName");
        if (newAliasNames.isEmpty())
        {
            throw new RuntimeException(
                "Please configure subspace.newAliasName");
        }

        aliasPrefix =
            config.getProperty("subspace.aliasPrefix", "comms-");

        subjectPrepend =
            config.getProperty("subspace.message.subjectPrepend");
    }

    /**
     * Configure the SMTPServer before it starts.
     */
    public void configure(
        SMTPServer server)
        throws RuntimeException
    {
        server.setPort(
            config.getMandatoryPortProperty("subspace.smtpdPort"));
    }

    @Override
    public MessageHandler create(
        MessageContext context)
    {
        return new MyMessageHandler();
    }

    /**
     * Return the local part of the email address, or null if the
     * address isn't local.
     */
    protected String getLocalPart(
        String emailAddress)
    {
        int atIndex = emailAddress.indexOf('@');
        if (atIndex < 0)
        {
            return null;
        }

        String name = emailAddress.substring(0, atIndex);
        String host = emailAddress.substring(atIndex + 1);

        if (!localNames.contains(host))
        {
            return null;
        }

        return name;
    }
}
