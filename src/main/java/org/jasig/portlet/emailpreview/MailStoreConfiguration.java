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
package org.jasig.portlet.emailpreview;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jasig.portlet.emailpreview.dao.MailPreferences;
import org.jasig.portlet.emailpreview.service.IServiceBroker;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 */
public final class MailStoreConfiguration {
    
    // Connection settings
    private String protocol;
    private String host;
    private int port;
    private String inboxFolderName;
    
    private int timeout;
    private int connectionTimeout;
    
    private String linkServiceKey;
    private String authenticationServiceKey;
    private List<String> allowableAuthenticationServiceKeys;
    private String usernameSuffix;
    private boolean displayMailAttribute;

    private boolean exchangeAutodiscover;
    private String mailAccount;

    private boolean ewsUseMailAttribute;

    private Map<String, String> additionalProperties = new HashMap<String, String>();
    private Map<String, String> javaMailProperties = new HashMap<String, String>();

    // Preferences
    private boolean markMessagesAsRead;
    private boolean allowRenderingEmailContent = true;
    
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String mailStoreProtocol) {
        this.protocol = mailStoreProtocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String mailHost) {
        this.host = mailHost;
    }
    
    public boolean getMarkMessagesAsRead() {
        return markMessagesAsRead;
    }
    
    public void setMarkMessagesAsRead(boolean markMessagesAsRead) {
        this.markMessagesAsRead = markMessagesAsRead;
    }

    public void setAllowRenderingEmailContent(Boolean allow) {
        allowRenderingEmailContent = allow;
    }
    
    public boolean getAllowRenderingEmailContent() {
        return allowRenderingEmailContent;
    }
    
    public int getPort() {
        return port;
    }

    public void setPort(int mailPort) {
        this.port = mailPort;
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

    public Map<String, String> getJavaMailProperties() {
        return javaMailProperties;
    }

    public boolean isExchangeAutodiscover() {
        return exchangeAutodiscover;
    }

    public void setExchangeAutodiscover(boolean exchangeAutodiscover) {
        this.exchangeAutodiscover = exchangeAutodiscover;
    }

    public String getMailAccount() {
        return mailAccount;
    }

    public void setMailAccount(String mailAccount) {
        this.mailAccount = mailAccount;
    }

    public boolean isEwsUseMailAttribute() { return ewsUseMailAttribute; }

    public void setEwsUseMailAttribute(boolean ewsUseMailAttribute) {this.ewsUseMailAttribute = ewsUseMailAttribute; }

    public void setJavaMailProperties(Map<String, String> properties) {
        this.javaMailProperties = properties;
    }
    
    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
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

    public List<String> getAllowableAuthenticationServiceKeys() {
        return allowableAuthenticationServiceKeys;
    }

    public void setAllowableAuthenticationServiceKeys(List<String> allowableAuthenticationServiceKeys) {
        this.allowableAuthenticationServiceKeys = Collections.unmodifiableList(allowableAuthenticationServiceKeys);
    }
    
    public String getUsernameSuffix() {
        return usernameSuffix;
    }

    public void setUsernameSuffix(String usernameSuffix) {
        this.usernameSuffix = usernameSuffix;
    }

    public boolean isDisplayMailAttribute() {
        return displayMailAttribute;
    }

    public void setDisplayMailAttribute(boolean displayMailAttribute) {
        this.displayMailAttribute = displayMailAttribute;
    }

    public boolean isReadOnly(PortletRequest req, MailPreferences mp) {
        PortletPreferences prefs = req.getPreferences();
        return prefs.isReadOnly(mp.getKey());
    }

    public boolean supportsToggleSeen() {
        // To the best of my understanding, the IMAP protocol supports 
        // the SEEN flag and POP3 just doesn't  
        String protocol = this.getProtocol().toLowerCase(); // Left toLowerCase in case existing installations have case wrong
        return protocol.startsWith(IServiceBroker.IMAP.toLowerCase())
                || protocol.equalsIgnoreCase(IServiceBroker.EXCHANGE_WEB_SERVICES);
        // NB:  We probably *should* also be checking whether the javax.mail.Folder 
        // object implements UIDFolder, but that's not easy with the present set of 
        // class interactions.  Something to work in on refactoring.
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MailStoreConfiguration)) {
            return false;
        }

        MailStoreConfiguration owner = (MailStoreConfiguration) obj;
        
        return new EqualsBuilder()
            .append(this.host, owner.getHost())
            .append(this.port, owner.getPort())
            .append(this.protocol, owner.getProtocol())
            .append(this.inboxFolderName, owner.getInboxFolderName())
            .append(this.timeout, owner.getTimeout())
            .append(this.connectionTimeout, owner.getConnectionTimeout())
            .append(this.linkServiceKey, owner.getLinkServiceKey())
            .append(this.authenticationServiceKey, owner.getAuthenticationServiceKey())
            .append(this.allowableAuthenticationServiceKeys, owner.getAllowableAuthenticationServiceKeys())
            .append(this.usernameSuffix, owner.getUsernameSuffix())
            .append(this.exchangeAutodiscover, owner.isExchangeAutodiscover())
            .append(this.ewsUseMailAttribute, owner.isEwsUseMailAttribute())
            .append(this.displayMailAttribute, owner.isDisplayMailAttribute())
            .append(this.additionalProperties, owner.getAdditionalProperties())
            .append(this.javaMailProperties, owner.getJavaMailProperties())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.host)
            .append(this.port)
            .append(this.protocol)
            .append(this.inboxFolderName)
            .append(this.timeout)
            .append(this.connectionTimeout)
            .append(this.linkServiceKey)
            .append(this.authenticationServiceKey)
            .append(this.allowableAuthenticationServiceKeys)
            .append(this.usernameSuffix)
            .append(this.exchangeAutodiscover)
            .append(this.ewsUseMailAttribute)
            .append(this.displayMailAttribute)
            .append(this.additionalProperties)
            .append(this.javaMailProperties)
            .append(this.markMessagesAsRead)
            .append(this.allowRenderingEmailContent)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("host", this.host)
            .append("port", this.port)
            .append("protocol", this.protocol)
            .append("inbox", this.inboxFolderName)
            .append("timeout", this.timeout)
            .append("connectionTimeout", this.connectionTimeout)
            .append("linkServiceKey", this.linkServiceKey)
            .append("authenticationServiceKey", this.authenticationServiceKey)
            .append("allowableAuthenticationServiceKeys", this.allowableAuthenticationServiceKeys)
            .append("usernameSuffix", this.usernameSuffix)
            .append("markMessagesAsRead", this.markMessagesAsRead)
            .append("allowRenderingEmailContent", this.allowRenderingEmailContent)
            .append("exchangeAutodiscover", this.exchangeAutodiscover)
            .append("ewsUseMailAttribute", this.ewsUseMailAttribute)
            .append("displayMailAttribute", this.displayMailAttribute)
            .append("Additional properties", this.additionalProperties)
            .append("Java Mail properties", this.javaMailProperties)
            .toString();
    }

}
