/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.portlet.emailpreview.service.auth;

import java.util.Collection;

public interface IAuthenticationServiceRegistry {

  /**
   * Return an instance of the authentication service associated with the supplied key. If no
   * matching authentication service can be found, this method will return <code>null</code>.
   *
   * @param key
   * @return
   */
  public IAuthenticationService getAuthenticationService(String key);

  /**
   * Return a list of all currently-registered authentication services.
   *
   * @return
   */
  public Collection<IAuthenticationService> getServices();
}
