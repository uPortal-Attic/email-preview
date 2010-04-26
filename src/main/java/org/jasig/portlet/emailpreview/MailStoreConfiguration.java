package org.jasig.portlet.emailpreview;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class MailStoreConfiguration {
    
    private String protocol;
    private String host;
    private int port;
    private String inboxFolderName;
    
    private int timeout;
    private int connectionTimeout;
    
    private String linkServiceKey;
    private String authenticationServiceKey;
    
    private Map<String, String> additionalProperties = new HashMap<String, String>();
    private Map<String, String> javaMailProperties = new HashMap<String, String>();
    
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
            .append(this.additionalProperties)
            .append(this.javaMailProperties)
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
            .append("Additional properties", this.additionalProperties)
            .append("Java Mail properties", this.javaMailProperties)
            .toString();
    }

}
