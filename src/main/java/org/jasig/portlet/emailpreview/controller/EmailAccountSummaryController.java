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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.dao.IEmailAccountService;
import org.jasig.portlet.emailpreview.exception.MailAuthenticationException;
import org.jasig.portlet.emailpreview.exception.MailTimeoutException;
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

    @Autowired(required = true)
    private IEmailAccountService accountDao;

    @Autowired(required = true)
    private AjaxPortletSupportService ajaxPortletSupportService;
    
    public static final String FORCE_REFRESH_PARAMETER = "forceRefresh";

    @RequestMapping(params = "action=accountSummary")
    public void getAccountSummary(ActionRequest req, ActionResponse res,
            @RequestParam("pageStart") int start,
            @RequestParam("numberOfMessages") int max) throws IOException {

        // Define view and generate model
        Map<String, Object> model = new HashMap<String, Object>();

        String username = req.getRemoteUser();
        try {

            // Force a re-load from the data source if called for by the UI.
            boolean refresh = Boolean.valueOf(req.getParameter(FORCE_REFRESH_PARAMETER));
            
            // Or because of a change in settings.
            if (req.getPortletSession().getAttribute(FORCE_REFRESH_PARAMETER) != null) {
                // Doesn't matter what the value is;  this calles for a refresh...
                refresh = true;
                req.getPortletSession().removeAttribute(FORCE_REFRESH_PARAMETER);
            }

            // Get current user's account information
            AccountSummary accountSummary = accountDao.getAccountSummary(req, start, max, refresh);
            
            // Check for AuthN failure...
            if (accountSummary.isValid()) {
                model.put("accountSummary", accountSummary);
                model.put("inboxUrl", accountSummary.getInboxUrl());
                ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, req, res);
            } else {
                Throwable cause = accountSummary.getErrorCause();
                if (MailAuthenticationException.class.isAssignableFrom(cause.getClass())) {
                    model.put(HttpErrorResponseController.HTTP_ERROR_CODE, HttpServletResponse.SC_UNAUTHORIZED);
                    ajaxPortletSupportService.redirectAjaxResponse("ajax/error", model, req, res);
                    log.info( "Authentication Failure (username='" + username + "') : " + cause.getMessage() );
                } else {
                    // See note below...
                    model.put( "errorMessage", cause.getMessage() );
                    ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, req, res);
                    log.error( "Unanticipated Error", cause);
                }
            }

        } catch (MailTimeoutException ex) {
            model.put(HttpErrorResponseController.HTTP_ERROR_CODE, HttpServletResponse.SC_GATEWAY_TIMEOUT);
            ajaxPortletSupportService.redirectAjaxResponse("ajax/error", model, req, res);
            log.error( "Mail Service Timeout", ex);
        } catch (Exception ex) {
            /* ********************************************************
                In the case of an unknown error we want to send the
                exception's message back to the portlet. This will
                let implementers write specific instructions for
                their service desks to follow for specific errors.
            ******************************************************** */

            model.put( "errorMessage", ex.getMessage() );
            ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, req, res);
            log.error( "Unanticipated Error", ex);
        }

    }

}
