public class Test {    public void internalSendNotificationOfInterest(TaskLocal p_taskLocal, Set p_contactablesLocal) {
        URLName url = ServerConfiguration.getInstance().getMailURLName();
        if (url != null) {
            try {
                InternetAddress[] toAddresses = getToAddresses(p_contactablesLocal);
                if (toAddresses.length > 0) {
                    Properties props = System.getProperties();
                    props.put("mail.smtp.host", url.getHost());
                    props.put("mail.smtp.user", url.getUsername());
                    Session session = Session.getDefaultInstance(props, null);
                    session.setPasswordAuthentication(url, new PasswordAuthentication(url.getUsername(), url.getPassword()));
                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom(ServerConfiguration.getInstance().getMailFromAddress());
                    msg.setRecipients(Message.RecipientType.TO, toAddresses);
                    msg.setSubject("Velocityme update, task ID = " + p_taskLocal.getTaskId().toString() + " has changed.");
                    msg.setSentDate(new Date());
                    MimeBodyPart mbp1 = new MimeBodyPart();
                    StringBuffer messageBuffer = new StringBuffer();
                    createInterestMessage(messageBuffer, p_taskLocal);
                    mbp1.setContent(messageBuffer.toString(), "text/html");
                    Multipart mp = new MimeMultipart();
                    mp.addBodyPart(mbp1);
                    msg.setContent(mp);
                    Transport.send(msg);
                }
            } catch (MessagingException e) {
                m_logger.warn("Sending email notification of interest failed", e);
            } catch (NamingException e) {
                throw new EJBException(e);
            } catch (FinderException e) {
                throw new EJBException(e);
            }
        }
    }
}