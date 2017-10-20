package com.example.subspace;

import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class MailMessage extends MimeMessage
{
    private boolean mPreserveMessageID;

    public MailMessage(
        Session session)
    {
        super(session);

        mPreserveMessageID = false;
    }

    public MailMessage(
        Session session,
        InputStream is)
        throws MessagingException
    {
        super(session, is);

        mPreserveMessageID = false;
    }

    public MailMessage(
        MailMessage source)
        throws MessagingException
    {
        super(source);

        mPreserveMessageID = source.mPreserveMessageID;
    }

    /**
     * Generate a new Message-ID, and preserve it until the next call
     * to this method.
     */
    public void generateMessageID()
        throws MessagingException
    {
        super.updateMessageID();
        mPreserveMessageID = true;
    }

    @Override
    protected void updateMessageID()
        throws MessagingException
    {
        if (!mPreserveMessageID)
        {
            super.updateMessageID();
        }
    }
}
