package org.jasig.portlet.emailpreview.dao;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.impl.PortletPreferencesMailStoreDaoImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PortletPreferencesMailStoreDaoImplTest {
    
    private @Mock PortletPreferences preferences;
    private @Mock ActionRequest request;
    private MailStoreConfiguration configuration;
    private IMailStoreDao mailStoreDao;
    
    private String host = "imap.gmail.com";
    private int port = 993;
    private String protocol = "imaps";
    private String inboxName = "INBOX";
    private int timeout = -1;
    private int connectionTimeout = -1;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mailStoreDao = new PortletPreferencesMailStoreDaoImpl();
        
        configuration = new MailStoreConfiguration();
        configuration.setHost("imap.gmail.com");
        configuration.setPort(993);
        configuration.setProtocol("imaps");
        configuration.setInboxFolderName("INBOX");
        configuration.setTimeout(-1);
        configuration.setConnectionTimeout(-1);
        
        when(request.getPreferences()).thenReturn(preferences);
        when(preferences.getValue("host", null)).thenReturn(host);
        when(preferences.getValue("protocol", null)).thenReturn(protocol);
        when(preferences.getValue("inboxName", null)).thenReturn(inboxName);
        when(preferences.getValue("port", "25")).thenReturn(String.valueOf(port));
        when(preferences.getValue("timeout", "-1")).thenReturn(String.valueOf(timeout));
        when(preferences.getValue("connectionTimeout", "-1")).thenReturn(String.valueOf(connectionTimeout));
        
    }
    
    @Test
    public void testGetConfiguration() {
        MailStoreConfiguration config = mailStoreDao.getConfiguration(request);
        assert (config.equals(configuration));
    }
    
    @Test
    public void testStoreConfiguration() throws ReadOnlyException {
        mailStoreDao.saveConfiguration(request, configuration);
        
        verify(preferences).setValue("host", host);
        verify(preferences).setValue("protocol", protocol);
        verify(preferences).setValue("inboxName", inboxName);
        verify(preferences).setValue("port", String.valueOf(port));
        verify(preferences).setValue("timeout", String.valueOf(timeout));
        verify(preferences).setValue("connectionTimeout", String.valueOf(connectionTimeout));
    }

}
