/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portlet.emailpreview.dao;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.caching.AccountSummaryCacheKeyGeneratorImpl;
import org.jasig.portlet.emailpreview.caching.IAccountSummaryCacheKeyGenerator;
import org.jasig.portlet.emailpreview.caching.IMailAccountCacheKeyGenerator;
import org.jasig.portlet.emailpreview.caching.MailAccountCacheKeyGeneratorImpl;
import org.jasig.portlet.emailpreview.service.ICredentialsProvider;
import org.jasig.portlet.emailpreview.service.IServiceBroker;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;

import javax.mail.Folder;
import javax.portlet.PortletRequest;
import java.util.List;

/**
 * Implements the email account service, handling caching of data for efficiency.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public class EmailAccountService implements IEmailAccountService {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired(required = true)
    private IAuthenticationServiceRegistry authServiceRegistry;

    @Autowired(required = true)
    @Qualifier("simpleServiceBroker")
    private IServiceBroker serviceBroker;

    private IMailAccountDao dao;
    @Autowired
    private ICredentialsProvider credentialsService;

    @Autowired
    @Qualifier("inboxCache")
    private Cache inboxCache;
    private IAccountSummaryCacheKeyGenerator accountSummaryCacheKeyGenerator = new AccountSummaryCacheKeyGeneratorImpl();

    @Autowired
    @Qualifier("exchangeFolderCache")
    private Cache folderCache;
    private IMailAccountCacheKeyGenerator folderCacheKeyGenerator = new MailAccountCacheKeyGeneratorImpl();

    private String folderCacheKeyPrefix = "MailFolders";

    public void setInboxCache(Cache inboxCache) {
        this.inboxCache = inboxCache;
    }

    public void setAccountSummaryCacheKeyGenerator(IAccountSummaryCacheKeyGenerator accountSummaryCacheKeyGenerator) {
        this.accountSummaryCacheKeyGenerator = accountSummaryCacheKeyGenerator;
    }

    public void setFolderCache(Cache folderCache) {
        this.folderCache = folderCache;
    }

    public void setFolderCacheKeyGenerator(IMailAccountCacheKeyGenerator folderCacheKeyGenerator) {
        this.folderCacheKeyGenerator = folderCacheKeyGenerator;
    }

    public void setFolderCacheKeyPrefix(String folderCacheKeyPrefix) {
        this.folderCacheKeyPrefix = folderCacheKeyPrefix;
    }

    @Required
    public void setDao(IMailAccountDao dao) {
        this.dao = dao;
    }

    public void setCredentialsService(ICredentialsProvider credentialsService) {
        this.credentialsService = credentialsService;
    }

    @Override
    public AccountSummary getAccountSummary(PortletRequest req, int start, int max, boolean refresh,
                                            String folder) {

        String username = req.getRemoteUser();
        if (username == null) {
            throw new EmailPreviewException("Anonymous access is not supported");
        }

        MailStoreConfiguration config = initializeEnvironment(req);
        config.setInboxFolderName(folder);
        String mailAccount = config.getMailAccount();

        String key = accountSummaryCacheKeyGenerator.getKey(credentialsService.getUsername(), mailAccount, folder, config.getProtocol());
        if (!refresh) {
            Element element = inboxCache.get(key);
            if (element != null) {
                AccountSummary summary = (AccountSummary) element.getObjectValue();
                // If user has changed their starting index or max # of messages to return, need to refresh
                if (summary.getMessagesStart() == start && summary.getMessagesMax() == max) {
                    log.debug("Returning cached AccountSummary for [username={}, mailAccount={}, folder={}, start={}, max={}]",
                            username, mailAccount, folder, start, max);
                    return summary;
                }
                log.debug("Different min or max so setting refresh = true for username={}, mailAccount={}",
                        username, mailAccount);
                refresh = true;
            }
        }

        log.debug("Creating new AccountSummary for [username={}, mailAccount={}, folder={}, start={}, max={}]",
                username, mailAccount, folder, start, max);

        AccountSummary summary = dao.fetchAccountSummaryFromStore(config, username, mailAccount, folder, start, max, req);
        Element element = new Element(key, summary);
        inboxCache.put(element);
        return summary;
    }

    private void populateCredentials(PortletRequest req, MailStoreConfiguration config) {
        IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
        credentialsService.initialize(req, config, authService);
    }

    private MailStoreConfiguration setupMailStoreConfig(PortletRequest req) {
        MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
        if (authService == null) {
            String msg = "Unrecognized authentication service:  " + config.getAuthenticationServiceKey();
            log.error(msg);
            throw new EmailPreviewException(msg);
        }
        String mailAccount = authService.getMailAccountName(req, config);
        config.setMailAccount(mailAccount);
        return config;
    }

    private MailStoreConfiguration initializeEnvironment(PortletRequest req) {
        MailStoreConfiguration config = setupMailStoreConfig(req);
        populateCredentials(req, config);
        return config;
    }

    private void clearInboxCache(MailStoreConfiguration config) {
        String key = accountSummaryCacheKeyGenerator.getKey(credentialsService.getUsername(), config.getMailAccount(),
                config.getInboxFolderName(), config.getProtocol());
        inboxCache.remove(key);
    }

    @Override
    public EmailMessage getMessage(PortletRequest req, String messageId) {
        MailStoreConfiguration config = initializeEnvironment(req);
        if (config.getMarkMessagesAsRead()) {
            log.debug("Getting message - marking message as read (may already be marked as read");
            setSeenFlag(req, new String[] {messageId}, true);
            // Account summary is now invalid so clear it
            clearInboxCache(config);
        }
        log.debug("Getting message id={}", messageId);
        return dao.getMessage(config, messageId, req);
    }

    @Override
    public boolean deleteMessages(PortletRequest req, String[] messageIds) {
        MailStoreConfiguration config = initializeEnvironment(req);
        if (log.isDebugEnabled()) {
            log.debug("Deleting messages {}", convertIdsToString(messageIds));
        }
        boolean wasDeleted = dao.deleteMessages(config, messageIds);
        // Account summary is now invalid so clear it
        clearInboxCache(config);
        return wasDeleted;
    }

    private String convertIdsToString(String[] messageIds) {
        StringBuffer ids = new StringBuffer();
        for (String id : messageIds) {
            ids.append(id).append(",");
        }
        ids.deleteCharAt(ids.length() - 1);
        return ids.toString();
    }

    @Override
    public boolean setSeenFlag(PortletRequest req, String[] messageIds, boolean read) {
        MailStoreConfiguration config = initializeEnvironment(req);
        boolean result = dao.setMessageReadStatus(config, messageIds, read);
        // Account summary is now invalid so clear it
        clearInboxCache(config);
        return result;
    }

    @Override
    public List<Folder> getAllUserInboxFolders(PortletRequest req) {
        MailStoreConfiguration config = setupMailStoreConfig(req);
        populateCredentials(req, config);

        // Retrieve from cache or fetch
        String key = folderCacheKeyGenerator.getKey(credentialsService.getUsername(), config.getMailAccount(), folderCacheKeyPrefix);
        Element element = folderCache.get(key);
        if (element != null) {
            log.debug("Returning folder list from cache for user {} mail account {}", credentialsService.getUsername(), config.getMailAccount());
            return (List<Folder>) element.getObjectValue();
        } else {
            log.debug("Fetching all folders");
            List<Folder> folders = dao.getAllUserInboxFolders(config);
            folderCache.put(new Element(key, folders));
            return folders;
        }
    }

}
