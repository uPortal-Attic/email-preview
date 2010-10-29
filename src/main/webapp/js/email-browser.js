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
            success: function(data) { 
                account = data; 
                var messages = account.accountInfo.messages;
                that.cache[0] = that.cache[1] || [];
                that.cache[0][20] = messages;
            },
            error: function(request, textStatus, error) { showErrorMessage(that, request.status); }
        });
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
    
    var errorHandlers = {
        401: function(that) {
            var msg = "Failed to authenticate to mail store";
            that.locate("errorText").html(msg);
            that.locate("errorMessage").show();
        },
        504: function(that) {
            var msg = "Mail store connection failed:  Gateway Timeout";
            that.locate("errorText").html(msg);
            that.locate("errorMessage").show();
        },
        'default': function(that) {
            var msg = "Unknown mail store error";
            that.locate("errorText").html(msg);
            that.locate("errorMessage").show();
        }
    }
    var showErrorMessage = function(that, code) {
        that.locate("loadingMessage").hide();
        that.locate("emailList").hide();
        that.locate("emailMessage").hide();
        var handler = errorHandlers[code] || errorHandlers['default'];
        handler(that);
    };

    var getClasses = function(idx, message) {
        var classes = "";
        if (idx % 2 == 0) classes += " portlet-section-alternate";
        if (message.unread) classes += " unread";
        if (message.answered) classes += " answered";
        if (message.deleted) classes += " deleted";
        return classes;
    };
    
    var removeMessages = function(messages, cache) {
        that.locate("emailRow").each(function(index, row) {
            if (row.find(options.selectors.selectMessage).attr("checked")) {
                row.remove();
            }
        });
    };
                
    jasig.EmailBrowser = function(container, options) {
        
        var that = fluid.initView("jasig.EmailBrowser", container, options);

        that.cache = [];

        showLoadingMessage(that);
        
        var account = getAccount(that);
        if (!account) { return that; }
        that.locate("unreadMessageCount").html(account.accountInfo.unreadMessageCount + (account.accountInfo.unreadMessageCount != 1 ? " unread messages" : " unread message"));
        
        var batchOptions = {
            pagerOptions: {
               columnDefs: [
                   { key: "select", valuebinding: "*.select",
                        components: function(row, index) {
                            return {
                                markup: '<input type="checkbox" class="select-message" name="selectMessage" value="\${*.uid}"/>',
                                decorators: [
                                    { attrs: { messageNum: '\${*.messageNumber}' } },
                                    { type: "addClass", classes: getClasses(index, row) }
                                ]
                            }
                        }
                   },
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
        
        that.pager = unicon.batchedpager(that.locate("emailList"), batchOptions);

        that.refresh = function() {
            showLoadingMessage(that);
            clearCache = "true";  // Server-side cache
            that.cache = [];      // Client-side cache
            that.pager.refreshView();
            showEmailList(that);
        };

        that.deleteMessage = function() {
            if (that.locate("emailRow").find("input[checked='true']").size() === 0) {
                alert("No Messages Selected.");
                return;
            }
            if (confirm("Delete Selected Messages?")) {
                showLoadingMessage(that);
                var params = that.locate("emailForm").serializeArray();
                var ajaxOptions = {
                    url: options.deleteUrl,
                    type: "POST",
                    data: params,
                    dataType: "json",
                    error: function(XMLHttpRequest, textStatus, errorThrown) {
                        showErrorMessage(that, request.status);
                    },
                    success: function(data) {
                        that.refresh();
                    }
                }
                $.ajax(ajaxOptions);
            }
        };

        that.toggleSelectAll = function() {
            var chk = $(this).attr("checked");
            that.locate("selectMessage").attr("checked", chk);
        }

        that.locate("refreshLink").click(that.refresh);
        if (account.accountInfo.deleteSupported) {
            that.locate("deleteLink").click(that.deleteMessage);
        } else {
            var anchor = that.locate("deleteLink");
            anchor.find("span").html("Delete Not Available");
            anchor.find("span").addClass("fl-text-silver");
            anchor.attr("title", "The delete feature is not supported by this mail store");
        }
        that.locate("returnLink").click(function(){ showEmailList(that); });
        that.locate("inboxLink").attr("href", account.inboxUrl);
        
        that.locate("selectAll").click(that.toggleSelectAll);

        showEmailList(that);

        return that;
    
    };

    fluid.defaults("jasig.EmailBrowser", {
        accountInfoUrl: null,
        messageUrl: null,
        deleteUrl: null,
        selectors: {
            refreshLink: ".refresh-link",
            deleteLink: ".delete-link",
            returnLink: ".return-link",
            emailForm: "form[name='email']",
            timestamp: "input[name='timestamp']",
            selectAll: ".select-all",
            selectMessage: ".select-message",
            emailList: ".email-list",
            emailRow: ".email-row",
            emailMessage: ".email-message",
            loadingMessage: ".loading-message",
            errorMessage: ".error-message",
            errorText: ".error-message #error-text",
            unreadMessageCount: ".unread-message-count",
            inboxLink: ".inbox-link"
        }
    });

})(jQuery, fluid);