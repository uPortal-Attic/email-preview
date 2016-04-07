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
package org.jasig.portlet.emailpreview.service;

import org.apache.commons.lang3.StringUtils;

public class ConfigurationParameter {

    private String key;
    private String label;
    private String defaultValue;
    private boolean requiresEncryption;

    public ConfigurationParameter() {}

    public ConfigurationParameter(String key, String label, String defaultValue, boolean requiresEncryption) {
        
        // Assertions
        if (StringUtils.isBlank(key)) {
            String msg = "Argument 'key' cannot be blank";
            throw new IllegalArgumentException(msg);
        }
        if (StringUtils.isBlank(label)) {
            String msg = "Argument 'label' cannot be blank";
            throw new IllegalArgumentException(msg);
        }
        // NB:  defaultValue may be null
        
        this.key = key;
        this.label = label;
        this.defaultValue = defaultValue;
        this.requiresEncryption = requiresEncryption;
        
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
