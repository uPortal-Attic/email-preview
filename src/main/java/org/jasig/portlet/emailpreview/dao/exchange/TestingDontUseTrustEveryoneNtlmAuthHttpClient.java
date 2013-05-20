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
package org.jasig.portlet.emailpreview.dao.exchange;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

/**
 * Simple Test client that ignores cert issues.  DO NOT USE IN PRODUCTION!!!
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

public class TestingDontUseTrustEveryoneNtlmAuthHttpClient extends DefaultHttpClient {
    static SchemeRegistry registry;
    static {
        try {
            registry = new SchemeRegistry();

            SSLSocketFactory socketFactory = new SSLSocketFactory(new TrustStrategy() {

                public boolean isTrusted(final X509Certificate[] chain, String authType) throws CertificateException {
                    // Oh, I am easy...
                    return true;
                }

            }, org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            registry.register(new Scheme("https", 443, socketFactory));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    TestingDontUseTrustEveryoneNtlmAuthHttpClient() {
        super(new PoolingClientConnectionManager(registry));
    }
}
