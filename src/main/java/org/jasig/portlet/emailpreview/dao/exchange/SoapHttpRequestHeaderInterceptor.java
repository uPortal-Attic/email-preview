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
package org.jasig.portlet.emailpreview.dao.exchange;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * Interceptor to remove {@code Content-Length} and {@code Transfer-Encoding} headers from the request. SAAJ and other
 * SOAP implementations set these headers themselves, and HttpClient throws an exception if they have been set.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

public class SoapHttpRequestHeaderInterceptor implements HttpRequestInterceptor {

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (request.containsHeader(HTTP.TRANSFER_ENCODING)) {
            request.removeHeaders(HTTP.TRANSFER_ENCODING);
        }
        if (request.containsHeader(HTTP.CONTENT_LEN)) {
            request.removeHeaders(HTTP.CONTENT_LEN);
        }
    }
}
