package org.jasig.portlet.emailpreview.dao;

import javax.mail.Authenticator;

import org.jasig.portlet.emailpreview.AccountInfo;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;

/**
 * IEmailAccountDao is repsonsible for retrieving email messages from an
 * arbitrary email store.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IEmailAccountDao {

    /**
     * Retrieve a list of recent email from the mail store, as well as a 
     * summary of the email account's current state. 
     * 
     * @param storeConfig
     * @param auth
     * @param maxMessages
     * @return
     * @throws EmailPreviewException
     */
    public AccountInfo retrieveEmailAccountInfo(
            MailStoreConfiguration storeConfig, Authenticator auth,
            int maxMessages) throws EmailPreviewException;
    
    /**
     * Retrieve an individual message from the mail store.
     * 
     * @param storeConfig
     * @param auth
     * @param messageNum
     * @return
     */
    public EmailMessage retrieveMessage(MailStoreConfiguration storeConfig, Authenticator auth, int messageNum);
    
}