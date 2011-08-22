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
import javax.portlet.PortletPreferences;

import javax.portlet.RenderRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

@Controller
@RequestMapping("HELP")
public class EmailHelpController {    

	public final static String HELP_TITLE_PREFERENCE = "helpTitle";
	public final static String DEFAULT_HELP_TITLE = "Welcome to Help";
	public final static String HELP_INSTRUCTIONS_PREFERENCE = "helpInstructions";
	public final static String DEFAULT_HELP_INSTRUCTIONS = "";
	
    @RequestMapping
    public ModelAndView getAccountFormView(RenderRequest req) {
    	PortletPreferences prefs = req.getPreferences();
    	Map<String,Object> model = new HashMap<String,Object>();
        model.put(HELP_TITLE_PREFERENCE, prefs.getValue(HELP_TITLE_PREFERENCE, DEFAULT_HELP_TITLE));
        model.put(HELP_INSTRUCTIONS_PREFERENCE, prefs.getValue(HELP_INSTRUCTIONS_PREFERENCE, DEFAULT_HELP_INSTRUCTIONS));
        return new ModelAndView("help",model);
    }
}
