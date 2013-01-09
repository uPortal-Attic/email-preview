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
package org.jasig.portlet.emailpreview.security;

import org.jasig.portlet.emailpreview.EmailPreviewException;

/**
 * A custom Exception for the email preview portlet that may be thrown as the result 
 * of a failure in encrypting/decrypting a string.
 *
 * @author Misagh Moayyed
 * @see IStringEncryptionService
 */
public class StringEncryptionException extends EmailPreviewException {

    private static final long serialVersionUID = 2252205978899483802L;
    
    public StringEncryptionException(Throwable cause) {
        super(cause);
    }

    public StringEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public StringEncryptionException(String message) {
        super(message);
    }

}
