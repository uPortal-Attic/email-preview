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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IMailStoreDao;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

/**
 *
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("VIEW")
public class EmailSummaryController {

    public final static String DEFAULT_VIEW_PREFERENCE = "defaultView";
    public final static String PAGE_SIZE_KEY = "pageSize";
    public final static String ALLOW_DELETE_KEY = "allowDelete";
    public final static String VIEW_ROLLUP = "rollup";
    public final static String VIEW_PREVIEW = "preview";

    private final static String SHOW_CONFIG_LINK_KEY = "showConfigLink";
    
    private final Pattern domainPattern = Pattern.compile("\\.([a-zA-Z0-9]+\\.[a-zA-Z0-9]+)\\z");

    private String adminRoleName = "admin";

    private final Log log = LogFactory.getLog(this.getClass());
    
    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }
    
    private IMailStoreDao mailStoreDao;

    @Autowired(required = true)
    public void setMailStoreDao(IMailStoreDao mailStoreDao) {
        this.mailStoreDao = mailStoreDao;
    }

    private IAuthenticationServiceRegistry authServiceRegistry;

    @Autowired(required = true)
    public void setAuthenticationServiceRegistry(IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
    }

    private ILinkServiceRegistry linkServiceRegistry;

    @Autowired(required = true)
    public void setLinkServiceRegistry(ILinkServiceRegistry linkServiceRegistry) {
        this.linkServiceRegistry = linkServiceRegistry;
    }
    
    @Resource
    private Map<String,String> jsErrorMessages;
    
    /*
     * Action Phase
     */

    @RequestMapping(params="action=showRollup")
    public void switchToRollup(ActionRequest req, ActionResponse res) {
        PortletPreferences prefs = req.getPreferences();
        try {
            prefs.setValue(EmailSummaryController.DEFAULT_VIEW_PREFERENCE, VIEW_ROLLUP);
            prefs.store();
        } catch (Throwable t) {
            log.error("Failed to update " + DEFAULT_VIEW_PREFERENCE + " for user " + req.getRemoteUser(), t);
            throw new RuntimeException(t);
        }
    }

    @RequestMapping(params="action=showPreview")
    public void switchToPreview(ActionRequest req, ActionResponse res) {
        PortletPreferences prefs = req.getPreferences();
        try {
            prefs.setValue(EmailSummaryController.DEFAULT_VIEW_PREFERENCE, VIEW_PREVIEW);
            prefs.store();
        } catch (Throwable t) {
            log.error("Failed to update " + DEFAULT_VIEW_PREFERENCE + " for user " + req.getRemoteUser(), t);
            throw new RuntimeException(t);
        }
    }
    
    /*
     * Render Phase
     */
    
    @RequestMapping
    @SuppressWarnings("unchecked")
    public ModelAndView chooseView(RenderRequest req, RenderResponse res) throws Exception {
        
        String showView = null;
        
        // Rule #1:  WindowState trumps other factors
        if (req.getWindowState().equals(WindowState.MAXIMIZED)) {
            showView = VIEW_PREVIEW;
        }
        
        // Rule #2:  Use the defaultView preference;  this setting gets updated 
        // every time the user changes from one to the other
        if (showView == null) {
            PortletPreferences prefs = req.getPreferences();
            showView = prefs.getValue(DEFAULT_VIEW_PREFERENCE, VIEW_ROLLUP);
        }

        // Now render the choice...
        ModelAndView rslt = null;
        if (VIEW_PREVIEW.equals(showView)) {
            rslt = showPreview(req, res);
        } else {
            rslt = showRollup(req, res);
        }
        
        // Add common model stuff...
        rslt.getModel().put("jsErrorMessages", jsErrorMessages);
        rslt.getModel().put("supportsEdit", req.isPortletModeAllowed(PortletMode.EDIT));
        
        return rslt;

    }

    private ModelAndView showRollup(RenderRequest request, RenderResponse response) throws Exception {
        
        Map<String,Object> model = new HashMap<String,Object>();
        
        MailStoreConfiguration config = mailStoreDao.getConfiguration(request);
        IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());

        // Make an intelligent guess about the emailAddress
        String emailAddress = null;
        String mailAccount = authService.getMailAccountName(request, config);
        String nameSuffix = config.getUsernameSuffix();
        String serverName = config.getHost();
        if (mailAccount.contains("@")) {
            emailAddress = mailAccount;
        } else if (nameSuffix != null && nameSuffix.length() != 0) {
            emailAddress = mailAccount + nameSuffix;
        } else {
            emailAddress = mailAccount;
            Matcher m = domainPattern.matcher(serverName);
            if (m.find()) {
                emailAddress = emailAddress + "@" + m.group(1);
            }
        }
        model.put("emailAddress", emailAddress);
        
        IEmailLinkService linkService = linkServiceRegistry.getEmailLinkService(config.getLinkServiceKey());
        if (linkService != null) {
            String inboxUrl = linkService.getInboxUrl(request, config);
            model.put("inboxUrl", inboxUrl);
        }

        return new ModelAndView("rollup", model);

    }

    private ModelAndView showPreview(RenderRequest request, RenderResponse response) throws Exception {

        Map<String,Object> model = new HashMap<String,Object>();

        PortletPreferences prefs = request.getPreferences();

        // PageSize:  this value can be set by administrators as a publish-time 
        // portlet preference, and (normally) overridden by users as a 
        // user-defined portlet preference.
        int pageSize = Integer.parseInt(prefs.getValue(PAGE_SIZE_KEY, "10"));
        model.put(PAGE_SIZE_KEY, pageSize);
        
        // Check to see if the portlet is configured to display a link
        // to config mode and if it applies to this user
        boolean showConfigLink = Boolean.valueOf(prefs.getValue(
                            SHOW_CONFIG_LINK_KEY, "false"));
        if (showConfigLink) {
            showConfigLink = request.isUserInRole(this.adminRoleName);
        }
        model.put("showConfigLink", showConfigLink);

        // Also see if the portlet is configured
        // to permit users to delete messages
        boolean allowDelete = Boolean.valueOf(prefs.getValue(
                            ALLOW_DELETE_KEY, "false"));
        model.put("allowDelete", allowDelete);
        
        MailStoreConfiguration config = mailStoreDao.getConfiguration(request);
        model.put("markMessagesAsRead", config.getMarkMessagesAsRead());

        return new ModelAndView("preview", model);

    }

}
