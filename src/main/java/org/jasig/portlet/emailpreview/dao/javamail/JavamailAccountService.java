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
package org.jasig.portlet.emailpreview.dao.javamail;

import java.io.IOException;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.Flags.Flag;
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
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A Data Access Object (DAO) for retrieving email account information.
 * Currently all the information retrieved is related to the user's
 * inbox.
 *
 * @author Andreas Christoforides
 * @author Drew Wills, drew@unicon.net
 */
@Component
public final class JavamailAccountService implements IEmailAccountService {

    @Autowired(required = true)
    private IAuthenticationServiceRegistry authServiceRegistry;

    @Autowired(required = true)
    private IServiceBroker serviceBroker;

    @Autowired
    private IJavamailAccountDao dao;
    
    private final Log log = LogFactory.getLog(getClass());
    
    /*
     * Public API
     */

    @Override
    public AccountSummary getAccountSummary(PortletRequest req, int start,
            int max, boolean refresh) throws EmailPreviewException {
        
        String username = req.getRemoteUser();
        if (username == null) {
            throw new EmailPreviewException("Anonymous access is not supported");
        }
        
        MailStoreConfiguration config = serviceBroker.getConfiguration(req);

        IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
        if (authService == null) {
            String msg = "Unrecognized authentication service:  "
                            + config.getAuthenticationServiceKey();
            log.error(msg);
            throw new EmailPreviewException(msg);
        }
        Authenticator auth = authService.getAuthenticator(req, config);
        String mailAccount = authService.getMailAccountName(req, config);

        if (refresh) {
            dao.clearCache(username, mailAccount);
        }
        
        AccountSummary rslt = dao.fetchAccountSummaryFromStore(config, auth, username, mailAccount, start, max);

        // NB:  Now we must make sure we return the right
        // AccountSummary based on *all* the parameters, not just the ones
        // annotated with @PartialCacheKey on fetchAccountSummaryFromStore.
        
        if (rslt.getMessagesStart() != start || rslt.getMessagesMax() != max) {

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
            dao.clearCache(username, mailAccount);
            rslt = dao.fetchAccountSummaryFromStore(config, auth, username,
                    mailAccount, start, max);
        }

        return rslt;
        
    }

    @Override
    public EmailMessage getMessage(PortletRequest req, int messageNum) {

        Folder inbox = null;
        try {

            MailStoreConfiguration config = serviceBroker.getConfiguration(req);
            int mode = config.getMarkMessagesAsRead() ? Folder.READ_WRITE : Folder.READ_ONLY;

            IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
            if (authService == null) {
                String msg = "Unrecognized authentication service:  "
                                + config.getAuthenticationServiceKey();
                log.error(msg);
                throw new EmailPreviewException(msg);
            }
            Authenticator auth = authService.getAuthenticator(req, config);

            // Retrieve user's inbox
            Session session = dao.openMailSession(config, auth);
            inbox = dao.getUserInbox(session, config.getInboxFolderName());
            inbox.open(mode);

            Message message = inbox.getMessage(messageNum);
            boolean unread = !message.isSet(Flags.Flag.SEEN);
            if (config.getMarkMessagesAsRead()) {
                message.setFlag(Flag.SEEN, true);
            }
            EmailMessage emailMessage = dao.wrapMessage(message, true, session);
            if (!config.getMarkMessagesAsRead()) {
                // NOTE:  This is more than a little bit annoying.  Apparently
                // the mere act of accessing the body content of a message in
                // Javamail flags the in-memory representation of that message
                // as SEEN.  It does *nothing* to the mail server (the message
                // is still unread in the SOR), but it wreaks havoc on local
                // functions that key off that value and expect it to be
                // accurate.  We're obligated, therefore, to restore the value
                // to what it was before the call to wrapMessage().
                emailMessage.setUnread(unread);
            }

            return emailMessage;
        } catch (MessagingException e) {
            log.error("Messaging exception while retrieving individual message", e);
        } catch (IOException e) {
            log.error("IO exception while retrieving individual message", e);
        } catch (ScanException e) {
            log.error("AntiSamy scanning exception while retrieving individual message", e);
        } catch (PolicyException e) {
            log.error("AntiSamy policy exception while retrieving individual message", e);
        } finally {
            if ( inbox != null ) {
                try {
                    inbox.close(false);
		} catch ( Exception e ) {
                    log.warn("Can't close correctly javamail inbox connection");
		}
		try {
		    inbox.getStore().close();
                } catch ( Exception e ) {
		    log.warn("Can't close correctly javamail store connection");
		}
            }
        }

        return null;

    }

    @Override
    public boolean deleteMessages(PortletRequest req, long[] uids) {

        Folder inbox = null;
        try {

            MailStoreConfiguration config = serviceBroker.getConfiguration(req);

            IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
            if (authService == null) {
                String msg = "Unrecognized authentication service:  "
                                + config.getAuthenticationServiceKey();
                log.error(msg);
                throw new EmailPreviewException(msg);
            }
            Authenticator auth = authService.getAuthenticator(req, config);

            // Retrieve user's inbox
            Session session = dao.openMailSession(config, auth);
            inbox = dao.getUserInbox(session, config.getInboxFolderName());

            // Verify that we can even perform this operation
            if (!(inbox instanceof UIDFolder)) {
                String msg = "Delete feature is supported only for UIDFolder instances";
                throw new UnsupportedOperationException(msg);
            }

            inbox.open(Folder.READ_WRITE);

            Message[] msgs = ((UIDFolder) inbox).getMessagesByUID(uids);
            inbox.setFlags(msgs, new Flags(Flag.DELETED), true);

            return true;  // Indicate success

        } catch (MessagingException e) {
            log.error("Messaging exception while deleting messages", e);
        } finally {
            if ( inbox != null ) {
                try {
                    inbox.close(false);
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail inbox connection");
		}
		try {
		    inbox.getStore().close();
                } catch ( Exception e ) {
		    log.warn("Can't close correctly javamail store connection");
                }
            }
        }

        return false;  // We failed if we reached this point

    }

    @Override
    public boolean setSeenFlag(PortletRequest req, long[] uids, boolean value) {

        Folder inbox = null;
        try {

            MailStoreConfiguration config = serviceBroker.getConfiguration(req);

            IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
            if (authService == null) {
                String msg = "Unrecognized authentication service:  "
                                + config.getAuthenticationServiceKey();
                log.error(msg);
                throw new EmailPreviewException(msg);
            }
            Authenticator auth = authService.getAuthenticator(req, config);

            // Retrieve user's inbox
            Session session = dao.openMailSession(config, auth);
            inbox = dao.getUserInbox(session, config.getInboxFolderName());

            // Verify that we can even perform this operation
            if (!(inbox instanceof UIDFolder)) {
                String msg = "Toggle unread feature is supported only for UIDFolder instances";
                throw new UnsupportedOperationException(msg);
            }

            inbox.open(Folder.READ_WRITE);

            Message[] msgs = ((UIDFolder) inbox).getMessagesByUID(uids);
            inbox.setFlags(msgs, new Flags(Flag.SEEN), value);

            return true;  // Indicate success

        } catch (MessagingException e) {
            log.error("Messaging exception while deleting messages", e);
        } finally {
            if ( inbox != null ) {
                try {
                    inbox.close(false);
		} catch ( Exception e ) {
                    log.warn("Can't close correctly javamail inbox connection");
		}
		try {
		    inbox.getStore().close();
                } catch ( Exception e ) {
		    log.warn("Can't close correctly javamail store connection");
                }
            }
        }

        return false;  // We failed if we reached this point

    }

}
