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
package org.jasig.portlet.emailpreview.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountInfo;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IEmailAccountDao;
import org.jasig.portlet.emailpreview.dao.IMailStoreDao;
import org.jasig.portlet.emailpreview.exception.MailAuthenticationException;
import org.jasig.portlet.emailpreview.exception.MailTimeoutException;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.jasig.portlet.emailpreview.servlet.HttpErrorResponseController;
import org.jasig.web.service.AjaxPortletSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 */
@Controller
@RequestMapping("VIEW")
public class EmailAccountSummaryController {

    protected final Log log = LogFactory.getLog(getClass());

    private IEmailAccountDao accountDao;

    @Autowired(required = true)
    public void setAccountInfoDao(IEmailAccountDao accountInfoDao) {
        this.accountDao = accountInfoDao;
    }

    private IMailStoreDao mailStoreDao;

    @Autowired(required = true)
    public void setMailStoreDao(IMailStoreDao mailStoreDao) {
        this.mailStoreDao = mailStoreDao;
    }

    private AjaxPortletSupportService ajaxPortletSupportService;

    /**
     * Set the service for handling portlet AJAX requests.
     *
     * @param ajaxPortletSupportService
     */
    @Autowired(required = true)
    public void setAjaxPortletSupportService(
                    AjaxPortletSupportService ajaxPortletSupportService) {
            this.ajaxPortletSupportService = ajaxPortletSupportService;
    }

    private ILinkServiceRegistry linkServiceRegistry;

    @Autowired(required = true)
    public void setLinkServiceRegistry(ILinkServiceRegistry linkServiceRegistry) {
        this.linkServiceRegistry = linkServiceRegistry;
    }

    private IAuthenticationServiceRegistry authServiceRegistry;

    @Autowired(required = true)
    public void setAuthenticationServiceRegistry(IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
    }

    @RequestMapping(params = "action=accountSummary")
    public void getAccountSummary(ActionRequest request, ActionResponse response,
            @RequestParam("pageStart") int pageStart,
            @RequestParam("numberOfMessages") int numberOfMessages) throws IOException {

        // Define view and generate model
        Map<String, Object> model = new HashMap<String, Object>();

        String username = request.getRemoteUser();
        try {

            MailStoreConfiguration config = mailStoreDao.getConfiguration(request);

            IEmailLinkService linkService = linkServiceRegistry.getEmailLinkService(config.getLinkServiceKey());
            if (linkService != null) {
                String inboxUrl = linkService.getInboxUrl(request, config);
                model.put("inboxUrl", inboxUrl);
            }

            IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
            if (authService == null) {
                String msg = "Unrecognized authentication service:  "
                                + config.getAuthenticationServiceKey();
                log.error(msg);
                throw new RuntimeException(msg);
            }
            Authenticator auth = authService.getAuthenticator(request, config);
            String mailAccountName = authService.getMailAccountName(request, config);

            // Check if this is a refresh call;  clear cache if it is
            if (Boolean.parseBoolean(request.getParameter("forceRefresh"))) {
                accountDao.clearCache(username, mailAccountName);
            }

            // Get current user's account information
            AccountInfo accountInfo = getAccountInfo(username, mailAccountName,
                                config, auth, pageStart, numberOfMessages);

            model.put("accountInfo", accountInfo);

            ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, request, response);

        } catch (MailAuthenticationException ex) {
            model.put(HttpErrorResponseController.HTTP_ERROR_CODE, HttpServletResponse.SC_UNAUTHORIZED);
            ajaxPortletSupportService.redirectAjaxResponse("ajax/error", model, request, response);
            log.info( "Authentication Failure (username='" + username + "') : " + ex.getMessage() );
        } catch (MailTimeoutException ex) {
            model.put(HttpErrorResponseController.HTTP_ERROR_CODE, HttpServletResponse.SC_GATEWAY_TIMEOUT);
            ajaxPortletSupportService.redirectAjaxResponse("ajax/error", model, request, response);
            log.error( "Mail Service Timeout", ex);
        } catch (Exception ex) {
            /* ********************************************************
                In the case of an unknown error we want to send the
                exception's message back to the portlet. This will
                let implementers write specific instructions for
                their service desks to follow for specific errors.
            ******************************************************** */

            model.put( "errorMessage", ex.getMessage() );
            ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, request, response);
            log.error( "Unanticipated Error", ex);
        }

    }

    private AccountInfo getAccountInfo(String username, String mailAccount,
            MailStoreConfiguration config, Authenticator auth, int start,
            int count) throws EmailPreviewException {

        // NB:  The role of this method is to make sure we return the right
        // AccountInfo based on *all* the parameters, not just the ones
        // annotated with @PartialCacheKey on fetchAccountInfoFromStore (below).

        AccountInfo rslt = accountDao.fetchAccountInfoFromStore(username,
                        mailAccount, config, auth, start, count);

        if (rslt.getMessagesStart() != start || rslt.getMessagesCount() != count) {
            
            if (log.isTraceEnabled()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Clearing AccountInfo cache for username '")
                                .append(username).append("', mailAccount '")
                                .append(mailAccount).append("':  start=[")
                                .append(rslt.getMessagesStart()).append(" prev, ")
                                .append(start).append(" current] ").append("count=[")
                                .append(rslt.getMessagesCount()).append(" prev, ")
                                .append(count).append(" current]");
                log.trace(msg.toString());
            }
            
            // Clear the cache & try again
            accountDao.clearCache(username, mailAccount);
            rslt = accountDao.fetchAccountInfoFromStore(username,
                    mailAccount, config, auth, start, count);
        }

        return rslt;

    }

}
