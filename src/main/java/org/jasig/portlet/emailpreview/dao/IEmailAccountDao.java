package org.jasig.portlet.emailpreview.dao;

import javax.mail.Authenticator;

import org.jasig.portlet.emailpreview.AccountInfo;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;

public interface IEmailAccountDao {

    public abstract AccountInfo retrieveEmailAccountInfo(
            MailStoreConfiguration storeConfig, Authenticator auth, int messageCount) throws EmailPreviewException;

}