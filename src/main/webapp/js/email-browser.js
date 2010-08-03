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
    
    // If true, will drop the cache entry (if present) 
    // for this user on the next call to the server
    var clearCache = "false";
                    
    /**
     * Retrieve batched email from the server
     */
    var getEmail = function(that, start, size, sortKey, sortDir) {
        if (that.cache[start] && that.cache[start][size]) {
            return that.cache[start][size];
        }
        
        var account;
        $.ajax({
            url: that.options.accountInfoUrl,
            async: false,
            data: { pageStart: start, numberOfMessages: size, forceRefresh: clearCache },
            type: 'POST',
            dataType: "json",
            success: function(data) { 
                clearCache = "false";
                account = data; 
            },
            error: function(request, textStatus, error) { showErrorMessage(that, request.status); }
        });
        var messages = account.accountInfo.messages;
        that.cache[start] = that.cache[start] || [];
        that.cache[start][size] = messages;
        
        return messages;
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
            success: function(data) { message = data.message; },
            error: function(request, textStatus, error) { showErrorMessage(that, request.status); }
        });
        return message;
    };
     
    var getAccount = function(that) {
        var account;
        $.ajax({
            url: that.options.accountInfoUrl,
            async: false,
            data: { pageStart: 0, numberOfMessages: 20 },
            type: 'POST',
            dataType: "json",
            success: function(data) { account = data; },
            error: function(request, textStatus, error) { showErrorMessage(that, request.status); }
        });
        var messages = account.accountInfo.messages;
        that.cache[0] = that.cache[1] || [];
        that.cache[0][20] = messages;
        return account;
    };
     
    var showEmailList = function(that) {
        that.locate("loadingMessage").hide();
        that.locate("emailMessage").hide();
        that.locate("errorMessage").hide();
        that.locate("emailList").show();
    };
     
    var showLoadingMessage = function(that) {
        that.locate("emailList").hide();
        that.locate("emailMessage").hide();
        that.locate("errorMessage").hide();
        that.locate("loadingMessage").show();
    };
     
    var showEmailMessage = function(that) {
        that.locate("loadingMessage").hide();
        that.locate("emailList").hide();
        that.locate("errorMessage").hide();
        that.locate("emailMessage").show();
    };
    
    var showErrorMessage = function(that, code) {
        var message;
        if (code == 401) message = "Failed to authenticate to mail store";
        else if (code == 504) message = "Failed to authenticate to mail store";
        else message = "Unknown error connecting to mail store";
        
        that.locate("loadingMessage").hide();
        that.locate("emailList").hide();
        that.locate("emailMessage").hide();
        that.locate("errorMessage").html(message).show();
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

        that.cache = [];

        showLoadingMessage(that);
        
        var account = getAccount(that);
        if (!account) { return that; }
        that.locate("unreadMessageCount").html(account.accountInfo.unreadMessageCount + (account.accountInfo.unreadMessageCount != 1 ? " unread messages" : " unread message"));
        
        var options = {
            pagerOptions: {
               columnDefs: [
                   { key: "subject", valuebinding: "*.subject",
                        components: function(row, index) {
                            return {
                                markup: "<a href=\"javascript:;\">\${*.subject}</a>",
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

        that.refresh = function() {
            showLoadingMessage(that);
            clearCache = "true";  // Server-side cache
            that.cache = [];      // Client-side cache
            that.pager.refreshView();
            showEmailList(that);
        };

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
            errorMessage: ".error-message",
            unreadMessageCount: ".unread-message-count",
            inboxLink: ".inbox-link"
        }
    });

})(jQuery, fluid);