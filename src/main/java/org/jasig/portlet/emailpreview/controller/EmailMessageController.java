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

import java.util.HashMap;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IEmailAccountDao;
import org.jasig.portlet.emailpreview.dao.IMailStoreDao;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
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
public class EmailMessageController {


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

    private IAuthenticationServiceRegistry authServiceRegistry;

    @Autowired(required = true)
    public void setAuthenticationServiceRegistry(IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
    }

    @RequestMapping(params = "action=emailMessage")
    public void showMessage(ActionRequest request, ActionResponse response,
            @RequestParam("messageNum") int messageNum){

        try {

            MailStoreConfiguration config = mailStoreDao.getConfiguration(request);

            IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
            if (authService == null) {
                String msg = "Unrecognized authentication service:  "
                                + config.getAuthenticationServiceKey();
                log.error(msg);
                throw new RuntimeException(msg);
            }
            Authenticator auth = authService.getAuthenticator(request, config);

            // Get current user's account information
            EmailMessage message = accountDao.retrieveMessage(config, auth, messageNum);

            // Define view and generate model
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("message", message);

            ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, request, response);

        } catch (Exception ex) {
            log.error("Error encountered while attempting to retrieve message", ex);
        }

    }

    @RequestMapping(params = "action=deleteMessages")
    public void deleteMessages(ActionRequest req, ActionResponse res,
                @RequestParam(value="selectMessage", required=false) long[] messages) {

        try {

            String deletePermitted = req.getPreferences().getValue(EmailSummaryController.ALLOW_DELETE_KEY, "true");
            if (!Boolean.valueOf(deletePermitted)) {
                String msg = "The delete function is not permitted for this portlet";
                throw new RuntimeException(msg);
            }

            if (messages != null && messages.length != 0) {

                MailStoreConfiguration config = mailStoreDao.getConfiguration(req);

                IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
                if (authService == null) {
                    String msg = "Unrecognized authentication service:  "
                                    + config.getAuthenticationServiceKey();
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
                Authenticator auth = authService.getAuthenticator(req, config);

                accountDao.deleteMessages(config, auth, messages);

            }

            // Define view and generate model
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("success", "success");

            ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, req, res);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete specified messages", e);
        }

    }

}
