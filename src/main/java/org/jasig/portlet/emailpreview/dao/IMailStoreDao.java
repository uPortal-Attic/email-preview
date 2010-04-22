package org.jasig.portlet.emailpreview.dao;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;

/**
 * IMailStoreDao defines an interface for persisting and reading e-mail store
 * configuration information.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IMailStoreDao {

    /**
     * 
     * 
     * @param request
     * @return
     */
    public MailStoreConfiguration getConfiguration(PortletRequest request);

    /**
     * 
     * @param request
     * @param configuration
     */
    public void saveConfiguration(ActionRequest request,
            MailStoreConfiguration configuration);

}