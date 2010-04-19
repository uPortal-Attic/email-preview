package org.jasig.portlet.emailpreview.dao.impl;

import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.springframework.stereotype.Component;

@Component
public class PortletPreferencesMailStoreDaoImpl implements IMailStoreDao {
    
    private static final String CONNECTION_TIMEOUT_KEY = "connectionTimeout";
    private static final String HOST_KEY = "host";
    private static final String PORT_KEY = "port";
    private static final String INBOX_NAME_KEY = "inboxName";
    private static final String PROTOCOL_KEY = "protocol";
    private static final String TIMEOUT_KEY = "timeout";
    
    /* (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.dao.impl.IMailStoreDao#getConfiguration(javax.portlet.PortletRequest)
     */
    public MailStoreConfiguration getConfiguration(PortletRequest request) {
        
        PortletPreferences preferences = request.getPreferences();
        
        MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost(preferences.getValue(HOST_KEY, null));
        config.setInboxFolderName(preferences.getValue(INBOX_NAME_KEY, null));
        config.setProtocol(preferences.getValue(PROTOCOL_KEY, null));
        
        try {
            int port = Integer.parseInt(preferences.getValue(PORT_KEY, "25"));
            config.setPort(port);
        } catch (NumberFormatException e) {
        }

        try {
            int connectionTimeout = Integer.parseInt(preferences.getValue(CONNECTION_TIMEOUT_KEY, "-1"));
            config.setConnectionTimeout(connectionTimeout);
        } catch (NumberFormatException e) {
        }

        try {
            int timeout = Integer.parseInt(preferences.getValue(TIMEOUT_KEY, "-1"));
            config.setTimeout(timeout);
        } catch (NumberFormatException e) {
        }

        return config;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.dao.impl.IMailStoreDao#saveConfiguration(javax.portlet.ActionRequest, org.jasig.portlet.emailpreview.MailStoreConfiguration)
     */
    public void saveConfiguration(ActionRequest request, MailStoreConfiguration config) {
        
        PortletPreferences preferences = request.getPreferences();
        
        try {
            preferences.setValue(HOST_KEY, config.getHost());
            preferences.setValue(PROTOCOL_KEY, config.getProtocol());
            preferences.setValue(INBOX_NAME_KEY, config.getInboxFolderName());
            preferences.setValue(PORT_KEY, String.valueOf(config.getPort()));
            preferences.setValue(CONNECTION_TIMEOUT_KEY, String.valueOf(config.getConnectionTimeout()));
            preferences.setValue(TIMEOUT_KEY, String.valueOf(config.getTimeout()));
            
            preferences.store();
        } catch (Exception e) {
            throw new RuntimeException("Failed to store configuration", e);
        }
        
    }

}
