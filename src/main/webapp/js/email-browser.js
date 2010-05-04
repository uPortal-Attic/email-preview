/*
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
var jasig = jasig || {};

(function($, fluid){

    var displayMessage = function(that, num) {
        
        // display the loading message while we retrieve the desired message
        showLoadingMessage(that);
        
        // get the message
        var message = getMessage(that, num);
        
        // update the individual message display with our just-retrieved message
        var html = message.content.html ? message.content.contentString : "<pre>" + message.content.contentString + "</pre>";
        $(".message-content").html(html);
        $(".email-message .subject").html(message.subject);
        $(".email-message .sender").html(message.sender);
        $(".email-message .sentDate").html(message.sentDateString);
        
        // show the message display
        showEmailMessage(that);
        
        return false;
    };
                    
    /**
     * Retrieve batched email from the server
     */
    var getEmail = function(that, start, size, sortKey, sortDir) {
        var account;
        $.ajax({
            url: that.options.accountInfoUrl,
            async: false,
            data: { pageStart: start, numberOfMessages: size },
            type: 'POST',
            dataType: "json",
            success: function(data) { account = data; }
        });
        return account.accountInfo.messages;
    };
     
    var getEmailFunction = function(that) {
        return function(start, size, sortKey, sortDir) {
            return getEmail(that, start, size, sortKey, sortDir);
        };
    };
     
    var getMessage = function(that, messageNum) {
        var message;
        $.ajax({
            url: that.options.messageUrl,
            async: false,
            data: { messageNum: messageNum },
            type: 'POST',
            dataType: "json",
            success: function(data) { message = data.message; }
        });
        return message;
    };
     
    var getAccount = function(that) {
        var account;
        $.ajax({
            url: that.options.accountInfoUrl,
            async: false,
            data: { pageStart: 1, numberOfMessages: 1 },
            type: 'POST',
            dataType: "json",
            success: function(data) { account = data; }
        });
        return account;
    };
     
    var showEmailList = function(that) {
        that.locate("loadingMessage").hide();
        that.locate("emailMessage").hide();
        that.locate("emailList").show();
    };
     
    var showLoadingMessage = function(that) {
        that.locate("emailList").hide();
        that.locate("emailMessage").hide();
        that.locate("loadingMessage").show();
    };
     
    var showEmailMessage = function(that) {
        that.locate("loadingMessage").hide();
        that.locate("emailList").hide();
        that.locate("emailMessage").show();
    };

    var getClasses = function(idx, message) {
        var classes = "";
        if (idx % 2 == 0) classes += " portlet-section-alternate";
        if (message.unread) classes += " unread";
        if (message.answered) classes += " answered";
        if (message.deleted) classes += " deleted";
        return classes;
    };
                
    jasig.EmailBrowser = function(container, options) {
        
        var that = fluid.initView("jasig.EmailBrowser", container, options);

        that.refresh = function() {
            // TODO
        };

        showLoadingMessage(that);
        
        var account = getAccount(that);
        that.locate("unreadMessageCount").html(account.accountInfo.unreadMessageCount + (account.accountInfo.unreadMessageCount != 1 ? " new messages" : " new message"));
        
        var options = {
            pagerOptions: {
               columnDefs: [
                   { key: "subject", valuebinding: "*.subject",
                        components: function(row, index) {
                            return {
                                value: "\${*.subject}",
                                decorators: [
                                    { attrs: { messageNum: '\${*.messageNumber}' } },
                                    { type: "jQuery", func: "click",
                                        args: function(){ displayMessage(that, $(this).attr("messageNum")); }
                                    },
                                    { type: "addClass", classes: getClasses(index, row) }
                                ]
                            }
                        }
                   },
                   { key: "sender", valuebinding: "*.senderName",
                       components: function(row, index) {
                           return {
                               value: "\${*.senderName}",
                               decorators: [
                                   { type: "addClass", classes: getClasses(index, row) }
                               ]
                           }
                       }
                   },
                   { key: "sentDate", valuebinding: "*.sentDateString",
                       components: function(row, index) {
                           return {
                               value: "\${*.sentDateString}",
                               decorators: [
                                   { type: "addClass", classes: getClasses(index, row) }
                               ]
                           }
                       }
                   },
                   { key: "flags", valuebinding: "*.answered",
                       components: function(row, index) {
                           return {
                               decorators: [
                                   { type: "addClass", classes: getClasses(index, row) }
                               ]
                           };
                       }
                   }
               ],
               bodyRenderer: {
                   type: "fluid.pager.selfRender",
                   options: {
                       selectors: { root: "table" },
                       row: "row:"
                   }
               },
               pagerBar: {
                   type: "fluid.pager.pagerBar", 
                   options: {
                       pageList: { 
                           type: "fluid.pager.renderedPageList",
                           options: { 
                               linkBody: "a",
                               pageStrategy: fluid.pager.gappedPageStrategy(3, 1)
                           }
                       }
                   }
               }
            },
            dataFunction: getEmailFunction(that),
            dataLengthFunction: function() { return account.accountInfo.totalMessageCount; }
        };
        
        that.pager = unicon.batchedpager(that.locate("emailList"), options);

        that.locate("refreshLink").click(that.refresh);
        that.locate("returnLink").click(function(){ showEmailList(that); });
        that.locate("inboxLink").attr("href", account.inboxUrl);

        showEmailList(that);

        return that;
    
    };

    fluid.defaults("jasig.EmailBrowser", {
        accountInfoUrl: null,
        messageUrl: null,
        selectors: {
            refreshLink: ".refresh-link",
            returnLink: ".return-link",
            emailList: ".email-list",
            emailMessage: ".email-message",
            loadingMessage: ".loading-message",
            unreadMessageCount: ".unread-message-count",
            inboxLink: ".inbox-link"
        }
    });

})(jQuery, fluid);