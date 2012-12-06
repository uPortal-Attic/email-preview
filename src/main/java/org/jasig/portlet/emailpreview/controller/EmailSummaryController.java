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

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.IServiceBroker;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.jasig.portlet.emailpreview.util.EmailAccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

/**
 *
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 */
@Controller
@RequestMapping("VIEW")
public class EmailSummaryController {

    public final static String WELCOME_TITLE_PREFERENCE = "welcomeTitle";
    public final static String WELCOME_INSTRUCTIONS_PREFERENCE = "welcomeInstructions";
    public final static String DEFAULT_VIEW_PREFERENCE = "defaultView";
    public final static String PAGE_SIZE_PREFERENCE = "pageSize";
    public final static String ALLOW_DELETE_PREFERENCE = "allowDelete";

    public final static String SUPPORTS_TOGGLE_SEEN_KEY = "supportsToggleSeen";
    private final static String SHOW_CONFIG_LINK_KEY = "showConfigLink";

    private final static String DEFAULT_WELCOME_TITLE = "Welcome to Email Preview";
    private final static String DEFAULT_WELCOME_INSTRUCTIONS = "";
    private final static String MSG_CONTAINER = "messagesInfoContainer";

    private final Log log = LogFactory.getLog(this.getClass());
    private String adminRoleName = "admin";

    @Autowired(required = true)
    private IServiceBroker serviceBroker;

    @Autowired(required = true)
    private IAuthenticationServiceRegistry authServiceRegistry;

    @Autowired(required = true)
    private ILinkServiceRegistry linkServiceRegistry;

    @Resource
    private Map<String,String> jsErrorMessages;

	@Resource
	protected ViewSelectorDefault viewSelector;
    /**
     * Three possible views for this controller.
     */
    public enum View {

        /**
         * Indicates the portlet is not yet (completely) configured
         */
        WELCOME("welcome") {
            @Override
            public ModelAndView show(RenderRequest req, RenderResponse res, EmailSummaryController controller) {
                PortletPreferences prefs = req.getPreferences();
                Map<String,Object> model = new HashMap<String,Object>();
                model.put(WELCOME_TITLE_PREFERENCE, prefs.getValue(WELCOME_TITLE_PREFERENCE, DEFAULT_WELCOME_TITLE));
                model.put(WELCOME_INSTRUCTIONS_PREFERENCE, prefs.getValue(WELCOME_INSTRUCTIONS_PREFERENCE, DEFAULT_WELCOME_INSTRUCTIONS));
                return new ModelAndView(getKey(), model);
            }
        },

        /**
         * Indicates the portlet is not yet (completely) configured
         */
        ROLLUP("rollup") {
            
            @Override
            public ModelAndView show(RenderRequest req, RenderResponse res, EmailSummaryController controller) {
                Map<String,Object> model = new HashMap<String,Object>();

                MailStoreConfiguration config = controller.serviceBroker.getConfiguration(req);
                IAuthenticationService authService = controller.authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());

                String emailAddress = EmailAccountUtils.determineUserEmailAddress(req, config, authService);
                model.put("emailAddress", emailAddress);

                return new ModelAndView(getKey(), model);
            }
        },

        /**
         * Tabular view of the INBOX with lots of features
         */
        PREVIEW("preview") {
            @Override
            public ModelAndView show(RenderRequest req, RenderResponse res, EmailSummaryController controller) {
                Map<String,Object> model = new HashMap<String,Object>();

                PortletPreferences prefs = req.getPreferences();

                // PageSize:  this value can be set by administrators as a publish-time
                // portlet preference, and (normally) overridden by users as a
                // user-defined portlet preference.
                int pageSize = Integer.parseInt(prefs.getValue(PAGE_SIZE_PREFERENCE, "10"));
                model.put(PAGE_SIZE_PREFERENCE, pageSize);

                // Check to see if the portlet is configured to display a link
                // to config mode and if it applies to this user
                boolean showConfigLink = Boolean.valueOf(prefs.getValue(
                                    SHOW_CONFIG_LINK_KEY, "false"));
                if (showConfigLink) {
                    showConfigLink = req.isUserInRole(controller.adminRoleName);
                }
                model.put("showConfigLink", showConfigLink);

                // Also see if the portlet is configured
                // to permit users to delete messages
                boolean allowDelete = Boolean.valueOf(prefs.getValue(
                                    ALLOW_DELETE_PREFERENCE, "false"));
                model.put("allowDelete", allowDelete);

                MailStoreConfiguration config = controller.serviceBroker.getConfiguration(req);
                model.put("markMessagesAsRead", config.getMarkMessagesAsRead());

                // Check if this mail server supports setting the READ/UNREAD flag
                model.put(SUPPORTS_TOGGLE_SEEN_KEY, config.supportsToggleSeen());
                //Display message in a table
                model.put(MSG_CONTAINER,"table");

                return new ModelAndView(getKey(), model);
            }
        },
        
        /**
         * Mobile view of the INBOX with lots of features
         */
        MOBILEPREVIEW("mobilePreview") {
            @Override
            public ModelAndView show(RenderRequest req, RenderResponse res, EmailSummaryController controller) {
                Map<String,Object> model = new HashMap<String,Object>();

                PortletPreferences prefs = req.getPreferences();

                // PageSize:  this value can be set by administrators as a publish-time
                // portlet preference, and (normally) overridden by users as a
                // user-defined portlet preference.
                int pageSize = Integer.parseInt(prefs.getValue(PAGE_SIZE_PREFERENCE, "10"));
                model.put(PAGE_SIZE_PREFERENCE, pageSize);

                // Check to see if the portlet is configured to display a link
                // to config mode and if it applies to this user
                boolean showConfigLink = Boolean.valueOf(prefs.getValue(
                                    SHOW_CONFIG_LINK_KEY, "false"));
                if (showConfigLink) {
                    showConfigLink = req.isUserInRole(controller.adminRoleName);
                }
                model.put("showConfigLink", showConfigLink);

                // Also see if the portlet is configured
                // to permit users to delete messages
                boolean allowDelete = Boolean.valueOf(prefs.getValue(
                                    ALLOW_DELETE_PREFERENCE, "false"));
                model.put("allowDelete", allowDelete);

                MailStoreConfiguration config = controller.serviceBroker.getConfiguration(req);
                model.put("markMessagesAsRead", config.getMarkMessagesAsRead());

                // Check if this mail server supports setting the READ/UNREAD flag
                model.put(SUPPORTS_TOGGLE_SEEN_KEY, config.supportsToggleSeen());
                //Display message in a div 
                model.put(MSG_CONTAINER,"div.message_infos");

                return new ModelAndView(getKey(), model);
            }
        };        

        private final String key;

        public static View getInstance(String key) {
            for (View v : View.values()) {
                if (v.getKey().equals(key)) {
                    return v;
                }
            }
            throw new RuntimeException("Unrecognized view:  " + key);
        }

        private View(String key) { this.key = key; }

        public String getKey() { return key; }

        public abstract ModelAndView show(RenderRequest req, RenderResponse res, EmailSummaryController controller);

    }

    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }

    /*
     * Action Phase
     */

    @RequestMapping(params="action=showRollup")
    public void switchToRollup(ActionRequest req, ActionResponse res) {
        PortletPreferences prefs = req.getPreferences();
        try {
            prefs.setValue(EmailSummaryController.DEFAULT_VIEW_PREFERENCE, View.ROLLUP.getKey());
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
            prefs.setValue(EmailSummaryController.DEFAULT_VIEW_PREFERENCE, View.PREVIEW.getKey());
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
    public ModelAndView chooseView(RenderRequest req, RenderResponse res) throws Exception {

        View showView = null;

        MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
        if (!authService.isConfigured(req, config)) {
            // Rule #1:  If we're not configured for authentication,
            // show the 'welcome' view so the user knows what to do
            showView = View.WELCOME;
        } else if (req.getWindowState().equals(WindowState.MAXIMIZED)) {
            // Rule #2:  We don't show the rollup in MAXIMIZED state
            showView = viewSelector.getEmailPreviewViewName(req);
        } else {
            // Rule #3:  Use the defaultView preference;  this setting gets updated
            // every time the user changes from one to the other (it's sticky)
            PortletPreferences prefs = req.getPreferences();
            String viewName = prefs.getValue(DEFAULT_VIEW_PREFERENCE, View.ROLLUP.getKey());
            showView = View.getInstance(viewName);
        }

        // Now render the choice...
        ModelAndView rslt = showView.show(req, res, this);

        // Add common model stuff...
        rslt.getModel().put("jsErrorMessages", jsErrorMessages);
        rslt.getModel().put("supportsEdit", req.isPortletModeAllowed(PortletMode.EDIT));
        rslt.getModel().put("supportsHelp", req.isPortletModeAllowed(PortletMode.HELP));

        return rslt;

    }
    
    /*
     * Other stuff
     */
    
    @ModelAttribute("inboxUrl")
    public String getInboxUrl(PortletRequest req) {
        
        String rslt = null;  // default
        
        final MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        final IEmailLinkService linkService = linkServiceRegistry.getEmailLinkService(config.getLinkServiceKey());
        if (linkService != null) {
            rslt = linkService.getInboxUrl(config);
        }
        
        return rslt;

    }

}
