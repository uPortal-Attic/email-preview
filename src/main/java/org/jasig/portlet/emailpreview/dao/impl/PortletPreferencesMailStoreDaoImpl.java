package org.jasig.portlet.emailpreview.dao.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IMailStoreDao;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component
public class PortletPreferencesMailStoreDaoImpl implements IMailStoreDao {
    
    protected static final String CONNECTION_TIMEOUT_KEY = "connectionTimeout";
    protected static final String HOST_KEY = "host";
    protected static final String PORT_KEY = "port";
    protected static final String INBOX_NAME_KEY = "inboxName";
    protected static final String PROTOCOL_KEY = "protocol";
    protected static final String TIMEOUT_KEY = "timeout";
    protected static final String LINK_SERVICE_KEY = "linkServiceKey";
    protected static final String AUTHENTICATION_SERVICE_KEY = "authenticationServiceKey";
    
    protected static final List<String> RESERVED_PROPERTIES = Arrays
            .asList(new String[] { HOST_KEY, PORT_KEY, INBOX_NAME_KEY,
                    PROTOCOL_KEY, TIMEOUT_KEY, CONNECTION_TIMEOUT_KEY, 
                    LINK_SERVICE_KEY, AUTHENTICATION_SERVICE_KEY });    
    
    /* (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.dao.impl.IMailStoreDao#getConfiguration(javax.portlet.PortletRequest)
     */
    public MailStoreConfiguration getConfiguration(PortletRequest request) {
        
        PortletPreferences preferences = request.getPreferences();
        
        MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost(preferences.getValue(HOST_KEY, null));
        config.setInboxFolderName(preferences.getValue(INBOX_NAME_KEY, null));
        config.setProtocol(preferences.getValue(PROTOCOL_KEY, null));
        config.setLinkServiceKey(preferences.getValue(LINK_SERVICE_KEY, null));
        config.setAuthenticationServiceKey(preferences.getValue(AUTHENTICATION_SERVICE_KEY, null));
        
        // set the port number
        try {
            int port = Integer.parseInt(preferences.getValue(PORT_KEY, "25"));
            config.setPort(port);
        } catch (NumberFormatException e) {
        }

        // set the connection timeout
        try {
            int connectionTimeout = Integer.parseInt(preferences.getValue(CONNECTION_TIMEOUT_KEY, "-1"));
            config.setConnectionTimeout(connectionTimeout);
        } catch (NumberFormatException e) {
        }

        // set the timeout
        try {
            int timeout = Integer.parseInt(preferences.getValue(TIMEOUT_KEY, "-1"));
            config.setTimeout(timeout);
        } catch (NumberFormatException e) {
        }
        
        
        /*
         * Iterate through the preferences map, adding all preferences not 
         * handled above to either the java mail properties map or the 
         * arbitrary properties map as appropriate.
         * 
         * This code assumes that all java mail properties begin with
         * "mail." and does now allow administrators to define arbitary
         * properties beginning with that string.
         */

        @SuppressWarnings("unchecked")
        Map<String, String> preferenceMap = preferences.getMap();
        for (String key : preferenceMap.keySet()) {
            
            if (!RESERVED_PROPERTIES.contains(key)) {
                String value = preferenceMap.get(key);

                if (key.startsWith("mail.")) {
                    config.getJavaMailProperties().put(key, value);
                } else {
                    config.getAdditionalProperties().put(key, value);
                }
            }
            
        }


        return config;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.dao.impl.IMailStoreDao#saveConfiguration(javax.portlet.ActionRequest, org.jasig.portlet.emailpreview.MailStoreConfiguration)
     */
    public void saveConfiguration(ActionRequest request, MailStoreConfiguration config) {
        
        PortletPreferences preferences = request.getPreferences();
        
        try {
            
            for (Map.Entry<String, String> entry : config.getAdditionalProperties().entrySet()) {
                preferences.setValue(entry.getKey(), entry.getValue());
            }
            
            for (Map.Entry<String, String> entry : config.getJavaMailProperties().entrySet()) {
                preferences.setValue(entry.getKey(), entry.getValue());
            }
            
            preferences.setValue(HOST_KEY, config.getHost());
            preferences.setValue(PROTOCOL_KEY, config.getProtocol());
            preferences.setValue(INBOX_NAME_KEY, config.getInboxFolderName());
            preferences.setValue(PORT_KEY, String.valueOf(config.getPort()));
            preferences.setValue(CONNECTION_TIMEOUT_KEY, String.valueOf(config.getConnectionTimeout()));
            preferences.setValue(TIMEOUT_KEY, String.valueOf(config.getTimeout()));
            preferences.setValue(LINK_SERVICE_KEY, String.valueOf(config.getLinkServiceKey()));
            preferences.setValue(AUTHENTICATION_SERVICE_KEY, String.valueOf(config.getAuthenticationServiceKey()));
            
            preferences.store();
        } catch (Exception e) {
            throw new RuntimeException("Failed to store configuration", e);
        }
        
    }

}
