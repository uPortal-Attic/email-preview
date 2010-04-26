package org.jasig.portlet.emailpreview.service.link;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

/**
 * LinkServiceRegistryImpl provides the default implementation of 
 * ILinkServiceRegistry.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component
public class LinkServiceRegistryImpl implements ILinkServiceRegistry {
    
    private Map<String, IEmailLinkService> serviceMap = new HashMap<String, IEmailLinkService>();
    
    /**
     * 
     * @param services
     */
    @Resource(name = "linkServices")
    @Required
    public void setServices(Collection<IEmailLinkService> services) {
        serviceMap.clear();
        for (IEmailLinkService service : services) {
            registerService(service);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.ILinkServiceRegistry#getEmailLinkService(java.lang.String)
     */
    public IEmailLinkService getEmailLinkService(String key) {
        return serviceMap.get(key);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.ILinkServiceRegistry#getServices()
     */
    public Collection<IEmailLinkService> getServices() {
        return serviceMap.values();
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.ILinkServiceRegistry#registerService(org.jasig.portlet.emailpreview.service.IEmailLinkService)
     */
    public void registerService(IEmailLinkService linkService) {
        serviceMap.put(linkService.getKey(), linkService);
    }
    
}
