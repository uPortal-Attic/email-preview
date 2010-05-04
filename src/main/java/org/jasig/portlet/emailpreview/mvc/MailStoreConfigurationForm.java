package org.jasig.portlet.emailpreview.mvc;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.LazyMap;

public class MailStoreConfigurationForm {

    private String protocol;
    private String host;
    private int port;
    private String inboxFolderName;

    private int timeout;
    private int connectionTimeout;

    private String linkServiceKey;
    private String authenticationServiceKey;

    @SuppressWarnings("unchecked")
    private Map<String, Attribute> additionalProperties = LazyMap.decorate(
            new HashMap<String, Attribute>(), new AttributeFactory());
    
    @SuppressWarnings("unchecked")
    private Map<String, Attribute> javaMailProperties = LazyMap.decorate(
            new HashMap<String, Attribute>(), new AttributeFactory());

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

}
