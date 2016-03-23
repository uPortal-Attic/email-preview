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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.MailPreferences;
import org.jasig.portlet.emailpreview.mvc.Attribute;
import org.jasig.portlet.emailpreview.mvc.MailStoreConfigurationForm;
import org.jasig.portlet.emailpreview.security.IStringEncryptionService;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;
import org.jasig.portlet.emailpreview.service.IServiceBroker;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 */
@Controller
@RequestMapping("CONFIG")
public class MailStoreConfigurationController extends BaseEmailController {
    
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired(required = true)
    private IAuthenticationServiceRegistry authServiceRegistry;
    @Autowired(required = true)
    private ILinkServiceRegistry linkServiceRegistry;
    @Autowired(required = true)
    private IStringEncryptionService encryptionService;
    
    public void setLinkServiceRegistry(ILinkServiceRegistry linkServiceRegistry) {
        this.linkServiceRegistry = linkServiceRegistry;
    }

    public void setAuthServiceRegistry(IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
    }

    public void setEncryptionService(IStringEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @RequestMapping
    public String getAccountFormView() {
        return "config";
    }
    
    @RequestMapping(params = "action=updateConfiguration")
    public void saveAccountConfiguration(ActionRequest request,
            ActionResponse response,
            @ModelAttribute("form") MailStoreConfigurationForm form,
            @RequestParam(value="save", required=false) String save)
            throws PortletModeException {
        
        if (StringUtils.isNotBlank(save)) {
            MailStoreConfiguration config = new MailStoreConfiguration();
            config.setHost(form.getHost());
            config.setPort(form.getPort());
            config.setProtocol(form.getProtocol());
            config.setInboxFolderName(form.getInboxFolderName());

            // For Exchange, the inbox is called 'Inbox' not 'INBOX'. Change it so the folder drop-down on the
            // preview and summary pages shows the correct inbox folder.
            if (IServiceBroker.EXCHANGE_WEB_SERVICES.equals(config.getProtocol())
                    && "INBOX".equals(config.getInboxFolderName())) {
                config.setInboxFolderName("Inbox");
            }
            List<String> allowableAuthKeys = form.getAllowableAuthenticationServiceKeys();
            // A bit of a work-around:  default the serviceKey in 
            // use to the first allowable one.  Users must select 
            // another service if desired, and to do so the preference 
            // must be marked "user editable."
            String authService = allowableAuthKeys.size() != 0
                                        ? allowableAuthKeys.get(0)
                                        : null;
            config.setAuthenticationServiceKey(authService);
            config.setAllowableAuthenticationServiceKeys(allowableAuthKeys);
            config.setUsernameSuffix(form.getUsernameSuffix());
            config.setLinkServiceKey(form.getLinkServiceKey());
            config.setConnectionTimeout(form.getConnectionTimeout());
            config.setTimeout(form.getTimeout());
            config.setExchangeAutodiscover(form.getExchangeAutodiscover());
            config.setEwsUseMailAttribute(form.getEwsUseMailAttribute());
            config.setDisplayMailAttribute(form.getDisplayMailAttribute());

            String allowContent = request.getParameter(MailPreferences.ALLOW_RENDERING_EMAIL_CONTENT.getKey());
            if (StringUtils.isNotEmpty(allowContent)) {
                config.setAllowRenderingEmailContent(Boolean.valueOf(allowContent));
            } else {
                config.setAllowRenderingEmailContent(false);
            }
            
            config.setMarkMessagesAsRead(form.getMarkMessagesAsRead());
            
            for (Map.Entry<String, Attribute> entry : form.getJavaMailProperties().entrySet()) {
                config.getJavaMailProperties().put(entry.getKey(), entry.getValue().getValue());
            }
            
            for (Map.Entry<String, Attribute> entry : form.getAdditionalProperties().entrySet()) {
                config.getAdditionalProperties().put(entry.getKey(), entry.getValue().getValue());
            }
            
            log.debug("Saving new mail store configuration: {" + config.toString() + "}");
            serviceBroker.saveConfiguration(request, config);
        }
        
        response.setPortletMode(PortletMode.VIEW);
    }

    @ModelAttribute("usingDefaultEncryptionKey")
    public Boolean isUsingDefaultEncryptionKey() {
        return encryptionService.usingDefaultEncryptionKey();
    }

    @ModelAttribute("form")
    public MailStoreConfigurationForm getConfigurationForm(PortletRequest req) {
        final MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        return MailStoreConfigurationForm.create(config, req);
    }
    
    @ModelAttribute("authServices")
    public List<IAuthenticationService> getAvailableAuthServices() {
        return new ArrayList<IAuthenticationService>(authServiceRegistry.getServices());
    }

    @ModelAttribute("serviceParameters")
    public Map<String, List<ConfigurationParameter>> getServiceParameters(PortletRequest request) {
        Map<String, List<ConfigurationParameter>> parameters = new HashMap<String, List<ConfigurationParameter>>();

        MailStoreConfiguration config = serviceBroker.getConfiguration(request);

        IEmailLinkService linkService = linkServiceRegistry.getEmailLinkService(config.getLinkServiceKey());
        if (linkService != null) {
            parameters.put("linkParameters", linkService.getAdminConfigurationParameters());
        }

        return parameters;
    }
    
    @ModelAttribute("protocols")
    public Set<String> getProtocols() {
        return serviceBroker.getSupportedProtocols();
    }
    
    @ModelAttribute("linkServices")
    public Collection<IEmailLinkService> getLinkServices() {
        return this.linkServiceRegistry.getServices();
    }
    
    @ModelAttribute("authServices")
    public Collection<IAuthenticationService> getAuthServices() {
        return this.authServiceRegistry.getServices();
    }

    @ResourceMapping(value = "parameters")
    public ModelAndView getParameters(ResourceRequest req, ResourceResponse res,
            @RequestParam("linkService") String linkServiceKey,
            @RequestParam("authService") String authServiceKey) throws IOException {

        Map<String, Object> model = new HashMap<String, Object>();

        try {

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


        } catch (Exception ex) {
            log.error("Error encountered attempting to retrieve parameter definitions", ex);
            res.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
            model.put("error", "Error encountered attempting to retrieve parameter definitions");
        }

        return new ModelAndView("json", model);

    }

}
