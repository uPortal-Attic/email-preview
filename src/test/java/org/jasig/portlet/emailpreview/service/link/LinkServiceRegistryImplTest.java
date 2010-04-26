package org.jasig.portlet.emailpreview.service.link;

import java.util.Collection;

import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.jasig.portlet.emailpreview.service.link.LinkServiceRegistryImpl;
import org.jasig.portlet.emailpreview.service.link.SimpleEmailLinkServiceImpl;
import org.junit.Test;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class LinkServiceRegistryImplTest {
    
    @Test
    public void testRegistry() {
        IEmailLinkService linkService = new SimpleEmailLinkServiceImpl();
        ILinkServiceRegistry registry = new LinkServiceRegistryImpl();
        registry.registerService(linkService);
        
        Collection<IEmailLinkService> services = registry.getServices();
        assert services.size() == 1;
        
        IEmailLinkService defaultService = registry.getEmailLinkService("default");
        assert defaultService != null;
        
    }

}
