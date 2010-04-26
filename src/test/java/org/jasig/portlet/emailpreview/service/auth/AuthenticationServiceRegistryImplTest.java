package org.jasig.portlet.emailpreview.service.auth;

import java.util.Collection;

import org.junit.Test;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class AuthenticationServiceRegistryImplTest {
    
    @Test
    public void testRegistry() {
        IAuthenticationService authenticationService = new CachedPasswordAuthenticationServiceImpl();
        IAuthenticationServiceRegistry registry = new AuthenticationServiceRegistryImpl();
        registry.registerService(authenticationService);
        
        Collection<IAuthenticationService> services = registry.getServices();
        assert services.size() == 1;
        
        IAuthenticationService defaultService = registry.getAuthenticationService("cachedPassword");
        assert defaultService != null;
        
    }

}
