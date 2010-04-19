package org.jasig.portlet.emailpreview;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SimplePasswordAuthenticator extends Authenticator {

    private PasswordAuthentication authentication;
    
    public SimplePasswordAuthenticator(String username, String password) {
        this.authentication = new PasswordAuthentication(username, password);
    }
    
    public SimplePasswordAuthenticator(PasswordAuthentication authentication) {
        this.authentication = authentication;
    }
    
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return this.authentication;
    }

}
