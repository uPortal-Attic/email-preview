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
package org.jasig.portlet.emailpreview.service.link;

import java.util.Collections;
import java.util.List;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jen Bourey, jbourey@unicon.net
 */
@Component("simpleEmailLinkService")
public class SimpleEmailLinkServiceImpl implements IEmailLinkService {

    private static final String KEY = "default";
    public static final String INBOX_URL_PROPERTY = "inboxUrl";

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.IEmailLinkService#getKey()
     */
    public String getKey() {
        return KEY;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.IEmailLinkService#getInboxUrl(javax.portlet.PortletRequest, org.jasig.portlet.emailpreview.MailStoreConfiguration)
     */
    @Override
    public String getInboxUrl(MailStoreConfiguration configuration) {
        return configuration.getAdditionalProperties().get(INBOX_URL_PROPERTY);
    }

    @Override
    public List<ConfigurationParameter> getAdminConfigurationParameters() {
        ConfigurationParameter param = new ConfigurationParameter();
        param.setKey(INBOX_URL_PROPERTY);
        param.setLabel("Webmail inbox URL");
        return Collections.singletonList(param);
    }

    @Override
    public List<ConfigurationParameter> getUserConfigurationParameters() {
        return Collections.<ConfigurationParameter>emptyList();
    }

}
