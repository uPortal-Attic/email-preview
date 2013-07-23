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

import org.junit.Test;
import static org.junit.Assert.*;

class EmailQuotaTest {

    @Test
    void testGetUsageAsPercentage() {
        Map testCases = [
            "0.00%"   : new EmailQuota(limit: 100L, usage: 0L),
            "25.00%"  : new EmailQuota(limit: 100L, usage: 25L),
            "50.00%"  : new EmailQuota(limit: 100L, usage: 50L),
            "100.00%" : new EmailQuota(limit: 100L, usage: 100L),
            "27.30%"  : new EmailQuota(limit: 1000L, usage: 273L)
        ];
        testCases.each { k, v ->
            assertTrue("Expected '$k' was '${v.usageAsPercentage}'", k == v.usageAsPercentage);
        }
    }

}
