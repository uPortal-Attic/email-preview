package org.jasig.portlet.emailpreview.dao;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.mail.Authenticator;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.SimplePasswordAuthenticator;
import org.jasig.portlet.emailpreview.dao.impl.EmailAccountDaoImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

public class EmailAccountDaoImplIntegrationTest {
    
    GreenMail greenMail;
    
    private MailStoreConfiguration testMailStore;
    private EmailAccountDaoImpl accountDao;
    
    @Before
    public void setUp() throws InterruptedException {
        
        testMailStore = new MailStoreConfiguration();
        testMailStore.setHost("localhost");
        testMailStore.setPort(3993);
        testMailStore.setProtocol("imaps");
        
        accountDao = new EmailAccountDaoImpl();
        
        greenMail = new GreenMail(); //uses test ports by default
        greenMail.start();
        GreenMailUtil.sendTextEmailTest("to@localhost.com", "from@localhost.com", "subject", "body"); //replace this with your send code
        assert greenMail.waitForIncomingEmail(5000, 1);

    }
    
    @Test
    public void test() throws UnknownHostException, IOException, InterruptedException {
        assert "body".equals(GreenMailUtil.getBody(greenMail.getReceivedMessages()[0]));
        
        Authenticator auth = new SimplePasswordAuthenticator("to@localhost.com", "to@localhost.com");
//        accountDao.retrieveEmailAccountInfo(testMailStore, auth, 5);
        
    }
    
    @After
    public void tearDown() {
        greenMail.stop();
    }

}
