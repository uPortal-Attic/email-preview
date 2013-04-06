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

package org.jasig.portlet.emailpreview.caching;

/**
 * 
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public interface IMessageCacheKeyGenerator {

	/**
	 * Returns a cache key for the user message
	 * 
	 * @param username username
	 * @param messageId messageId (note may not be globally unique to user with POP3 or IMAP protocols)
	 * @return cache key for the user message
	 */
	String getKey(String username, String messageId);

}