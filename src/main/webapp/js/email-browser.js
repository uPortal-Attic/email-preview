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
	
	// Remember these items so we can access the cache quickly & accurately
	var lastRequestedStart;
	var lastRequestedSize;

	var displayMessage = function(that, element) {
    
        if (!that.options.allowRenderingEmailContent) {
            return false;
        }
        
        // display the loading message while we retrieve the desired message
        showLoadingMessage(that);

        // get the message
        var messageNum = parseInt($(element).attr("messageNum"));
        var message = getMessage(that, messageNum);

        // update the individual message display with our just-retrieved message
        var html = message.content.html ? message.content.contentString : "<pre>" + message.content.contentString + "</pre>";
        that.container.find(".message-content").html(html);
        that.container.find(".email-message .subject").html(message.subject);
        that.container.find(".email-message .sender").html(message.sender);
        that.container.find(".email-message .sentDate").html(message.sentDateString);
        that.container.find(".email-message .message-uid").val(message.uid);

        // Mark messages read?
        if (that.options.markMessagesAsRead || !message.unread) {
            that.locate("markMessageReadButton").hide();
            that.locate("markMessageUnreadButton").show();
        } else {
            that.locate("markMessageReadButton").show();
            that.locate("markMessageUnreadButton").hide();
        }

        // Access the messageObject in cache
        var mostRecentCache = that.cache[lastRequestedStart][lastRequestedSize];
        var cacheIndex = -1;
        for (var i in mostRecentCache) {
        	var msg = mostRecentCache[i];
        	if (msg.messageNumber === messageNum) {
        		cacheIndex = parseInt(i);
        		if (msg.unread && that.options.markMessagesAsRead) {
        	        // Update the display to reflect the new state of the SEEN flag
                    msg.unread = false;
                    var unreadCount = parseInt(that.locate("unreadMessageCount").html());
                    if (unreadCount && unreadCount > 0) {
                        that.locate("unreadMessageCount").html(unreadCount - 1);
                    }                    
                    that.pager.refreshView();
        		}
                break;
            }
        }
        
        if (cacheIndex != -1) {
        	// Load the previous link...
        	if (cacheIndex > 0) {
        		var previousMsg = mostRecentCache[cacheIndex - 1];
    	        $(".email-message .previous-msg").attr("messageNum", previousMsg.messageNumber);
    	        $(".email-message .previous-msg").show();
        	} else {
            	$(".email-message .previous-msg").hide();
        	}
        	// Load the next link...
        	if (cacheIndex < mostRecentCache.length - 1) {
        		var nextMsg = mostRecentCache[cacheIndex + 1];
    	        $(".email-message .next-msg").attr("messageNum", nextMsg.messageNumber);
    	        $(".email-message .next-msg").show();
        	} else {
            	$(".email-message .next-msg").hide();
        	}
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
        
        // Cache the response
        that.cache[start] = that.cache[start] || [];
        that.cache[start][size] = messages;
        
    	// Remember start/size for easy cache access
    	lastRequestedStart = start;
    	lastRequestedSize = size;

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
                          
                            if (that.options.allowRenderingEmailContent) {
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
                            return {
                                  markup: "<span>\${*.subject}</span>",
                                  decorators: [
                                      { attrs: { messageNum: '\${*.messageNumber}' } },
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
                if (that.locate('emailRow').find('input:checked').size() === 0) {
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
                that.locate("deleteMessageButton").show();
            } else {
                var anchor = that.locate("deleteMessagesLink");
                anchor.find("span").html(that.options.jsMessages['deleteNotAvailable']);
                anchor.find("span").addClass("fl-text-silver");
                anchor.attr("title", that.options.jsMessages['deleteNotAvailableTitle']);
                that.locate("deleteMessageButton").hide();
            }
            that.locate("returnLink").click(function(){ showEmailList(that); });
            that.locate("previousMsg").click(function(){ displayMessage(that, this); });
            that.locate("nextMsg").click(function(){ displayMessage(that, this); });              
            that.locate("markMessageReadButton").click(function(){ doToggleSeen(that.locate("messageForm").serializeArray(), 'true'); });
            that.locate("markMessageUnreadButton").click(function(){ doToggleSeen(that.locate("messageForm").serializeArray(), 'false'); });
            that.locate("inboxLink").attr("href", account.inboxUrl);
            //Mobile view          
            that.locate("mobileSelect").find("span.ui-btn-text").html(that.options.pageSize);
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
            stats: ".stats",
            previousMsg: ".previous-msg",
            nextMsg: ".next-msg",
            mobileSelect: "#mobileSelect"
        },
        listeners: {},
        jsErrorMessages: {'default': 'Server Error'},
        markMessagesAsRead: true
    });

})(jQuery, fluid);
