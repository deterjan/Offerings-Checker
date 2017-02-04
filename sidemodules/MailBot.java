package sidemodules;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Mail bot that disables itself for given milliseconds after shooting an email
 */

public class MailBot
{
    // Modify below strings for e-mail functionality
    final String username = "INSERT-SENDER-EMAIL-ADDRESS";
    final String password = "INSERT-SENDER-EMAIL-PASSWORD";

    Properties props;
    Session session;
    String receiver;
    boolean enabled;
    int waitTime;

    public MailBot( int waitTime, String receiver)
    {
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        enabled = true;
        this.waitTime = waitTime;
        this.receiver = receiver;
    }

    public void sendMail( String subject, String body)
    {
        if ( enabled)
        {
            try
            {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress( username));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse( receiver));
                message.setSubject( subject);
                message.setText( body);

                Transport.send( message);

                enabled = false;
                waitAndEnable();
            } catch (MessagingException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private void waitAndEnable()
    {
        new java.util.Timer().schedule(
                new java.util.TimerTask()
                {
                    @Override
                    public void run()
                    {
                        enabled = true;
                    }
                },
                waitTime
        );
    }
}
