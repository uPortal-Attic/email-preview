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
package org.jasig.portlet.emailpreview.service.auth;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision: 20529 $
 */
public class SimplePasswordAuthenticator extends Authenticator {
    
    private final String username;
    private final String password;

    /**
     * Construct a new SimplePasswordAuthenticator instance.
     * 
     * @param username
     * @param password
     */
    public SimplePasswordAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * @see javax.mail.Authenticator#getPasswordAuthentication()
     */
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimplePasswordAuthenticator)) {
            return false;
        }
        
        SimplePasswordAuthenticator auth2 = (SimplePasswordAuthenticator) obj;
        
        return new EqualsBuilder()
            .append(this.username, auth2.username)
            .append(this.password, auth2.password)
            .isEquals();
        
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.username)
            .append(this.password)
            .toHashCode();
    }
    
}
