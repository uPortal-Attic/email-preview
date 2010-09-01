package org.jasig.portlet.emailpreview.service;

public class ConfigurationParameter {

    private String key;
    private String label;
    private String defaultValue;
    private boolean requiresEncryption;

    public ConfigurationParameter() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isEncryptionRequired() {
        return requiresEncryption;
    }

    public void setEncryptionRequired(boolean requiresEncryption) {
        this.requiresEncryption = requiresEncryption;
    }

}
