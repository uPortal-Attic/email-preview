package org.jasig.portlet.emailpreview.service.auth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationServiceRegistryImpl implements IAuthenticationServiceRegistry {
    
    private Map<String, IAuthenticationService> serviceMap = new HashMap<String, IAuthenticationService>();

    @Resource(name = "authServices")
    @Required
    public void setServices(Collection<IAuthenticationService> services) {
        serviceMap.clear();
        for (IAuthenticationService service : services) {
            registerService(service);
        }
    }

    public IAuthenticationService getAuthenticationService(String key) {
        return serviceMap.get(key);
    }

    public Collection<IAuthenticationService> getServices() {
        return serviceMap.values();
    }

    public void registerService(IAuthenticationService authService) {
        serviceMap.put(authService.getKey(), authService);
    }

}
