package org.jasig.portlet.emailpreview;

import org.junit.Test;

public class EmailMessageTest {
    
    @Test
    public void testSubjectName() {
        EmailMessage message = new EmailMessage();
        message.setSender("Test User <testuser@nowhere.net>");
        assert "Test User".equals(message.getSenderName());
        
        message.setSender("Test User");
        assert "Test User".equals(message.getSenderName());
    }

}
