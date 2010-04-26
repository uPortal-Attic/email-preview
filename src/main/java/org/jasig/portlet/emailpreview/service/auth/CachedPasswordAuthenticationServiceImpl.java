package org.jasig.portlet.emailpreview.service.auth;

import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.SimplePasswordAuthenticator;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component("cachedPasswordAuthenticationService")
public class CachedPasswordAuthenticationServiceImpl implements
        IAuthenticationService {
    
    private static final String KEY = "cachedPassword";
    
    private String usernameKey = "user.login.id";
    private String passwordKey = "password";

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.IAuthenticationService#getAuthenticator(javax.portlet.PortletRequest, org.jasig.portlet.emailpreview.MailStoreConfiguration)
     */
    public Authenticator getAuthenticator(PortletRequest request,
            MailStoreConfiguration config) {
        
        @SuppressWarnings("unchecked")
        Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        String username = userInfo.get(usernameKey);
        String password = userInfo.get(passwordKey);
        
        return new SimplePasswordAuthenticator(username, password);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.IAuthenticationService#getKey()
     */
    public String getKey() {
        return KEY;
    }

}
