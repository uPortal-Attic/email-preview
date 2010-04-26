package org.jasig.portlet.emailpreview.service.link;

import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component("simpleEmailLinkService")
public class SimpleEmailLinkServiceImpl implements IEmailLinkService {

    private static final String KEY = "default";
    public static final String INBOX_URL_PROPERTY = "inboxUrl";
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.IEmailLinkService#getKey()
     */
    public String getKey() {
        return KEY;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.IEmailLinkService#getInboxUrl(javax.portlet.PortletRequest, org.jasig.portlet.emailpreview.MailStoreConfiguration)
     */
    public String getInboxUrl(PortletRequest request, MailStoreConfiguration configuration) {
        return configuration.getAdditionalProperties().get(INBOX_URL_PROPERTY);
    }

}
