package org.jasig.portlet.emailpreview.mvc;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.collections.map.LazyMap;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IMailStoreDao;

public class MailStoreConfigurationForm implements Serializable {

    public static final String UNCHANGED_SECURE_VALUE = "uNch@ng3d.S3cur3!"; 
    private static final long serialVersionUID = 1L;

    private String protocol;
    private String host;
    private int port;
    private String inboxFolderName;

    private int timeout;
    private int connectionTimeout;

    private String linkServiceKey;
    private String authenticationServiceKey;
    private List<String> allowableAuthenticationServiceKeys = Collections.emptyList();

    @SuppressWarnings("unchecked")
    private Map<String, Attribute> additionalProperties = LazyMap.decorate(
            new HashMap<String, Attribute>(), new AttributeFactory());
    
    @SuppressWarnings("unchecked")
    private Map<String, Attribute> javaMailProperties = LazyMap.decorate(
            new HashMap<String, Attribute>(), new AttributeFactory());

    public static MailStoreConfigurationForm create(IMailStoreDao mailStoreDao, PortletRequest req) {
        
        MailStoreConfiguration config = mailStoreDao.getConfiguration(req);

        MailStoreConfigurationForm form = new MailStoreConfigurationForm();
        form.setHost(config.getHost());
        form.setPort(config.getPort());
        form.setProtocol(config.getProtocol());
        form.setInboxFolderName(config.getInboxFolderName());
        form.setAuthenticationServiceKey(config.getAuthenticationServiceKey());
        form.setAllowableAuthenticationServiceKeys(config.getAllowableAuthenticationServiceKeys());
        form.setLinkServiceKey(config.getLinkServiceKey());
        form.setConnectionTimeout(config.getConnectionTimeout());
        form.setTimeout(config.getTimeout());
        
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
