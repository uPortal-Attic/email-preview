package org.jasig.portlet.emailpreview.service.auth;

import java.util.Collection;

public interface IAuthenticationServiceRegistry {

    /**
     * Register a authentication service.
     * 
     * @param authService
     */
    public void registerService(IAuthenticationService authService);
    
    /**
     * Return an instance of the authentication service associated with the supplied key.
     * If no matching authentication service can be found, this method will return
     * <code>null</code>.
     * 
     * @param key
     * @return
     */
    public IAuthenticationService getAuthenticationService(String key);
    
    /**
     * Return a list of all currently-registered authentication services.
     * 
     * @return
     */
    public Collection<IAuthenticationService> getServices();


}
