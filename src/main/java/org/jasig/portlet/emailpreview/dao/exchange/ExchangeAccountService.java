package org.jasig.portlet.emailpreview.dao.exchange;

import javax.mail.Folder;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IEmailAccountService;
import org.jasig.portlet.emailpreview.service.IServiceBroker;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the email account service for Exchange using Exchange Web Services.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public class ExchangeAccountService implements IEmailAccountService {

    @Autowired(required = true)
    private IAuthenticationServiceRegistry authServiceRegistry;

    @Autowired(required = true)
    private IServiceBroker serviceBroker;

    @Autowired
    private IExchangeAccountDao dao;

    @Autowired
    private IExchangeCredentialsService exchangeCredentialsService;

    public void setExchangeCredentialsService(IExchangeCredentialsService exchangeCredentialsService) {
        this.exchangeCredentialsService = exchangeCredentialsService;
    }

    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public AccountSummary getAccountSummary(PortletRequest req, int start, int max, boolean refresh,
                                            String folder) throws EmailPreviewException {

        String username = req.getRemoteUser();
        if (username == null) {
            throw new EmailPreviewException("Anonymous access is not supported");
        }

        MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        config.setInboxFolderName(folder);
        IAuthenticationService authService = populateCredentials(req, config);

        String mailAccount = authService.getMailAccountName(req, config);

        if (log.isDebugEnabled()) {
            log.debug("Account summary requested for " + username + ", folder=" + folder + ", start=" + start
                    + " max=" + max + " refresh=" + refresh);
        }

        if (refresh) {
            dao.clearAccountSummaryCache(username, mailAccount);
        }

        AccountSummary rslt = dao.fetchAccountSummaryFromStore(config, username, mailAccount, start, max, folder);

        // NB:  Now we must make sure we return the right
        // AccountSummary based on *all* the parameters, not just the ones
        // annotated with @PartialCacheKey on fetchAccountSummaryFromStore.
        // JNW: To handle pagination (changing start) or # entries per page.  Folder can change but UI currently requests with refresh=true.
        // todo: move cache logic inside dao and have it manage this by including fields in cache key

        if ( rslt.getMessagesStart() != start || rslt.getMessagesMax() != max) {

            if (log.isDebugEnabled()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Clearing AccountSummary cache for username '")
                        .append(username).append("', mailAccount '")
                        .append(mailAccount).append("':  start=[")
                        .append(rslt.getMessagesStart()).append(" prev, ")
                        .append(start).append(" current] ").append("count=[")
                        .append(rslt.getMessagesMax()).append(" prev, ")
                        .append(max).append(" current]");
                log.debug(msg.toString());
            }

            // Clear the cache & try again
            dao.clearAccountSummaryCache(username, mailAccount);
            rslt = dao.fetchAccountSummaryFromStore(config, username, mailAccount, start, max, folder);
        }

        return rslt;
    }

    private IAuthenticationService populateCredentials(PortletRequest req, MailStoreConfiguration config) {
        IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
        if (authService == null) {
            String msg = "Unrecognized authentication service:  " + config.getAuthenticationServiceKey();
            log.error(msg);
            throw new EmailPreviewException(msg);
        }
        exchangeCredentialsService.initialize(req, config, authService);
        return authService;
    }

    @Override
    public EmailMessage getMessage(PortletRequest req, String messageId) {
        MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        IAuthenticationService authService = populateCredentials(req, config);

        if (config.getMarkMessagesAsRead()) {
            setSeenFlag(req, new String[] {messageId}, true);
            dao.clearAccountSummaryCache(req.getRemoteUser(), authService.getMailAccountName(req, config));
        }

        return dao.getMessage(messageId, config);
    }

    @Override
    public boolean deleteMessages(PortletRequest req, String[] messageIds) {
        MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        IAuthenticationService authService = populateCredentials(req, config);
        dao.deleteMessages(messageIds);
        return true;
    }

    @Override
    public boolean setSeenFlag(PortletRequest req, String[] messageIds, boolean read) {
        MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        IAuthenticationService authService = populateCredentials(req, config);
        dao.setMessageReadStatus(messageIds, read);
        return true;
    }

    @Override
    public Folder[] getAllUserInboxFolders(PortletRequest req) {
        MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        IAuthenticationService authService = populateCredentials(req, config);
        return dao.getAllUserInboxFolders().toArray(new Folder[0]);
    }

}
