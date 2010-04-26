package org.jasig.portlet.emailpreview.service.link;

import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.link.SimpleEmailLinkServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class SimpleEmailLinkServiceImplTest {
    
    SimpleEmailLinkServiceImpl linkService;
    MailStoreConfiguration configuration;
    @Mock PortletRequest request;
        
    @Before
    public void setUp() { 
        MockitoAnnotations.initMocks(this);
        
        linkService = new SimpleEmailLinkServiceImpl();
        
        configuration = new MailStoreConfiguration();
        configuration.getAdditionalProperties().put(SimpleEmailLinkServiceImpl.INBOX_URL_PROPERTY, "http://mail.google.com");
    }
    
    @Test
    public void testGetInboxUrl() {
        String inboxUrl = linkService.getInboxUrl(request, configuration);
        assert "http://mail.google.com".equals(inboxUrl);
    }

}
