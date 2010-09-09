/**
 * 
 */
package org.jasig.portlet.emailpreview.dao;

public enum MailPreferences {
    
    /*
     * Settings used by the mail DAO 
     */
    
    PROTOCOL("protocol"),
    HOST("host"),
    PORT("port"),
    INBOX_NAME("inboxName"),
    CONNECTION_TIMEOUT("connectionTimeout"),
    TIMEOUT("timeout"),
    LINK_SERVICE_KEY("linkServiceKey"),
    AUTHENTICATION_SERVICE_KEY("authenticationServiceKey"),
    ALLOWABLE_AUTHENTICATION_SERVICE_KEYS("allowableAuthenticationServiceKeys"),
    USERNAME_SUFFIX("usernameSuffix"),

    /*
     * Optional settings used by some auth services 
     */
    
    MAIL_ACCOUNT("username"),
    PASSWORD("password");

    /*
     * Implementation 
     */

    private final String key;
    MailPreferences(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
    
}