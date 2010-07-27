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

import java.util.Collections;

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
 * @version $Revision$
 */
@Controller
@RequestMapping("VIEW")
public class EmailSummaryController {
    
    private final String SHOW_CONFIG_LINK_KEY = "showConfigLink";

    private String adminRoleName = "admin";
    
    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }
    
	@RequestMapping
	public ModelAndView showEmail(RenderRequest request, RenderResponse response) throws Exception {
	    
	    // first check to see if the portlet is configured to display a link
	    // to config mode
	    PortletPreferences preferences = request.getPreferences();
        boolean showConfigLink = Boolean.valueOf(preferences.getValue(
                SHOW_CONFIG_LINK_KEY, "false"));
	    
	    // if it is, check to see if the user is in the administrative role
	    if (showConfigLink) {
	        showConfigLink = request.isUserInRole(this.adminRoleName);
	    }
	    
        return new ModelAndView("preview", Collections.singletonMap(
                "showConfigLink", showConfigLink));
	}

}
