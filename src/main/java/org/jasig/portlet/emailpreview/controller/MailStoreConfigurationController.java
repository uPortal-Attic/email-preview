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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IMailStoreDao;
import org.jasig.portlet.emailpreview.mvc.Attribute;
import org.jasig.portlet.emailpreview.mvc.MailStoreConfigurationForm;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("CONFIG")
public class MailStoreConfigurationController {
    
    protected final Log log = LogFactory.getLog(getClass());

    private IMailStoreDao mailStoreDao;
    
    @Autowired(required = true)
    public void setAccountDao(IMailStoreDao mailStoreDao) {
        this.mailStoreDao = mailStoreDao;
    }
    
    private List<String> protocols;
    
    @Resource(name="protocols")
    @Required
    public void setProtocols(List<String> protocols) {
        this.protocols = protocols;
    }
    
    private ILinkServiceRegistry linkServiceRegistry;
    
    @Autowired(required = true)
    public void setLinkServiceRegistry(ILinkServiceRegistry linkServiceRegistry) {
        this.linkServiceRegistry = linkServiceRegistry;
    }

    private IAuthenticationServiceRegistry authServiceRegistry;
    
    @Autowired(required = true)
    public void setAuthServiceRegistry(IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
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
            config.setLinkServiceKey(form.getLinkServiceKey());
            config.setConnectionTimeout(form.getConnectionTimeout());
            config.setTimeout(form.getTimeout());
            
            for (Map.Entry<String, Attribute> entry : form.getJavaMailProperties().entrySet()) {
                config.getJavaMailProperties().put(entry.getKey(), entry.getValue().getValue());
            }
            
            for (Map.Entry<String, Attribute> entry : form.getAdditionalProperties().entrySet()) {
                config.getAdditionalProperties().put(entry.getKey(), entry.getValue().getValue());
            }
            
            log.debug("Saving new mail store configuration: {" + config.toString() + "}");
            mailStoreDao.saveConfiguration(request, config);
        }
        
        response.setPortletMode(PortletMode.VIEW);
    }
    
    @ModelAttribute("form")
    public MailStoreConfigurationForm getConfigurationForm(PortletRequest req) {
        return MailStoreConfigurationForm.create(mailStoreDao, req);
    }
    
    @ModelAttribute("authServices")
    public List<IAuthenticationService> getAvailableAuthServices() {
        return new ArrayList<IAuthenticationService>(authServiceRegistry.getServices());
    }

    @ModelAttribute("serviceParameters")
    public Map<String, List<ConfigurationParameter>> getServiceParameters(PortletRequest request) {
        Map<String, List<ConfigurationParameter>> parameters = new HashMap<String, List<ConfigurationParameter>>();

        MailStoreConfiguration config = mailStoreDao.getConfiguration(request);

        IEmailLinkService linkService = linkServiceRegistry.getEmailLinkService(config.getLinkServiceKey());
        if (linkService != null) {
            parameters.put("linkParameters", linkService.getAdminConfigurationParameters());
        }

        return parameters;
    }
    
    @ModelAttribute("protocols")
    public List<String> getProtocols() {
        return this.protocols;
    }
    
    @ModelAttribute("linkServices")
    public Collection<IEmailLinkService> getLinkServices() {
        return this.linkServiceRegistry.getServices();
    }
    
    @ModelAttribute("authServices")
    public Collection<IAuthenticationService> getAuthServices() {
        return this.authServiceRegistry.getServices();
    }

}
