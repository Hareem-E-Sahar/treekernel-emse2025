package org.sbugs.logic.state;

import java.util.*;
import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.sbugs.model.defect.*;
import org.sbugs.model.user.*;
import org.sbugs.dao.user.*;
import org.sbugs.exceptions.*;

public class EmailHandler extends TransitionHandler {

    private static Session _mailSession;

    protected Session getSession() {
        if (_mailSession == null) {
            synchronized (this) {
                if (_mailSession == null) {
                    ResourceBundle bundle = ResourceBundle.getBundle("org.sbugs.database");
                    Properties props = new ResourceBundleProperties(bundle);
                    _mailSession = Session.getDefaultInstance(props, null);
                }
            }
        }
        return _mailSession;
    }

    public void handleTransition(Defect defect, Map params, Connection connection) throws StateChangeException {
        try {
            MimeMessage message = new MimeMessage(getSession());
            message.setSubject("[sbugs] Defect updated: #" + defect.getId());
            StringBuffer buffer = new StringBuffer(200);
            buffer.append("Defect #");
            buffer.append(defect.getId());
            buffer.append(" has been updated. \nHeadline: ");
            buffer.append(defect.getHeadline());
            buffer.append(" New state: ");
            buffer.append(defect.getState());
            message.setText(buffer.toString());
            Address senderAddress = createSenderAddress(params, connection);
            message.setFrom(senderAddress);
            Address[] recipients = createRecipientAddresses(defect, params, connection);
            if (recipients.length == 0) {
                return;
            }
            for (int i = 0; i < recipients.length; i++) {
                message.addRecipient(Message.RecipientType.TO, recipients[i]);
            }
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new StateChangeException(e.getMessage());
        }
    }

    /**
       params and connection provided to allow subclasses to implement additional
       logic in creating a sender's address.  They are not used in the default
       implementation.
     **/
    protected Address createSenderAddress(Map params, Connection connection) throws AddressException, java.io.UnsupportedEncodingException {
        return new InternetAddress("no-reply@sbugs.org", "Automated sBugs email");
    }

    /**
       Like createSenderAddress, params and connection are not used in the default
       implementation of this method.
     **/
    protected Address[] createRecipientAddresses(Defect defect, Map params, Connection connection) throws SQLException {
        StringTokenizer tokenizer = new StringTokenizer(defect.getEmailAddresses(), ";");
        ArrayList addressList = new ArrayList(3);
        while (tokenizer.hasMoreTokens()) {
            try {
                addressList.add(new InternetAddress(tokenizer.nextToken()));
            } catch (AddressException ae) {
                ae.printStackTrace();
            }
        }
        if (defect.getAssignedToUserId() != null) {
            User assignedTo = UserDAO.getInstance().loadUser(defect.getAssignedToUserId().intValue(), connection);
            if (assignedTo.getEmailAddr() != null) {
                try {
                    addressList.add(new InternetAddress(assignedTo.getEmailAddr()));
                } catch (AddressException ae) {
                    ae.printStackTrace();
                }
            }
        }
        return (Address[]) addressList.toArray(new Address[addressList.size()]);
    }
}

/**
A bit hackish, but works to force stuff in the format the mail api expects
Not a full implementation, but it suffices for now.  We can flesh it out later
if we ever need to.
*/
class ResourceBundleProperties extends Properties {

    private ResourceBundle _bundle;

    public ResourceBundleProperties(ResourceBundle bundle) {
        _bundle = bundle;
    }

    public String getProperty(String key) {
        try {
            return _bundle.getString(key);
        } catch (MissingResourceException mre) {
            return null;
        }
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            value = super.getProperty(key);
            if (value == null) {
                value = defaultValue;
            }
        }
        return value;
    }
}
