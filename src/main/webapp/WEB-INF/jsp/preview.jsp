<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>

<script src="<rs:resourceURL value="/rs/jquery/1.3.2/jquery-1.3.2.min.js"/>" type="text/javascript"></script>
<script src="<rs:resourceURL value="/rs/jqueryui/1.7.2/jquery-ui-1.7.2-v2.min.js"/>" type="text/javascript"></script>
<script src="<rs:resourceURL value="/rs/fluid/1.1.2/js/fluid-all-1.1.2.min.js"/>" type="text/javascript"></script>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/email.css"/>"/>

<c:set var="n"><portlet:namespace/></c:set>
<portlet:actionURL var="accountInfoUrl">
    <portlet:param name="action" value="accountSummary"/>
</portlet:actionURL>
<portlet:actionURL var="messageUrl">
    <portlet:param name="action" value="emailMessage"/>
</portlet:actionURL>

<div id="${n}container" class="email-container">

    <div class="loading-message">
        
    </div>

    <div class="email-list" style="display:none;">
        <p>
            Inbox (<span class="unread-message-count"></span>)
            | <a class="refresh-link" href="javascript:;">Refresh</a>
        </p>
    
        <table cellpadding="3" cellspacing="0" class="email-portlet-table portlet-font">
            <tr>
                <th class="flags-header">&nbsp;</th>
                <th>Subject</th>
                <th>Sender</th>
                <th>Date Sent</th>
            </tr>
            <tr class="email-row">
                <td class="flags">
                    <span class="answered-span">&nbsp;</span>
                </td>
                <td class="subject"></td>
                <td class="sender"></td>
                <td class="sentDate"></td>
            </tr>
        </table>
    </div>
    
    <div class="email-message" style="display:none">
        <table cellpadding="0" cellspacing="0" class="message-headers">
            <tr><td class="message-header-name">From</td><td class="subject"></td></tr>
            <tr><td class="message-header-name">Subject</td><td class="sender"></td></tr>
            <tr><td class="message-header-name">Date</td><td class="sentDate"></td></tr>
        </table>
        <hr/>
        <div class="message-content">
        </div>
        <p><a class="return-link" href="javascript:;">Return to messages</a></p>
    </div>

</div>

<script type="text/javascript">

    var ${n} = {};
    ${n}.jQuery = jQuery.noConflict(true);
    ${n}.fluid = fluid;
    fluid = null;

    ${n}.jQuery(function(){
       var $ = ${n}.jQuery;
       var fluid = ${n}.fluid;

       var cutpoints = [
            { id: "unreadMessageCount", selector: ".unread-message-count" },
            { id: "emailRow:", selector: ".email-row" },
            { id: "subject", selector: ".subject" },
            { id: "sender", selector: ".sender" },
            { id: "sentDate", selector: ".sentDate" }
        ];

       var getMessage = function(num) {
           $(".email-message").hide();
           $(".email-list").hide();
           $(".loading-message").show();
           $.post("${ messageUrl }", { messageNum: num }, 
                   function(data){
                       var html = data.message.html ? data.message.content.contentString : "<pre>" + data.message.content.contentString + "</pre>";
                       $(".message-content").html(html);
                       $(".email-message .subject").html(data.message.subject);
                       $(".email-message .sender").html(data.message.sender);
                       $(".email-message .sentDate").html(data.message.sentDateString);
                       $(".loading-message").hide();
                       $(".email-message").show();
                   }, 
                   "json");
               return false;
       };
                       
       
       var getComponentTreeFromAccount = function(account) {
           var tree = { children: [] };

           tree.children.push(
               { ID: "unreadMessageCount",  value: account.unreadMessageCount + (account.unreadMessageCount == 1 ? " new message" : " new messages") }
           );

           $(account.messages).each(function(idx, message){
               var classes = "";
               if (idx % 2 == 0) classes += " portlet-section-alternate";
               if (message.unread) classes += " unread";
               if (message.answered) classes += " answered";
               if (message.deleted) classes += " deleted";
               
               tree.children.push(
                   { ID: "emailRow:",
                       decorators: [
                          { type: "addClass", classes: classes }
                       ],
                       children: [
                          { 
                              ID: "subject", value: message.subject,
                              decorators: [
                                  { attrs: { messageNum: message.messageNumber } },
                                  { type: "jQuery", func: "click",
                                      args: function(){ getMessage($(this).attr("messageNum")); }
                                  }
                              ]
                          },
                          { ID: "sender", value: message.senderName },
                          { ID: "sentDate", value: message.sentDateString }
                       ]  
                   }
               );
           });

           return tree;

       };

       var loadEmail = function() {
           $.get("${ accountInfoUrl }", { }, 
               function(data){
                   $(".email-list").hide();
                   $(".loading-message").show();
                   var tree = getComponentTreeFromAccount(data.accountInfo);
                   fluid.selfRender($("#${n}container .email-list"), tree, { cutpoints: cutpoints });
                   $(".loading-message").hide();
                   $(".email-list").show();
                   $(".refresh-link").click(loadEmail);
                   $(".return-link").click(function(){
                       $(".email-message").hide();
                       $(".email-list").show();
                   });
               }, 
               "json");
           return false;
       };
                   
       $(document).ready(function(){
           loadEmail();
       }); 
    });

</script>
