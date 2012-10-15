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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IEmailAccountService;
import org.jasig.portlet.emailpreview.dao.MailPreferences;
import org.jasig.portlet.emailpreview.mvc.Attribute;
import org.jasig.portlet.emailpreview.mvc.MailStoreConfigurationForm;
import org.jasig.portlet.emailpreview.service.IServiceBroker;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.jasig.portlet.emailpreview.service.auth.pp.PortletPreferencesCredentialsAuthenticationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

@Controller
@RequestMapping("EDIT")
public final class EditPreferencesController {
    
    private static final String UNCHANGED_PASSWORD = "uNch@ng3d.pswd!";
    private static final String CONFIG_FORM_KEY = "org.jasig.portlet.emailpreview.controller.CONFIG_FORM_KEY";
    private static final String FOCUS_ON_PREVIEW_PREFERENCE = "focusOnPreview";
    private static final String DEFAULT_FOCUS_ON_PREVIEW = "true";

    private IServiceBroker serviceBroker;
    IEmailAccountService emailAccountDao;
    private IAuthenticationServiceRegistry authServiceRegistry;
    private List<String> protocols;
    private final Log log = LogFactory.getLog(getClass());

    @RequestMapping
    public ModelAndView getAccountFormView(RenderRequest req) {

        Map<String,Object> model = new HashMap<String,Object>();
        MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        
        // form
        PortletSession session = req.getPortletSession(false);
        MailStoreConfigurationForm form = (MailStoreConfigurationForm) session.getAttribute(CONFIG_FORM_KEY);
        if (form == null) {
            form = MailStoreConfigurationForm.create(serviceBroker, req);
        } else {
            session.removeAttribute(CONFIG_FORM_KEY);
        }
        model.put("form", form);
        
        // Disable some config elements?
        model.put("disableProtocol", config.isReadOnly(req, MailPreferences.PROTOCOL));
        model.put("disableHost", config.isReadOnly(req, MailPreferences.HOST));
        model.put("disablePort", config.isReadOnly(req, MailPreferences.PORT));
        model.put("disableAuthService", config.isReadOnly(req, MailPreferences.AUTHENTICATION_SERVICE_KEY));
        
        // Available protocols
        model.put("protocols", protocols);

        // AuthN info
        Map<String,IAuthenticationService> authServices = new HashMap<String,IAuthenticationService>();
        for (String key : config.getAllowableAuthenticationServiceKeys()) {
            IAuthenticationService auth = authServiceRegistry.getAuthenticationService(key);
            if (auth != null) {
                authServices.put(key, auth);
            } else {
                // Unknown authN service;  bad data
                if (log.isWarnEnabled()) {
                    log.warn("Portlet specified an allowable Authentication " +
                            "Service that is unknown to the registry:  '" + 
                            key + "'");
                }
            }
        }
        model.put("authenticationServices", authServices);
        if (form.getAdditionalProperties().containsKey(MailPreferences.PASSWORD.getKey())) {
            model.put("unchangedPassword", UNCHANGED_PASSWORD);
        }
        
        // Pass the errorMessage, if present
        if (req.getParameter("errorMessage") != null) {
            model.put("errorMessage", req.getParameter("errorMessage"));
        }
        
        return new ModelAndView("editPreferences", model);

    }

    @RequestMapping(params = "action=updatePreferences")
    public void updatePreferences(ActionRequest req, ActionResponse res) throws PortletModeException {
        
        /*
         * Preferences
         */

        PortletPreferences prefs = req.getPreferences();

        if (!prefs.isReadOnly(EmailSummaryController.DEFAULT_VIEW_PREFERENCE)) {
            String defaultViewParam = req.getParameter(EmailSummaryController.DEFAULT_VIEW_PREFERENCE);
            String currentDefaultView = prefs.getValue(EmailSummaryController.DEFAULT_VIEW_PREFERENCE, EmailSummaryController.View.ROLLUP.getKey());
            if (defaultViewParam != null && !currentDefaultView.equals(defaultViewParam)) {
                if (log.isDebugEnabled()) {
                    log.debug("Changing users default view for user '" + req.getRemoteUser() 
                                                    + "' to:  '" + defaultViewParam + "'");
                }
                try {
                    prefs.setValue(EmailSummaryController.DEFAULT_VIEW_PREFERENCE, defaultViewParam);
                    prefs.store();
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to set defalutView preference", t);
                }
            }
        }
        
        if (!prefs.isReadOnly(FOCUS_ON_PREVIEW_PREFERENCE)) {
            String focusOnPreviewParam = req.getParameter(FOCUS_ON_PREVIEW_PREFERENCE);
            String focusOnPreviewSelection = focusOnPreviewParam != null
                                                ? focusOnPreviewParam
                                                : "false";
            String currentFocusOnPreview = prefs.getValue(FOCUS_ON_PREVIEW_PREFERENCE, DEFAULT_FOCUS_ON_PREVIEW);
            if (!currentFocusOnPreview.equals(focusOnPreviewSelection)) {
                if (log.isDebugEnabled()) {
                    log.debug("Changing focusOnPreview setting for user '" + req.getRemoteUser() 
                                                    + "' to:  '" + focusOnPreviewSelection + "'");
                }
                try {
                    prefs.setValue(FOCUS_ON_PREVIEW_PREFERENCE, focusOnPreviewSelection);
                    prefs.store();
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to set focusOnPreview preference", t);
                }
            }
        }

        /*
         * Mail Config
         */
        
        MailStoreConfiguration config = serviceBroker.getConfiguration(req);
        MailStoreConfigurationForm form = MailStoreConfigurationForm.create(serviceBroker, req);
        String err = null;  // default

        if (!config.isReadOnly(req, MailPreferences.PROTOCOL)) {
            String protocol = req.getParameter(MailPreferences.PROTOCOL.getKey());
            protocol = protocol != null ? protocol.trim() : "";
            if (log.isDebugEnabled()) {
                log.debug("Receieved the following user input for Protocol:  '" + protocol + "'");
            }
            if (!protocols.contains(protocol)) {
                // User must have hacked a HttpRequest
                throw new RuntimeException("Unsupported protocol:  " + protocol);
            }
            form.setProtocol(protocol);
            if (protocol.length() == 0 && err == null) {
                err = "Server Protocol is required";
            }
        }
        
        if (!config.isReadOnly(req, MailPreferences.HOST)) {
            String host = req.getParameter(MailPreferences.HOST.getKey());
            host = host != null ? host.trim() : "";
            if (log.isDebugEnabled()) {
                log.debug("Receieved the following user input for Host:  '" + host + "'");
            }
            form.setHost(host);
            if (host.length() == 0 && err == null) {
                err = "Server Name is required";
            }
        }

        if (!config.isReadOnly(req, MailPreferences.PORT)) {
            String port = req.getParameter(MailPreferences.PORT.getKey());
            port = port != null ? port.trim() : "";
            if (log.isDebugEnabled()) {
                log.debug("Receieved the following user input for Port:  '" + port + "'");
            }
            try {
                form.setPort(Integer.parseInt(port));
            } catch (NumberFormatException nfe) {
                log.debug("The specified value is not a number:  " + port, nfe);
            }
            if (port.length() == 0 && err == null) {
                err = "Server Port is required";
            }
        }
        
        if (!config.isReadOnly(req, MailPreferences.MARK_MESSAGES_AS_READ)) {
            String markMessagesAsRead = req.getParameter(MailPreferences.MARK_MESSAGES_AS_READ.getKey());
            // No need to handle missing value because absence will (correctly) be converted to 'false' 
            if (log.isDebugEnabled()) {
                log.debug("Receieved the following user input for markMessagesAsRead:  '" + markMessagesAsRead + "'");
            }
            form.setMarkMessagesAsRead(Boolean.valueOf(markMessagesAsRead));
        }

        if (!config.isReadOnly(req, MailPreferences.AUTHENTICATION_SERVICE_KEY)) {
            String authKey = req.getParameter(MailPreferences.AUTHENTICATION_SERVICE_KEY.getKey());
            authKey = authKey != null ? authKey.trim() : "";
            if (log.isDebugEnabled()) {
                log.debug("Receieved the following user input for AuthN Service Key:  '" + authKey + "'");
            }
            if (authKey.length() != 0 && config.getAllowableAuthenticationServiceKeys().contains(authKey)) {  // authKey radio buttons may not be present
                form.setAuthenticationServiceKey(authKey);
            }
        }

        // ToDo:  Support for PortletPreferences auth is a 
        // bit hackish;  look for an opportunity to refactor 
        // toward abstractions.
        String ppPassword = null;  // default
        if (PortletPreferencesCredentialsAuthenticationServiceImpl.KEY.equals(form.getAuthenticationServiceKey())) {
            
            // Update username
            if (!config.isReadOnly(req, MailPreferences.MAIL_ACCOUNT)) {
                String mailAccount = req.getParameter(MailPreferences.MAIL_ACCOUNT.getKey());
                mailAccount = mailAccount != null ? mailAccount.trim() : "";
                if (log.isDebugEnabled()) {
                    log.debug("Receieved the following user input for mailAccount:  '" + mailAccount + "'");
                }
                if (mailAccount.length() != 0) {
                    form.getAdditionalProperties().put(MailPreferences.MAIL_ACCOUNT.getKey(), new Attribute(mailAccount));
                } else {
                    form.getAdditionalProperties().remove(MailPreferences.MAIL_ACCOUNT.getKey());
                }
            }

            // Update password, if entered
            if (!config.isReadOnly(req, MailPreferences.PASSWORD)) {
                String password = req.getParameter("ppauth_password");
                password = password != null ? password.trim() : "";
                if (log.isDebugEnabled()) {
                    log.debug("Receieved user input of the following length for Password:  " + password.length());
                }
                if (!UNCHANGED_PASSWORD.equals(password)) {
                    if (password.length() != 0) {
                        ppPassword = password;
                    } else {
                        err = "Password is required for this form of authentication";
                        form.getAdditionalProperties().remove(MailPreferences.PASSWORD.getKey());
                    }
                }
            }

        }
        
        // Proceed if there were no problems
        if (err == null) {

            // protocol/host/port
            config.setProtocol(form.getProtocol());
            config.setHost(form.getHost());
            config.setPort(form.getPort());
            config.setMarkMessagesAsRead(form.getMarkMessagesAsRead());
            config.setAuthenticationServiceKey(form.getAuthenticationServiceKey());
            
            // username/password
            if (PortletPreferencesCredentialsAuthenticationServiceImpl.KEY.equals(form.getAuthenticationServiceKey())) {
                Attribute username = form.getAdditionalProperties().get(MailPreferences.MAIL_ACCOUNT.getKey());
                config.getAdditionalProperties().put(MailPreferences.MAIL_ACCOUNT.getKey(), username.getValue());
                // NB:  we only accept password if username was also specified
                if (ppPassword != null) {
                    config.getAdditionalProperties().put(MailPreferences.PASSWORD.getKey(), ppPassword);
                }
            } else {
                // Make sure username/password are clear
                config.getAdditionalProperties().remove(MailPreferences.MAIL_ACCOUNT.getKey());
                config.getAdditionalProperties().remove(MailPreferences.PASSWORD.getKey());
            }
            
            serviceBroker.saveConfiguration(req, config);
            req.getPortletSession().setAttribute(EmailAccountSummaryController.FORCE_REFRESH_PARAMETER, Boolean .TRUE);
            res.setPortletMode(PortletMode.VIEW);
        } else {
            res.setRenderParameter("errorMessage", err);
            req.getPortletSession().setAttribute(CONFIG_FORM_KEY, form);
        }
        
    }

    @Autowired(required = true)
    public void setAuthenticationServiceRegistry(IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
    }
    
    @Autowired(required = true)
    public void setEmailAccountDao(IEmailAccountService emailAccountDao) {
        this.emailAccountDao = emailAccountDao;
    }

    @Autowired(required = true)
    public void setServiceBroker(IServiceBroker serviceBroker) {
        this.serviceBroker = serviceBroker;
    }

    @Resource(name="protocols")
    @Required
    public void setProtocols(List<String> protocols) {
        this.protocols = Collections.unmodifiableList(protocols);
    }

}
