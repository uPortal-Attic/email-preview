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

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

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
    private final static String LAST_VIEW_SESSION_ATTRIBUTE =
            "org.jasig.portlet.emailpreview.controller.EmailSummaryController.LAST_VIEW_SESSION_ATTRIBUTE"; 
    
    private final Pattern domainPattern = Pattern.compile("\\.([a-zA-Z0-9]+\\.[a-zA-Z0-9]+)\\z");

    private String adminRoleName = "admin";

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

    @RequestMapping
    public ModelAndView chooseView(RenderRequest request, RenderResponse response) throws Exception {

        ModelAndView rslt = null;
        
        // Once the user clicks a different view, that choice should 
        // be sticky until either (1) he makes a different choice, or 
        // (2) the session expires.
        String showView = (String) request.getPortletSession().getAttribute(LAST_VIEW_SESSION_ATTRIBUTE);
        if (showView == null) {
            // The user has not rendered a view yet, choose one based on settings
            PortletPreferences prefs = request.getPreferences();
            showView = prefs.getValue(DEFAULT_VIEW_PREFERENCE, VIEW_ROLLUP);
        }
        
        // Now render the choice...
        if (VIEW_PREVIEW.equals(showView)) {
            rslt = showPreview(request, response);
        } else {
            rslt = showRollup(request, response);
        }
        
        return rslt;

    }

    @RequestMapping(params="action=showRollup")
    public ModelAndView showRollup(RenderRequest request, RenderResponse response) throws Exception {
        
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

        // Lastly check whether EDIT is supported
        boolean supportsEdit = request.isPortletModeAllowed(PortletMode.EDIT);
        model.put("supportsEdit", supportsEdit);

        // Make this choice "sticky" 
        request.getPortletSession().setAttribute(LAST_VIEW_SESSION_ATTRIBUTE, VIEW_ROLLUP);
        
        return new ModelAndView("rollup", model);

    }

    @RequestMapping(params="action=showPreview")
    public ModelAndView showPreview(RenderRequest request, RenderResponse response) throws Exception {

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

        // Lastly check whether EDIT is supported
        boolean supportsEdit = request.isPortletModeAllowed(PortletMode.EDIT);
        model.put("supportsEdit", supportsEdit);

        // Make this choice "sticky" 
        request.getPortletSession().setAttribute(LAST_VIEW_SESSION_ATTRIBUTE, VIEW_PREVIEW);

        return new ModelAndView("preview", model);

    }

}
