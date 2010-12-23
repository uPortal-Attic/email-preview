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

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

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

    public final static String PAGE_SIZE_KEY = "pageSize";
    public final static String ALLOW_DELETE_KEY = "allowDelete";
    private final static String SHOW_CONFIG_LINK_KEY = "showConfigLink";

    private String adminRoleName = "admin";

    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }

    @RequestMapping
    public ModelAndView showEmail(RenderRequest request, RenderResponse response) throws Exception {

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

        return new ModelAndView("preview", model);
    }

}
