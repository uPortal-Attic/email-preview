/*
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
var emailPortlet = {
    init : function (context, portletDiv) {
        var $ = context.jQuery;
        $(document).ready(function(){
            hideProtocolIrrelevant();
            portletDiv.find('.plt-email-input-protocol').change(function() {
                hideProtocolIrrelevant();
            });
        });
        var hideAll = function hideAll() {
            portletDiv.find('.plt-email-exchange').addClass('hidden');
            portletDiv.find('.plt-email-imap').addClass('hidden');
            portletDiv.find('.plt-email-smtp').addClass('hidden');
        };
        var hideProtocolIrrelevant = function() {
            hideAll();
            var protocol = portletDiv.find('.plt-email-input-protocol').get(0).value;
            if (protocol == 'ExchangeWebServices') {
                portletDiv.find('.plt-email-exchange').removeClass('hidden');
            } else if (protocol == 'imap' || protocol == 'imaps') {
                portletDiv.find('.plt-email-imap').removeClass('hidden');
            } else if (protocol == 'pop3' || protocol == 'pop3s') {
                portletDiv.find('.plt-email-pop3').removeClass('hidden');
            }
        };
    }
}
