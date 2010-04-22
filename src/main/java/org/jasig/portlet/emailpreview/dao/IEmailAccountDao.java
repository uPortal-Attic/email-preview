package org.jasig.portlet.emailpreview.dao;

import javax.mail.Authenticator;

import org.jasig.portlet.emailpreview.AccountInfo;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IEmailAccountDao {

    public AccountInfo retrieveEmailAccountInfo(
            MailStoreConfiguration storeConfig, Authenticator auth,
            int maxMessages) throws EmailPreviewException;
    
    public EmailMessage retrieveMessage(MailStoreConfiguration storeConfig, Authenticator auth, int messageNum);
}