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
package org.jasig.portlet.emailpreview;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class EmailMessageContent {

    private String content;
    private boolean isHtml;

    /**
     * Default constructor
     */
    public EmailMessageContent() {
    }

    /**
     * 
     * @param contentString
     * @param isHtml
     */
    public EmailMessageContent(String contentString, boolean isHtml) {
        this.content = contentString;
        this.isHtml = isHtml;
    }

    /**
     * 
     * @return
     */
    public String getContentString() {
        return content;
    }

    /**
     * 
     * @param content
     */
    public void setContentString(String content) {
        this.content = content;
    }

    /**
     * 
     * @return
     */
    public boolean isHtml() {
        return isHtml;
    }

    /**
     * 
     * @param isHtml
     */
    public void setHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }

}
