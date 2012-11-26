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

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.dao.IEmailAccountService;
import org.jasig.portlet.emailpreview.exception.MailAuthenticationException;
import org.jasig.portlet.emailpreview.exception.MailTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

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

    public static final String FORCE_REFRESH_PARAMETER = "forceRefresh";

    public static final String KEY_ACCOUNT_SUMMARY = "accountSummary";
    public static final String KEY_INBOX_URL = "inboxUrl";
    public static final String KEY_ERROR = "error";
    public static final String KEY_USER_QUOTA = "userQuota";
    public static final String KEY_SPACE_USED = "spaceUsed";  

    @ResourceMapping(value = "accountSummary")
    public ModelAndView getAccountSummary(ResourceRequest req, ResourceResponse res,
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
                model.put(KEY_ACCOUNT_SUMMARY, accountSummary);
                model.put(KEY_INBOX_URL, accountSummary.getInboxUrl());
                if(accountSummary.getQuota().get("spaceUsed")==null)
                	model.put(KEY_SPACE_USED,"-1");
                else{
                model.put(KEY_SPACE_USED, accountSummary.getQuota().get("spaceUsed"));
                model.put(KEY_USER_QUOTA, accountSummary.getQuota().get("userQuota"));    
                }
            } else {
                Throwable cause = accountSummary.getErrorCause();
                if (MailAuthenticationException.class.isAssignableFrom(cause.getClass())) {
                    log.info("Authentication Failure (username='" + username + "') : " + cause.getMessage());
                    res.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(HttpServletResponse.SC_UNAUTHORIZED));
                    model.put(KEY_ERROR, "Not authorized");
                } else {
                    log.error("Unanticipated Error", cause);
                    res.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
                    model.put(KEY_ERROR, "Unanticipated Error");
                }
            }

        } catch (MailTimeoutException ex) {
            log.error("Mail Service Timeout", ex);
            res.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(HttpServletResponse.SC_GATEWAY_TIMEOUT));
            model.put(KEY_ERROR, "Mail Service Timeout");
        } catch (Exception ex) {
            /* ********************************************************
                In the case of an unknown error we want to send the
                exception's message back to the portlet. This will
                let implementers write specific instructions for
                their service desks to follow for specific errors.
            ******************************************************** */
            log.error( "Unanticipated Error", ex);
            res.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
            model.put(KEY_ERROR, "ex.getMessage()");
        }

        return new ModelAndView("json", model);

    }

}
