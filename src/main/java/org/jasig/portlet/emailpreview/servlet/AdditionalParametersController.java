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

package org.jasig.portlet.emailpreview.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("/parameters")
public class AdditionalParametersController {

    protected final Log log = LogFactory.getLog(getClass());

    private ILinkServiceRegistry linkServiceRegistry;

    @Autowired(required = true)
    public void setLinkServiceRegistry(ILinkServiceRegistry linkServiceRegistry) {
        this.linkServiceRegistry = linkServiceRegistry;
    }

    private IAuthenticationServiceRegistry authServiceRegistry;

    @Autowired(required = true)
    public void setAuthenticationServiceRegistry(
            IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
    }

    /**
     * Get a representation of the administrative parameters for a mail store.
     * 
     * @param request
     * @param response
     * @param linkServiceKey
     * @param authServiceKey
     * @throws IOException 
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getParameters(HttpServletRequest request, 
            HttpServletResponse response,
            @RequestParam("linkService") String linkServiceKey,
            @RequestParam("authService") String authServiceKey) throws IOException {

        try {

            Map<String, Object> model = new HashMap<String, Object>();

            // get administrative configuration parameters for the configured
            // authentication service
            final IAuthenticationService authService = authServiceRegistry
                    .getAuthenticationService(authServiceKey);
            if (authService != null) {
                final List<ConfigurationParameter> authParams = authService
                        .getAdminConfigurationParameters();
                model.put("authParams", authParams);
            }

            // get administrative configuration parameters for the configured
            // link service
            final IEmailLinkService linkService = linkServiceRegistry
                    .getEmailLinkService(linkServiceKey);
            if (linkService != null) {
                final List<ConfigurationParameter> linkParams = linkService
                        .getAdminConfigurationParameters();
                model.put("linkParams", linkParams);
            }

            return new ModelAndView("jsonView", model);

        } catch (Exception ex) {
            log.error("Error encountered attempting to retrieve parameter definitions", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

}
