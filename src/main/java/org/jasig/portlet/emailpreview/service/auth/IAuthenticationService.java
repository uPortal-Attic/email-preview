package org.jasig.portlet.emailpreview.service.auth;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;

public interface IAuthenticationService {

    /**
     * Return the unique key for this authentication service.  This key will be used
     * to retrieve an authentication service instance from the registry.
     * 
     * @return
     */
    public String getKey();

    /**
     * 
     * @param request
     * @param config
     * @return
     */
    public Authenticator getAuthenticator(PortletRequest request, MailStoreConfiguration config);

}
