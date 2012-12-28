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

(function($, fluid) {

	var displayMessage = function(that, cell) {
        // display the loading message while we retrieve the desired message
        showLoadingMessage(that);

        // get the message
        var messageNum = parseInt($(cell).attr("messageNum"));
        var message = getMessage(that, messageNum);

        // Update the display to reflect the new state of the SEEN flag
        if (that.options.markMessagesAsRead && $(cell).hasClass("unread")) {
            // The UI needs to reflect that a previously unread message is now read
            // $(cell).parent().children().removeClass("unread");
            var found = false;
            for (var f in that.cache) {
                var first = that.cache[f];
                for (var s in first) {
                    var second = first[s];
                    for (var m in second) {
                        var msg = second[m];
                        if (msg.messageNumber === messageNum) {
                            msg.unread = false;
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
                if (found) break;
            }
            that.pager.refreshView();
            var unreadCount = parseInt(that.locate("unreadMessageCount").html());
            if (unreadCount && unreadCount > 0) {
                that.locate("unreadMessageCount").html(unreadCount - 1);
            }
        }

        // update the individual message display with our just-retrieved message
        var html = message.content.html ? message.content.contentString : "<pre>" + message.content.contentString + "</pre>";

        that.container.find(".message-content").html(html);
        that.container.find(".email-message .subject").html(message.subject);
        that.container.find(".email-message .sender").html(message.sender);
        that.container.find(".email-message .sentDate").html(message.sentDateString);
        that.container.find(".email-message .message-uid").val(message.uid);

        if (that.options.markMessagesAsRead || !message.unread) {
            that.locate("markMessageReadButton").hide();
            that.locate("markMessageUnreadButton").show();
        } else {
            that.locate("markMessageReadButton").show();
            that.locate("markMessageUnreadButton").hide();
        }

        // show the message display
        showEmailMessage(that);

        return false;
    };

    // Top-level abstraction of the user's email account;
    // needs to be re-set whenever mail is fetched.
    var account = {};

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

        $.ajax({
            url: that.options.accountSummaryUrl,
            async: false,
            data: { pageStart: start, numberOfMessages: size, forceRefresh: clearCache },
            type: 'POST',
            dataType: "json",
            success: function(data) {
                if ( data.errorMessage != null ) {
                    showErrorMessage( that, 900, data.errorMessage );
                }
                clearCache = "false";
                account = data;
            },
            error: function(request, textStatus, error) {
                showErrorMessage(that, request.status);
            }
        });

        var messages = account.accountSummary ? account.accountSummary.messages : [];
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
            success: function(data) {
                if ( data.errorMessage != null ) {
                    showErrorMessage( that, 900, data.errorMessage );
                }
                message = data.message;
            },
            error: function(request, textStatus, error) {
                showErrorMessage(that, request.status);
            }
        });

        return message;
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

    var showErrorMessage = function(that, httpStatus, customMessage) {
        that.locate("loadingMessage").hide();
        that.locate("emailList").hide();
        that.locate("emailMessage").hide();
        if (httpStatus == 200) {
            /* We assume 200 AS AN ERROR means the mapge timed out (on uPortal
             * this event means the ACTION timed out and improperly went to
             * RENDER, where it should have resulted in a redirect).
             */
            httpStatus = 504;
        }
        var errorText = that.options.jsErrorMessages[httpStatus] || that.options.jsErrorMessages['default'];
        if (customMessage) {
            // Add a server-specified custom message to the end
            errorText += '<br/>' + customMessage;
        }
        that.locate("errorText").html(httpStatus + ": " + errorText);
        that.locate("errorMessage").show();
    };

    var getClasses = function(idx, message) {
        var classes = "";
        if (message.unread) classes += " unread";
        if (!message.unread) classes += " portlet-section-alternate";
        if (message.answered) classes += " answered";
        if (message.deleted) classes += " deleted";
        if (message.multipart) classes += " attached";
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

        var batchOptions = {
            batchSize: that.options.batchSize,
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
                    { key: "flags", valuebinding: "*.answered",
                        components: function(row, index) {
                            return {
                                decorators: [
                                    { type: "addClass", classes: getClasses(index, row) }
                                ]
                            };
                        }
                    },
                    { key: "attachments", valuebinding: "*.multipart",
                        components: function(row, index) {
                            return {
                                decorators: [
                                    { type: "addClass", classes: getClasses(index, row) }
                                ]
                            };
                        }
                    },
                    { key: "subject", valuebinding: "*.subject",
                        components: function(row, index) {
                            return {
                                markup: "<a href=\"javascript:;\">\${*.subject}</a>",
                                decorators: [
                                    { attrs: { messageNum: '\${*.messageNumber}' } },
                                    { type: "jQuery", func: "click",
                                        args: function(){ displayMessage(that, this); }
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
                }
                ],
                bodyRenderer: {
                    type: "fluid.pager.selfRender",
                    options: {
        				//Only change for mobile view :replace table by div.message_infos
                        selectors: { root: that.options.messagesInfoContainer },
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
                },
                model: {
                    pageSize: that.options.pageSize
                },
                listeners: that.options.listeners
            },
            dataFunction: getEmailFunction(that),
            dataLengthFunction: function() { return account.accountSummary ? account.accountSummary.totalMessageCount : 0; }
        };

        that.pager = unicon.batchedpager(that.locate("emailList"), batchOptions);

        // The 'accountSummary' key indicates we obtained email info successfully
        if (account.accountSummary) {

            that.refresh = function() {
                showLoadingMessage(that);
                clearCache = "true";  // Server-side cache
                that.cache = [];      // Client-side cache
                that.pager.refreshView();
                that.locate("unreadMessageCount").html(account.accountSummary.unreadMessageCount);
                showEmailList(that);
            };

            var doDelete = function(data) {
                showLoadingMessage(that);
                var ajaxOptions = {
                    url: options.deleteUrl,
                    type: "POST",
                    data: data,
                    dataType: "json",
                    error: function(request, textStatus, errorThrown) {
                        showErrorMessage(that, request.status);
                    },
                    success: function(data) {
                        if (data.errorMessage != null) {
                            showErrorMessage(that, 900, data.errorMessage);
                        }
                        that.refresh();
                    }
                };
                $.ajax(ajaxOptions);
            };

            var doToggleSeen = function(data, seenValue) {
                showLoadingMessage(that);
                data.push({name:'seenValue', value:seenValue});
                var ajaxOptions = {
                    url: options.toggleSeenUrl,
                    type: "POST",
                    data: data,
                    dataType: "json",
                    error: function(request, textStatus, errorThrown) {
                        showErrorMessage(that, request.status);
                    },
                    success: function(data) {
                        if (data.errorMessage != null) {
                            showErrorMessage(that, 900, data.errorMessage);
                        }
                        that.refresh();
                    }
                };
                $.ajax(ajaxOptions);
            };

            that.deleteSelectedMessages = function() {
                if (that.locate("emailRow").find("input[checked='true']").size() === 0) {
                    alert(that.options.jsMessages['noMessagesSelected']);
                    return;
                }
                if (confirm(that.options.jsMessages['deleteSelectedMessages'])) {
                    doDelete(that.locate("inboxForm").serializeArray());
                }
            };

            that.deleteShownMessage = function() {
                if (confirm(that.options.jsMessages['deleteMessage'])) {
                    doDelete(that.locate("messageForm").serializeArray());
                }
            };

            that.toggleSelectAll = function() {
                var chk = $(this).attr("checked");
                that.locate("selectMessage").attr("checked", chk);
            }

            that.locate("refreshLink").click(that.refresh);
            if (account.accountSummary.deleteSupported) {
                that.locate("deleteMessagesLink").click(that.deleteSelectedMessages);
                that.locate("deleteMessageButton").click(that.deleteShownMessage);
            } else {
                var anchor = that.locate("deleteMessagesLink");
                anchor.find("span").html(that.options.jsMessages['deleteNotAvailable']);
                anchor.find("span").addClass("fl-text-silver");
                anchor.attr("title", that.options.jsMessages['deleteNotAvailableTitle']);
            }
            that.locate("returnLink").click(function(){ showEmailList(that); });
            that.locate("markMessageReadButton").click(function(){ doToggleSeen(that.locate("messageForm").serializeArray(), 'true'); });
            that.locate("markMessageUnreadButton").click(function(){ doToggleSeen(that.locate("messageForm").serializeArray(), 'false'); });
            that.locate("inboxLink").attr("href", account.inboxUrl);

            that.locate("selectAll").live("click", that.toggleSelectAll);

            that.locate("unreadMessageCount").html(account.accountSummary.unreadMessageCount);
            if(account.spaceUsed=="-1"){
            	that.locate("stats").remove();
            }
            if (account.emailQuotaUsage <= 0 || account.emailQuotaLimit <= 0) {
	            that.locate("stats").remove();
            } else {
                that.locate("emailQuotaUsage").html(account.emailQuotaUsage);
                that.locate("emailQuotaLimit").html(account.emailQuotaLimit);
            }
            showEmailList(that);

        }

        return that;

    };

    fluid.defaults("jasig.EmailBrowser", {
        accountSummaryUrl: null,
        messageUrl: null,
        deleteUrl: null,
        toggleSeenUrl: null,
        pageSize: 10,
        batchSize: 20,
        selectors: {
            refreshLink: ".refresh-link",
            deleteMessagesLink: ".delete-link",
            returnLink: ".return-link",
            inboxForm: "form[name='inboxForm']",
            messageForm: "form[name='messageForm']",
            deleteMessageButton: ".delete-message-button",
            markMessageReadButton: ".mark-read-button",
            markMessageUnreadButton: ".mark-unread-button",
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
            inboxLink: ".inbox-link",
            emailQuotaUsage: ".email-quota-usage",
            emailQuotaLimit: ".email-quota-limit",
            stats: ".stats"
        },
        listeners: {},
        jsErrorMessages: {'default': 'Server Error'},
        markMessagesAsRead: true
    });

})(jQuery, fluid);
