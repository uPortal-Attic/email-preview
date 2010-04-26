package org.jasig.portlet.emailpreview.service.link;

import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;

/**
 * IEmailLinkService provides links to an external webmail client.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IEmailLinkService {
   
    /**
     * Return the unique key for this link service.  This key will be used
     * to retrieve a link service instances from the registry.
     * 
     * @return
     */
    public String getKey();

    /**
     * Get the URL of the inbox for this portlet request and mail
     * store configuration.  This method may simply provide the URL of an 
     * external webmail client.  Some implementations may wish to support
     * SSO or implement other interesting client-specific URLs.
     * 
     * @param request
     * @param config
     * @return
     */
    public String getInboxUrl(PortletRequest request, MailStoreConfiguration config);

}
