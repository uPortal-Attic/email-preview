package org.jasig.portlet.emailpreview.service.auth;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.SimplePasswordAuthenticator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class CachedPasswordAuthenticationServiceImplTest {
    
    CachedPasswordAuthenticationServiceImpl authService;
    MailStoreConfiguration configuration;
    @Mock PortletRequest request;
        
    @Before
    public void setUp() { 
        MockitoAnnotations.initMocks(this);
        
        authService = new CachedPasswordAuthenticationServiceImpl();
        configuration = new MailStoreConfiguration();
        
        Map<String, String> userInfo = new HashMap<String, String>();
        userInfo.put("user.login.id", "testuser");
        userInfo.put("password", "pass");
        
        when(request.getAttribute(PortletRequest.USER_INFO)).thenReturn(userInfo);
    }
    
    @Test
    public void testGetAuthenticator() {
        SimplePasswordAuthenticator auth = (SimplePasswordAuthenticator) authService.getAuthenticator(request, configuration);
        SimplePasswordAuthenticator expected = new SimplePasswordAuthenticator("testuser", "pass");
        
        assert expected.equals(auth);
    }


}
