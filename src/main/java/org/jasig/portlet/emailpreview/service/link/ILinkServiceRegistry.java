package org.jasig.portlet.emailpreview.service.link;

import java.util.Collection;

/**
 * ILinkServiceRegistry provides a registry of email link service implementation
 * instances.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface ILinkServiceRegistry {
    
    /**
     * Register a link service.
     * 
     * @param linkService
     */
    public void registerService(IEmailLinkService linkService);
    
    /**
     * Return an instance of the link service associated with the supplied key.
     * If no matching link service can be found, this method will return
     * <code>null</code>.
     * 
     * @param key
     * @return
     */
    public IEmailLinkService getEmailLinkService(String key);
    
    /**
     * Return a list of all currently-registered link services.
     * 
     * @return
     */
    public Collection<IEmailLinkService> getServices();

}
