package org.jasig.portlet.emailpreview;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class SimplePasswordAuthenticator extends Authenticator {
    
    private final String username;
    private final String password;

    /**
     * Construct a new SimplePasswordAuthenticator instance.
     * 
     * @param username
     * @param password
     */
    public SimplePasswordAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * @see javax.mail.Authenticator#getPasswordAuthentication()
     */
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimplePasswordAuthenticator)) {
            return false;
        }
        
        SimplePasswordAuthenticator auth2 = (SimplePasswordAuthenticator) obj;
        
        return new EqualsBuilder()
            .append(this.username, auth2.username)
            .append(this.password, auth2.password)
            .isEquals();
        
    }
    
}
