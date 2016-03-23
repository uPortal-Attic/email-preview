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
package org.jasig.portlet.emailpreview.mvc;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.collections4.map.LazyMap;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;

public class MailStoreConfigurationForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String protocol;
    private String host;
    private int port;
    private String inboxFolderName;
    private Boolean markMessagesAsRead;
    private Boolean allowRenderingEmailContent = true;

    private Boolean exchangeAutodiscover;
    private Boolean ewsUseMailAttribute;
    private Boolean displayMailAttribute;

    private int timeout;
    private int connectionTimeout;

    private String linkServiceKey;
    private String authenticationServiceKey;
    private List<String> allowableAuthenticationServiceKeys = Collections.emptyList();
    private String usernameSuffix;

    private Map<String, Attribute> additionalProperties = LazyMap.lazyMap(
            new HashMap<String, Attribute>(), new AttributeFactory());
    
    private Map<String, Attribute> javaMailProperties = LazyMap.lazyMap(
            new HashMap<String, Attribute>(), new AttributeFactory());

    public static MailStoreConfigurationForm create(final MailStoreConfiguration config, final PortletRequest req) {
        
        MailStoreConfigurationForm form = new MailStoreConfigurationForm();
        form.setHost(config.getHost());
        form.setPort(config.getPort());
        form.setProtocol(config.getProtocol());
        form.setInboxFolderName(config.getInboxFolderName());
        form.setMarkMessagesAsRead(config.getMarkMessagesAsRead());
        form.setAuthenticationServiceKey(config.getAuthenticationServiceKey());
        form.setAllowableAuthenticationServiceKeys(config.getAllowableAuthenticationServiceKeys());
        form.setUsernameSuffix(config.getUsernameSuffix());
        form.setLinkServiceKey(config.getLinkServiceKey());
        form.setConnectionTimeout(config.getConnectionTimeout());
        form.setTimeout(config.getTimeout());
        form.setAllowRenderingEmailContent(config.getAllowRenderingEmailContent());
        form.setExchangeAutodiscover(config.isExchangeAutodiscover());
        form.setEwsUseMailAttribute(config.isEwsUseMailAttribute());
        form.setDisplayMailAttribute(config.isDisplayMailAttribute());

        for (Map.Entry<String, String> entry : config.getJavaMailProperties().entrySet()) {
            form.getJavaMailProperties().put(entry.getKey(), new Attribute(entry.getValue()));
        }
        
        for (Map.Entry<String, String> entry : config.getAdditionalProperties().entrySet()) {
            form.getAdditionalProperties().put(entry.getKey(), new Attribute(entry.getValue()));
        }
        
        return form;

    }

    public List<String> getAllowableAuthenticationServiceKeys() {
        return allowableAuthenticationServiceKeys;
    }

    public void setAllowableAuthenticationServiceKeys(List<String> allowableAuthenticationServiceKeys) {
        if (allowableAuthenticationServiceKeys != null) {
            this.allowableAuthenticationServiceKeys = Collections.unmodifiableList(allowableAuthenticationServiceKeys);
        } else {
            this.allowableAuthenticationServiceKeys = Collections.emptyList();
        }
    }

    public String getUsernameSuffix() {
        return usernameSuffix;
    }

    public void setUsernameSuffix(String usernameSuffix) {
        this.usernameSuffix = usernameSuffix;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getInboxFolderName() {
        return inboxFolderName;
    }

    public void setInboxFolderName(String inboxFolderName) {
        this.inboxFolderName = inboxFolderName;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getLinkServiceKey() {
        return linkServiceKey;
    }

    public void setLinkServiceKey(String linkServiceKey) {
        this.linkServiceKey = linkServiceKey;
    }

    public String getAuthenticationServiceKey() {
        return authenticationServiceKey;
    }

    public void setAuthenticationServiceKey(String authenticationServiceKey) {
        this.authenticationServiceKey = authenticationServiceKey;
    }

    public Map<String, Attribute> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(
            Map<String, Attribute> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Map<String, Attribute> getJavaMailProperties() {
        return javaMailProperties;
    }

    public void setJavaMailProperties(Map<String, Attribute> javaMailProperties) {
        this.javaMailProperties = javaMailProperties;
    }

    public void setMarkMessagesAsRead(Boolean markMessagesAsRead) {
        this.markMessagesAsRead = markMessagesAsRead;
    }

    public boolean getMarkMessagesAsRead() {
        return markMessagesAsRead;
    }

    public void setAllowRenderingEmailContent(Boolean allow) {
        allowRenderingEmailContent = allow;
    }
    
    public boolean getAllowRenderingEmailContent() {
        return allowRenderingEmailContent;
    }

    public Boolean getExchangeAutodiscover() {
        return exchangeAutodiscover;
    }

    public void setExchangeAutodiscover(Boolean exchangeAutodiscover) {
        this.exchangeAutodiscover = exchangeAutodiscover;
    }

    public Boolean getEwsUseMailAttribute() {
        return ewsUseMailAttribute;
    }

    public void setEwsUseMailAttribute(Boolean ewsUseMailAttribute) {
        this.ewsUseMailAttribute = ewsUseMailAttribute;
    }

    public Boolean getDisplayMailAttribute() {
        return displayMailAttribute;
    }

    public void setDisplayMailAttribute(Boolean displayMailAttribute) {
        this.displayMailAttribute = displayMailAttribute;
    }
}
