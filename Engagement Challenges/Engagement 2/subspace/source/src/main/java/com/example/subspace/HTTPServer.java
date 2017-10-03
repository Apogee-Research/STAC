package com.example.subspace;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import fi.iki.elonen.NanoHTTPD;

import com.example.util.GeoPoint;

public class HTTPServer extends NanoHTTPD
{
    private static final Logger LOGGER =
        Logger.getLogger(HTTPServer.class.getName());

    protected Database db;

    /**
     * Outbound email session.
     */
    protected Session mailSession;

    protected final InternetAddress noReplyFrom;

    protected final String messageSubjectPrepend;

    protected final String messageConfirmSubject;

    protected final String messageConfirmText;

    protected final String messageDoneSubject;

    protected final String messageDoneText;

    public HTTPServer(
        Config config,
        Database db)
        throws RuntimeException
    {
        super(config.getMandatoryProperty("subspace.httpdHost"),
              config.getMandatoryPortProperty("subspace.httpdPort"));

        this.db = db;

        mailSession = Session.getDefaultInstance(config);

        try
        {
            noReplyFrom =
                new InternetAddress(
                    config.getMandatoryProperty(
                        "subspace.message.no-reply-from"));
        }
        catch (AddressException e)
        {
            throw new RuntimeException(e);
        }

        messageSubjectPrepend = config.getMandatoryProperty(
            "subspace.message.subjectPrepend");

        messageConfirmSubject = config.getMandatoryProperty(
            "subspace.message.register.confirm.subject");

        messageConfirmText = config.getMandatoryProperty(
            "subspace.message.register.confirm.text");

        messageDoneSubject = config.getMandatoryProperty(
            "subspace.message.register.done.subject");

        messageDoneText = config.getMandatoryProperty(
            "subspace.message.register.done.text");
    }

    @Override
    public Response serve(
        IHTTPSession session)
    {
        switch (session.getMethod())
        {
            case GET:
                if ("/register".equals(session.getUri()))
                {
                    return doRegister(session);
                }
                else if ("/confirm".equals(session.getUri()))
                {
                    return doConfirm(session);
                }
                else if ("/update-location".equals(session.getUri()))
                {
                    return doUpdateLocation(session);
                }
                else
                {
                    return getSimpleResponse(
                        Response.Status.NOT_FOUND);
                }

            default:
                return getSimpleResponse(
                    Response.Status.METHOD_NOT_ALLOWED);
        }
    }

    /**
     * Register a new user.
     */
    protected Response doRegister(
        IHTTPSession session)
    {
        String username = session.getParms().get("username");
        String password = session.getParms().get("password");
        String email = session.getParms().get("email");

        if (false
            || username == null
            || password == null
            || email == null
            )
        {
            return new Response(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                "Please specify a username, password, and email.");
        }

        InternetAddress emailAddress = null;
        try
        {
            emailAddress = new InternetAddress(email);
        }
        catch (AddressException e)
        {
            return new Response(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                "Invalid email address.");
        }

        String token = db.registerUser(username, password, email);
        if (token == null)
        {
            return new Response(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                "Registration failed.");
        }

        // Send a registration confirmation email.
        try
        {
            MailMessage message = new MailMessage(mailSession);

            message.setFrom(noReplyFrom);

            message.setRecipients(
                Message.RecipientType.TO,
                new InternetAddress[] {emailAddress});

            message.setSubject(
                messageSubjectPrepend + messageConfirmSubject);

            message.setText(String.format(
                messageConfirmText,
                token,
                username,
                email));

            Transport.send(message);
        }
        catch (MessagingException e)
        {
            LOGGER.log(
                Level.INFO,
                "Unable to send message",
                e);

            return new Response(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                "Internal error.");
        }

        return new Response(
            Response.Status.OK,
            MIME_PLAINTEXT,
            "Registration succeeded. Check your email.");
    }

    /**
     * Confirm a new user registration.
     */
    protected Response doConfirm(
        IHTTPSession session)
    {
        String token = session.getParms().get("token");

        if (false
            || token == null)
        {
            return new Response(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                "Please verify that you copied the entire URL.");
        }

        User user = db.confirmUser(token);
        if (user == null)
        {
            return new Response(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                "Please verify that you copied the entire URL. " +
                    "Or, if you've already confirmed your " +
                    "registration, check your email again.");
        }

        // Send a welcome email.
        try
        {
            MailMessage message = new MailMessage(mailSession);

            message.setFrom(noReplyFrom);

            message.setRecipients(
                Message.RecipientType.TO,
                user.getEmailAddress());

            message.setSubject(
                messageSubjectPrepend + messageDoneSubject);

            message.setText(String.format(
                messageDoneText,
                user.getUsername(),
                user.getEmailAddress()));

            Transport.send(message);
        }
        catch (MessagingException e)
        {
            LOGGER.log(
                Level.INFO,
                "Unable to send message",
                e);
        }

        return new Response(
            Response.Status.OK,
            MIME_PLAINTEXT,
            "Confirmation successful. Check your email.");
    }

    /**
     * Update a user's location.
     */
    protected Response doUpdateLocation(
        IHTTPSession session)
    {
        String username = null;
        String password = null;
        GeoPoint location = null;

        Response badParams = new Response(
            Response.Status.BAD_REQUEST,
            MIME_PLAINTEXT,
            "Bad parameters.");
        try
        {
            username = session.getParms().get("username");
            password = session.getParms().get("password");
        }
        catch (Exception e)
        {
            return badParams;
        }

        if (false
            || session.getParms().containsKey("lat")
            || session.getParms().containsKey("lon")
            )
        {
            try
            {
                location = new GeoPoint(
                    Double.parseDouble(session.getParms().get("lat")),
                    Double.parseDouble(session.getParms().get("lon")));
            }
            catch (Exception e)
            {
                return badParams;
            }
        }
        else if (session.getParms().containsKey("away"))
        {
            location = null;
        }
        else
        {
            return badParams;
        }

        if (false
            || username == null
            || password == null
            )
        {
            return badParams;
        }

        User user = db.getUser(username);
        if (user == null)
        {
            // This lets us run authenticate() below, so we don't leak
            // whether or not the username exists.
            user = InvalidUser.USER;
        }

        if (!user.authenticate(password))
        {
            return badParams;
        }

        db.updateUserLocation(user, location, false);

        return new Response(
            Response.Status.OK,
            MIME_PLAINTEXT,
            "Location will be updated shortly");
    }

    /**
     * Get a response with a plain text description of the status.
     */
    protected Response getSimpleResponse(
        Response.Status status)
    {
        return new Response(
            status,
            MIME_PLAINTEXT,
            status.getDescription());
    }
}
