package org.jasig.portlet.emailpreview.service.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.SimplePasswordAuthenticator;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;

public class PortletPreferencesCredentialsAuthenticationServiceImpl implements
        IAuthenticationService {
    
    protected static final String KEY = "portletPreferences";
    protected static final String USERNAME_KEY = "username";
    protected static final String PASSWORD_KEY = "password";
    
    private final List<ConfigurationParameter> userParameters;
    
    public PortletPreferencesCredentialsAuthenticationServiceImpl() {
        List<ConfigurationParameter> params = new ArrayList<ConfigurationParameter>();
        
        ConfigurationParameter usernameParam = new ConfigurationParameter();
        usernameParam.setKey(USERNAME_KEY);
        usernameParam.setLabel("Inbox folder name");
        usernameParam.setRequiresEncryption(true);
        params.add(usernameParam);

        ConfigurationParameter passwordParam = new ConfigurationParameter();
        passwordParam.setKey(PASSWORD_KEY);
        passwordParam.setLabel("Inbox folder name");
        passwordParam.setRequiresEncryption(true);
        params.add(passwordParam);
        
        this.userParameters = params;

    }

    public Authenticator getAuthenticator(PortletRequest request,
            MailStoreConfiguration config) {
        
        String username = config.getAdditionalProperties().get(USERNAME_KEY);
        String password = config.getAdditionalProperties().get(PASSWORD_KEY);
        
        return new SimplePasswordAuthenticator(username, password);
    }

    public String getKey() {
        return KEY;
    }

    public List<ConfigurationParameter> getAdminConfigurationParameters() {
        return Collections.<ConfigurationParameter>emptyList();
    }

    public List<ConfigurationParameter> getUserConfigurationParameters() {
        return this.userParameters;
    }

}
