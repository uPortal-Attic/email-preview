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

package org.jasig.portlet.emailpreview.util;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
*
* @author Drew Wills, drew@unicon.net
*/
@Component
public final class MessageUtils implements InitializingBean, ApplicationContextAware {
    
    private static final String CLICKABLE_URLS_REGEX =
        "\\b((?:(?:https?|ftp|file)://|www\\.|ftp\\.)" +
        "(?:\\([-A-Z0-9+&@#/%=~_|$?!:,.]*\\)|[-A-Z0-9+&@#/%=~_|$?!:,.])*" +
        "(?:\\([-A-Z0-9+&@#/%=~_|$?!:,.]*\\)|[A-Z0-9+&@#/%=~_|$]))";
    private static final Pattern CLICKABLE_URLS_PATTERN = Pattern.compile(CLICKABLE_URLS_REGEX, Pattern.CASE_INSENSITIVE);
    private static final String CLICKABLE_URLS_PART1 = "<a href=\"";
    private static final String CLICKABLE_URLS_PART2 = "\" target=\"_new\">";
    private static final String CLICKABLE_URLS_PART3 = "</a>";
    
	/** Pattern to find HTML anchors without target. */ 
	private static final String ANCHOR_WITHOUT_TARGET_REGEX = "<a(((?!target=)[^>])*)>";

	/** Replacement to add target to HTML anchors. */ 
	private static final String ADD_TARGET_TO_ANCHOR_REPLACEMENT = "<a$1 target=\"_new\">";
	
    private static final Log LOG = LogFactory.getLog(MessageUtils.class);

    private String filePath = "classpath:antisamy.xml";  // default
    private ApplicationContext ctx;
    private Policy policy;

    /**
     * Set the file path to the Anti-samy policy file to be used for cleaning
     * strings.
     *
     * @param path
     */
    public void setSecurityFile(String path) {
        this.filePath = path;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        InputStream stream = ctx.getResource(filePath).getInputStream();
        policy = Policy.getInstance(stream);
    }


    public String cleanHTML(String message) {
        // As a convenience for the caller and to avoid an error, if the message is null, return an empty string
        // to avoid an exception.
        if (message == null) {
            return "";
        }
        try {
            AntiSamy as = new AntiSamy();
            CleanResults cr = as.scan(message, policy);
            return cr.getCleanHTML();
        } catch (ScanException e) {
            throw new EmailPreviewException("Error cleansing email message", e);
        } catch (PolicyException e) {
            throw new EmailPreviewException("Error with AntiSamy policy exception", e);
        }
    }

    public static String addClickableUrlsToMessageBody(String msgBody) {
        
        // Assertions.
        if (msgBody == null) {
            String msg = "Argument 'msgBody' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        StringBuffer rslt = new StringBuffer();

        Matcher m = CLICKABLE_URLS_PATTERN.matcher(msgBody);
        while (m.find()) {
            StringBuilder bldr = new StringBuilder();
            String text = m.group(1);
            // Handle special case where URL not prefixed with required protocol
            String url = text.startsWith("www.") 
                                ? "http://" + text 
                                : text;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Making embedded URL '" + text + 
                        "' clickable at the following href:  " + url);
            }
            bldr.append(CLICKABLE_URLS_PART1).append(url)
                        .append(CLICKABLE_URLS_PART2).append(text)
                        .append(CLICKABLE_URLS_PART3);
            m.appendReplacement(rslt, bldr.toString());
        }
        m.appendTail(rslt);

        return rslt.toString();
        
    }

    public static String addMissingTargetToAnchors(final String msgBody) {
        
        // Assertions.
        if (msgBody == null) {
            String msg = "Argument 'msgBody' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        final String targetAddedContents = msgBody.replaceAll(ANCHOR_WITHOUT_TARGET_REGEX, ADD_TARGET_TO_ANCHOR_REPLACEMENT);

        return targetAddedContents;
        
    }
}
