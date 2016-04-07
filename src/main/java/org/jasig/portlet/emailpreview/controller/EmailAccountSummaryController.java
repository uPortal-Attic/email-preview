/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.emailpreview.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.exception.MailAuthenticationException;
import org.jasig.portlet.emailpreview.exception.MailTimeoutException;
import org.jasig.portlet.notice.NotificationCategory;
import org.jasig.portlet.notice.NotificationEntry;
import org.jasig.portlet.notice.NotificationQuery;
import org.jasig.portlet.notice.NotificationResponse;
import org.jasig.portlet.notice.NotificationResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.EventMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 */
@Controller
@RequestMapping("VIEW")
public class EmailAccountSummaryController extends BaseEmailController {

    protected final Log log = LogFactory.getLog(getClass());

    public static final String FORCE_REFRESH_PARAMETER = "forceRefresh";

    public static final String KEY_ACCOUNT_SUMMARY = "accountSummary";
    public static final String KEY_INBOX_URL = "inboxUrl";
    public static final String KEY_ERROR = "error";
    public static final String KEY_EMAIL_QUOTA_LIMIT = "emailQuotaLimit";
    public static final String KEY_EMAIL_QUOTA_USAGE = "emailQuotaUsage";
    public static final String INBOX_NAME_PREFERENCE = "inboxName";
    public static final String INBOX_NAME_DEFAULT = "INBOX";
    public static final String INBOX_NAME_UNDEFINED = "undefined";

    private static final String NOTIFICATION_NAMESPACE = "https://source.jasig.org/schemas/portlet/notification";
    private static final String NOTIFICATION_QUERY_LOCAL_NAME = "NotificationQuery";
    private static final String NOTIFICATION_QUERY_QNAME_STRING = "{" + NOTIFICATION_NAMESPACE + "}" + NOTIFICATION_QUERY_LOCAL_NAME;
    private static final String NOTIFICATION_RESULT_LOCAL_NAME = "NotificationResult";
    private static final QName NOTIFICATION_RESULT_QNAME = new QName(NOTIFICATION_NAMESPACE, NOTIFICATION_RESULT_LOCAL_NAME);
    private static final String CONTEXT_PATH_PREFERENCE = "portal.context.path";
    private static final String NOTIFICATION_REDIRECT_FNAME = "portlet.fname";
    private static final String NOTIFICATION_CATEGORY_NAME = "notification.category.title";
    private static final String NOTIFICATION_ENTRY_NAME = "notification.entry.title";
    private static final String NOTIFICATION_UNREAD_BODY = "notification.entry.body.unread";
    private static final String NOTIFICATION_ENTRY_SOURCE = "notification.entry.source";

    @ResourceMapping(value = "accountSummary")
    public ModelAndView getAccountSummary(ResourceRequest req, ResourceResponse res,
            @RequestParam("pageStart") int start,
            @RequestParam("numberOfMessages") int max, @RequestParam(value ="inboxFolder", required = false) String folder) throws IOException {

        // Define view and generate model
        Map<String, Object> model = new HashMap<String, Object>();

        String username = req.getRemoteUser();
        try {

            // Force a re-load from the data source if called for by the UI.
            boolean refresh = Boolean.valueOf(req.getParameter(FORCE_REFRESH_PARAMETER));

            // Or because of a change in settings.
            if (req.getPortletSession().getAttribute(FORCE_REFRESH_PARAMETER) != null) {
                // Doesn't matter what the value is;  this calls for a refresh...
                refresh = true;
                req.getPortletSession().removeAttribute(FORCE_REFRESH_PARAMETER);
            }
            
            PortletPreferences prefs = req.getPreferences();
            
            if ((folder==null)||(folder.equals(INBOX_NAME_UNDEFINED))) {
            	folder= prefs.getValue(EmailAccountSummaryController.INBOX_NAME_PREFERENCE, INBOX_NAME_DEFAULT);
            }
             
            String prefFolder= prefs.getValue(EmailAccountSummaryController.INBOX_NAME_PREFERENCE, INBOX_NAME_DEFAULT);
            if ((!prefs.isReadOnly(INBOX_NAME_PREFERENCE))&&(!folder.equals(prefFolder))) {
            	prefs.setValue(INBOX_NAME_PREFERENCE, folder);
	            prefs.store();
            }
            
            // Get current user's account information
            AccountSummary accountSummary = getEmailAccountService(req).getAccountSummary(req, start, max, refresh, folder);

            // Check for AuthN failure...
            if (accountSummary.isValid()) {
                model.put(KEY_ACCOUNT_SUMMARY, accountSummary);
                model.put(KEY_INBOX_URL, accountSummary.getInboxUrl());
                if(accountSummary.getQuota() == null) {
                	model.put(KEY_EMAIL_QUOTA_USAGE,"-1");
                } else {
                    model.put(KEY_EMAIL_QUOTA_USAGE, accountSummary.getQuota().getUsageAsPercentage());
                    model.put(KEY_EMAIL_QUOTA_LIMIT, accountSummary.getQuota().getLimitAsString());
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

    @EventMapping(NOTIFICATION_QUERY_QNAME_STRING)
    public void syndicateNotifications(final EventRequest req, final EventResponse res) {

        final NotificationQuery query = (NotificationQuery) req.getEvent().getValue();
        log.info("***** Syndicating announcements for Notification portlet with windowId=" + query.getQueryWindowId());

        long count = getUnreadEmail(req);

        if (count == 0) {
            log.info("No unread messages for " + req.getRemoteUser());
            return;
        }

        PortletPreferences prefs = req.getPreferences();

        // Single category for unread email
        final List<NotificationCategory> categories = new ArrayList<>();
        final NotificationCategory category = new NotificationCategory();
        category.setTitle(prefs.getValue(NOTIFICATION_CATEGORY_NAME, "Email"));
        categories.add(category);
        final List<NotificationEntry> entries = new ArrayList<>();

        // unread email entries
        final NotificationEntry entry = new NotificationEntry();
        entry.setTitle(prefs.getValue(NOTIFICATION_ENTRY_NAME, "Unread Email"));
        String body = prefs.getValue(NOTIFICATION_UNREAD_BODY, "You have {} unread email in your preview inbox.");
        entry.setBody(body.replace("{}", Long.toString(count)));
        entry.setSource(prefs.getValue(NOTIFICATION_ENTRY_SOURCE, "Email Preview"));
        entry.setUrl(generateUrl(req));
        entries.add(entry);
        category.setEntries(entries);

        // build response/result
        final NotificationResponse response = new NotificationResponse();
        response.setCategories(categories);
        final NotificationResult result = new NotificationResult();
        result.setQueryWindowId(query.getQueryWindowId());
        result.setResultWindowId(req.getWindowID());
        result.setNotificationResponse(response);

        res.setEvent(NOTIFICATION_RESULT_QNAME, result);
    }

    private String generateUrl(PortletRequest req) {
        PortletPreferences prefs = req.getPreferences();
        String contextPath = prefs.getValue(CONTEXT_PATH_PREFERENCE, "uportal");
        String fname = prefs.getValue(NOTIFICATION_REDIRECT_FNAME, "email-preview");
        String subscribeId = "";
        if (req.getWindowID() != null) {
            String[] parts = req.getWindowID().split("_");
            if (parts != null && parts.length >2) {
                subscribeId = "." + parts[1];
            }
        }
        return "/" + contextPath + "/p/" + fname + subscribeId + "/max/render.uP";
    }

    private long getUnreadEmail(PortletRequest req) {
        // get Inbox folder name from preferences
        PortletPreferences prefs = req.getPreferences();
        String folder = prefs.getValue(INBOX_NAME_PREFERENCE, INBOX_NAME_DEFAULT);

        // Get current user's account information
        AccountSummary accountSummary = getEmailAccountService(req).getAccountSummary(req, 0, 1, false, folder);
        return accountSummary.isValid() ? accountSummary.getUnreadMessageCount() : 0;
    }
}
