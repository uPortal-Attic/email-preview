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
var jasig = jasig || {};

(function($, fluid) {

	var ischecked = 0;

	function selectInputMessage(that, element) {
		if (element.firstChild.checked) {
		ischecked++;
		// console.log(ischecked + " checked " );
		} else {
		ischecked--;
        	// console.log(ischecked + " unchecked " );
        	}
        	if (ischecked === 0) {
                	$('.toolbar-middle').slideUp( "fast" );
        	} else {
                	$('.toolbar-middle').slideDown( "fast" );
        	}
	}

	function hideSupressToolbar() {
                $('.toolbar-middle').slideUp( "fast" );
                // console.log("ischecked = "+ ischecked);
                ischecked = 0;
                // console.log("reinit ischecked = "+ ischecked);
                return ischecked;
	}

	function removeActiveClassOnEmailList() {
        	$( ".email-row.active" ).each(function() {
                	$( this ).removeClass("active");
        	});
        	$(".email-row .subject a").each(function() {
                	$( this ).attr( "aria-expanded", "false" );
        	});
	}

	function hideAndShowRightMessage() {
        	if ($('.email-container').hasClass('show-left-menu')) {
                	$('.email-container').removeClass('show-left-menu');
        	}
                       
        	switch ($('.email-container').hasClass('show-right-menu')) {
                	case true:
                                /* there is already a message displayed */
                                /* hide it */
                                $('.email-container').toggleClass('show-right-menu');
                                if (window.matchMedia("(min-width: 768px)").matches) {
                                /* wait a little then show the new one on tablet and desktop */
                                setTimeout(function(){ $('.email-container').addClass('show-right-menu'); }, 500);
                                } else {
                                /* do not wait on mobile */
                                setTimeout(function(){ $('.email-container').addClass('show-right-menu'); }, 5);
                                }
                	break;
                	case false:
                                setTimeout(function(){ $('.email-container').toggleClass('show-right-menu'); }, 5);
                                break;
        	}
	}
                
	function hideAndShowMessage() {
        	var linkMessages = $('#email-list-table .right-content-email-toggle .subject');
        	for (var i=0; i < linkMessages.length; i++) {
        		// console.log("eventattached");
                	linkMessages[i].addEventListener("click", hideAndShowRightMessage);
        	}
	}


	
	// Remember these items so we can access the cache quickly & accurately
	var lastRequestedStart;
	var lastRequestedSize;

	var displayMessage = function(that, element) {
    
        if (!that.options.allowRenderingEmailContent) {
            return false;
        }
        
        // display the loading message while we retrieve the desired message
        showSpinner(that);

        // get the message
        var messageId = $(element).attr("messageId");
        var message = getMessage(that, messageId);

      if (message) {
        // update the individual message display with our just-retrieved message
        var html = message.content.html ? message.content.contentString : "<pre>" + message.content.contentString + "</pre>";
        that.container.find(".message-content").html(html);
        that.container.find(".email-message .subject").html(message.subject);
	that.container.find(".email-message .from").html(message.sender);
        that.container.find(".email-message .sentDate").html(message.sentDateString);
    	that.container.find(".email-message .toRecipients").html(message.toRecipients);
    	that.container.find(".email-message .ccRecipients").html(message.ccRecipients);
    	that.container.find(".email-message .bccRecipients").html(message.bccRecipients);
    	
    	if (that.container.find(".email-message .bccRecipients").text() == ""){
    		that.container.find(".email-message .bccInfo").hide();
    	} else {
    		that.container.find(".email-message .bccInfo").show();
    	}

        that.container.find(".email-message .message-uid").val(message.messageId);

        
        // Mark messages read?
        if (that.options.markMessagesAsRead || !message.unread) {
            that.locate("markMessageReadButton").hide();
            that.locate("markMessageUnreadButton").show();
            // console.log("markmessageunread");
        } else {
            that.locate("markMessageReadButton").show();
            that.locate("markMessageUnreadButton").hide();
            // console.log("markmessageread Button");
        }

        // Access the messageObject in cache
        var mostRecentCache = that.cache[lastRequestedStart][lastRequestedSize];
        var cacheIndex = -1;
        for (var i in mostRecentCache) {
        	var msg = mostRecentCache[i];
        	if (msg.messageId === messageId) {
        		cacheIndex = parseInt(i);
        		if (msg.unread && that.options.markMessagesAsRead) {
        	        // Update the display to reflect the new state of the SEEN flag
                           msg.unread = false;
                           // console.log("change selected message to unread");
                           // console.log(JSON.stringify(msg.messageId));
                           var unreadCount = parseInt(that.locate("unreadMessageCount").html());
                           if (unreadCount && unreadCount > 0) {
                               that.locate("unreadMessageCount").html(unreadCount - 1);
                           }                    
                           that.pager.refreshView(); 
        		}
                        // Update the display to show the actual selected message in email List
                           removeActiveClassOnEmailList();
                           var selectedUnreadMessageDisplayed = $(".email-row").find("span.subject[messageId='" + msg.messageId + "']");
                           selectedUnreadMessageDisplayed.closest("tr").addClass( "active" );
                           selectedUnreadMessageDisplayed.children().attr({'aria-expanded': 'true'});
                           // console.log("focus3");
                           // keyboard navigation : wait a little for the display of the selected message then move the focus on selected message
                           setTimeout(function(){ $('#right-content-email').focus(); }, 1000);
                           
                break;
            }
        }
        
        if (cacheIndex != -1) {
        	// Load the previous link...
        	if (cacheIndex > 0) {
        		var previousMsg = mostRecentCache[cacheIndex - 1];
    	        $(".email-message .previous-msg").attr("messageId", previousMsg.messageId);
    	        $(".email-message .previous-msg").show();
        	} else {
            	$(".email-message .previous-msg").hide();
        	}
        	// Load the next link...
        	if (cacheIndex < mostRecentCache.length - 1) {
        		var nextMsg = mostRecentCache[cacheIndex + 1];
    	        $(".email-message .next-msg").attr("messageId", nextMsg.messageId);
    	        $(".email-message .next-msg").show();
        	} else {
            	$(".email-message .next-msg").hide();
        	}
        }

        // show the message display
        showEmailMessage(that);
      }
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
    var getEmail = function(that, start, size, sortKey, sortDir, folderName) {
        //if (that.cache[start] && that.cache[start][size]) {
        //    return that.cache[start][size];
        //}

        $.ajax({
            url: that.options.accountSummaryUrl,
            async: false,
            data: { pageStart: start, numberOfMessages: size, forceRefresh: clearCache, inboxFolder: folderName },
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
        return function(start, size, sortKey, sortDir, folderName) {
            return getEmail(that, start, size, sortKey, sortDir, folderName);
        };
    };

    var getMessage = function(that, messageId) {
        var message;
        $.ajax({
            url: that.options.messageUrl,
            async: false,
            data: { messageId: messageId },
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
        console.log("showEmailList"); 
        that.locate("loadingMessage").hide();
        that.locate("spinner").hide();
        that.locate("wrapper").show();
        that.locate("emailMessage").show();
        that.locate("errorMessage").hide();
        that.locate("emailList").show();
        that.container.find(".toolbar-middle").hide();
        hideSupressToolbar();
        var p = $( "#right-content-email" );
        var position = p.position();
        console.log( "right-content-email left: " + position.left + ", right-content-email top: " + position.top );
        //console.log("attachEvent");
        /*attachAllEvent();*/
    };

    var showLoadingMessage = function(that) {
        console.log("showLoadingMessage");
        that.locate("spinner").hide();
        that.locate("wrapper").hide();
        that.locate("emailList").hide();
        that.locate("emailMessage").hide();
        that.locate("errorMessage").hide();
        that.locate("loadingMessage").show();
    };

    var showSpinner = function(that) {
        console.log("showSpinner");
        $(".outer-spinner").fadeToggle( "fast", "linear" ); /* Show() is buggy on safari, chrome and edge in this special case, not FF - Show the spinner */
        console.log("showSpinner fired");
        that.locate("wrapper").show();
        that.locate("emailList").show();
        that.locate("emailMessage").show();
        that.locate("errorMessage").hide();
        that.locate("loadingMessage").hide();
    };

    var showEmailMessage = function(that) {
        console.log("showEmailMessage");
        that.locate("loadingMessage").hide();
        that.locate("errorMessage").hide();
        that.locate("spinner").delay( 601 ).fadeToggle( "slow", "linear" ); /* Show() is buggy on safari, chrome and edge in this special case, not FF - Hide the spinner */
        that.locate("wrapper").show()
        that.locate("emailList").show();
        that.locate("emailMessage").show();
        var cont = $( ".email-container" );
        var position1 = cont.offset();
        var positionAfterSticky = position1.top - 90;
        var position2 = cont.scrollTop();
        // console.log ("offset = "+ position1.top + "scrolltop = " + position2 );
        /* if the user is at the end of emaillist and select a message, scroll to selected message */
        $('html, body').animate({scrollTop: positionAfterSticky});
        // console.log("focus1");
    };

    var showErrorMessage = function(that, httpStatus, customMessage) {
        console.log("showErrorMessage");
        that.locate("spinner").hide();
        that.locate("loadingMessage").hide();
        that.locate("wrapper").hide();
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
                console.log("remove row");
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
                strings: { last: " "},
                summary: {
                    type: "fluid.pager.summary",
                    options: {
                        // Override of unlocalized dinamic string in fluid : see at the end of preview.jsp in fluid options
                        message: that.options.fluidPagerSummaryOverride
                    }
                },
                columnDefs: [
                    { key: "selectlabel", valuebinding: "*.select",
                        components: function(row, index) {
                            return {
                                decorators: [
                                    { attrs: { 'for': "\${*.messageId}" } }
                                ]
                            }
                        }
                    },
                    { key: "select", valuebinding: "*.select",
                        components: function(row, index) {
                            return {
                                markup: '<input type="checkbox" class="select-message" name="selectMessage" value="\${*.messageId}" id="\${*.messageId}"/>',
                                decorators: [
                                    { attrs: { messageId: '\${*.messageId}' } },
                                    { type: "jQuery", func: "click",
                                          args: function(){ selectInputMessage(that,this); }
                                      },
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
                                  markup: "<span>\${*.subject}</span>",
                                  decorators: [
                                      { attrs: { messageId: '\${*.messageId}' } },
                                      { type: "addClass", classes: getClasses(index, row) }
                                  ]
                            }
                        }
                    },
                    { key: "subject-link", valuebinding: "*.subject",
                        components: function(row, index) {                          
                            if (that.options.allowRenderingEmailContent) {
                              return {
                                  markup: "<a href=\"javascript:void(0);\" aria-controls=\"right-content-email\" aria-flowto=\"right-content-email\" aria-expanded=\"false\">\${*.subject}</a>",
                                  decorators: [
                                      { attrs: { messageId: '\${*.messageId}' } },
                                      { type: "jQuery", func: "click",
                                          args: function(){
                                                   // remove previous active class
                                                   removeActiveClassOnEmailList();
                                                   // set active class
                                                   $(this).closest("tr").addClass( "active" );
                                                   // set accessibility attribute
                                                   $(this).children().attr({'aria-expanded': 'true'});
                                                   // call for selected message
                                                   displayMessage(that, this);
                                                   // close and open new message
                                                   hideAndShowRightMessage();
                                                   // console.log("focus2");
                                                }
                                      },
                                      { type: "addClass", classes: getClasses(index, row) }
                                  ]
                              }
                            }
                        }
                    },
                    { key: "email-link", valuebinding: "*.messageId",
                        components: function(row, index) {
                            if (that.options.allowRenderingEmailContent) {
                                return {
                                  decorators: [
                                      { attrs: { messageId: '\${*.messageId}' } },
                                      { type: "jQuery", func: "click",
                                          args: function(){ displayMessage(that, this); }
                                      },
                                      { type: "addClass", classes: getClasses(index, row) }
                                  ]
                              }
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
            dataFunction: getEmailFunction(that, that.options.folderName),
            dataLengthFunction: function() { return account.accountSummary ? account.accountSummary.totalMessageCount : 0; }
        };
        that.pager = unicon.batchedpager(that.locate("emailList"), batchOptions);

        // Notify the server of changes to pageSize so they can be remembered and update the container min-height class
        that.pager.pager.events.initiatePageSizeChange.addListener(function(newPageSize) {
            // console.log("newPageSize: "+ newPageSize);
            /* if #right-content-email has .emailpreview-container-min-height-XX class remove it */
            $("#right-content-email").removeClass (function (index, css) {
                  return (css.match (/\bemailpreview-container-min-height-\S+/g) || []).join(' ');
            });
            /* Set .emailpreview-container-min-height-[newPageSize] class on #right-content-email */
            $("#right-content-email").addClass( "emailpreview-container-min-height-"+newPageSize );

            $.post(that.options.updatePageSizeUrl, {"newPageSize": newPageSize});
        });


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

            var getFoldersList = function() {
	        	showLoadingMessage(that);
	        	var ajaxOptions = {
	                url: options.inboxFolderUrl,
	                type: "POST",
	                data: {} ,
	                dataType: "json",
                        success: function(response){  	                	
		                var select = that.locate("allFolders");	
		                $("option", select).remove();
		                if (select.prop) {
		                	var options = select.prop('options');
		                }
		                else {
		                	 var options = select.attr('options');
		                }
		                var selected;
		                $.each(response, function(index,response) {
		                	if (index=="selected"){
		                		selected = response;
		                	}
		                	else{
		                    	options[options.length] = new Option(index,response);
		                    }
		                });
		                // Sort by name
		                select.html(select.children("option").sort(function (a, b) {
		                  return a.text == b.text ? 0 : a.text < b.text ? -1 : 1;
		                }));
		                select.val(selected);
                                          
		                $(".styled-select .ui-btn-text").html(selected);
                                // Set folder name in h3
                                $(".navbar-brand span").html(selected);
		                //$("option:contains(':unread')").css("font-weight","bold");	
		                $("option:contains(':unread')").html(function(i, text) {
		                    return text.replace(/:unread/g, '');
		                });
                                // transform select into a list of button
                                $("#allFolders").quickselect({
                                    activeButtonClass: 'active',
                                    breakOutAll: true,
		                  //breakOutValues: ['Breakfast', 'Lunch', 'Dinner'],
		                    buttonClass: 'btn btn-default',
		                    selectDefaultText: 'Other',
		                    wrapperClass: 'col-xs-12 btn-group-vertical'
	                        });

                                // Attach event on click on folder button to trigger an event and the value of the selected folder value to the select
                                $(".quickselect__btn").on("click", function(event) {
                                   // event.preventDefault();
                                   $(".quickselect__btn.active").removeClass("active");
                                   $(this).addClass("active");
                                   var selectedFolder = $(this).attr("data-quickselect-value");
                                   //console.log(selection);
                                   $("#allFolders").trigger( "selected:folder:change", selectedFolder);
                                   return false;
                                });
		                console.log("list completed");
	                }

	            };
	    		$.ajax(ajaxOptions);
	        };

	    

            that.deleteSelectedMessages = function() {
                if (that.locate('emailRow').find('input:checked').size() === 0) {
                    alert(that.options.jsMessages['noMessagesSelected']);
                    return;
                }
                console.log("delete message");
                if (confirm(that.options.jsMessages['deleteSelectedMessages'])) {
                    doDelete(that.locate("inboxForm").serializeArray());
                }
            };

            that.deleteShownMessage = function() {
                if (confirm(that.options.jsMessages['deleteMessage'])) {
                    $('.email-container').toggleClass('show-right-menu');
                    doDelete(that.locate("messageForm").serializeArray());
                }
            };

            that.toggleSelectAll = function() {
                // var chk = $(this).attr("checked");
                var chk2 = $(this).prop('checked');
                // console.log("chk= "+ chk);
                // console.log("chk2= "+ chk2);
                if (chk2 == true) {
                that.locate("selectMessage").prop("checked", true);
                } else {
                that.locate("selectMessage").removeAttr("checked");
                }
            }

            that.toggleSelectAndHideToolbar = function() {
                that.locate("selectMessage").removeAttr("checked");
                that.locate("selectAll").removeAttr("checked");
                that.container.find(".toolbar-middle").hide();
                hideSupressToolbar();  
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
            /*that.locate("returnLink").click(function(){ that.refresh(); // showEmailList(that);
                                               });*/
            that.locate("returnLink").click(function(){ that.toggleSelectAndHideToolbar;removeActiveClassOnEmailList(); showEmailList(that);setTimeout(function(){ $('#content-middle-area').focus(); }, 200); });
            that.locate("previousMsg").click(function(){ displayMessage(that, this); });
            that.locate("nextMsg").click(function(){ displayMessage(that, this); });
            that.locate("markMessageReadButton").click(function(){ doToggleSeen(that.locate("messageForm").serializeArray(), 'true'); });
            that.locate("markMessageUnreadButton").click(function(){ doToggleSeen(that.locate("messageForm").serializeArray(), 'false'); });
            that.locate("allFolders").ready(function(){ getFoldersList();});
            that.locate("allFolders").on("selected:folder:change",
            	function(event, param1){
                    //console.log("selectedButton = "+ that.locate("selectedButton").attr("data-quickselect-value"));
                    //console.log("selectedFolder = "+ param1);
                    clearCache = "true";
	            	getEmail(that, 0, that.options.batchSize, undefined, undefined, param1);
	            	location.reload();
            	});    
            that.locate("inboxLink").attr("href", account.inboxUrl);
            $("#right-content-email").addClass( "emailpreview-container-min-height-"+ that.options.pageSize );
            //Mobile view fixes
            $("#mobileSelect").find("span.ui-btn-text").html(that.options.pageSize);
            console.log(that.pager);
            $("#results").change(function(){
                that.pager.pager.model.pageSize=$("#results option:selected").val();
                $.post(that.options.updatePageSizeUrl, {"newPageSize": that.pager.pager.model.pageSize});
                that.pager.refreshView();
            });
            //$(document).on("change",".flc-pager-summary",function() {console.log("change");console.log(this);});
            $(document).on("click", ".select-all", that.toggleSelectAll);
            $(document).on("click", ".hide-toolbar-middle", that.toggleSelectAndHideToolbar);

            that.locate("unreadMessageCount").html(account.accountSummary.unreadMessageCount);
            if (account.spaceUsed=="-1"){
            	that.locate("stats").remove();
            }
            if (account.emailQuotaUsage <= 0 || account.emailQuotaLimit <= 0) {
	            that.locate("stats").remove();
            } else {
                that.locate("emailQuotaUsage").html(account.emailQuotaUsage);
                if (account.emailQuotaUsage.split('%', 1)[0] <= 90) {
                that.locate("emailQuotaUsage").addClass("green");
                } else { that.locate("emailQuotaUsage").addClass("red");
                }
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
        inboxFolderUrl: null,
        pageSize: 10,
        batchSize: 20,
        selectors: {
            refreshLink: ".refresh-link",
            deleteMessagesLink: ".delete-link",
            returnLink: ".return-link",
            inboxForm: "form[name='inboxForm']",
            messageForm: "form[name='messageForm']",
            allFolders: "#allFolders",
            selectedButton: ".quickselect__btn.active",
            hideSuppressToolbar: ".hide-toolbar-middle",           
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
            spinner: ".outer-spinner",
            bccRecipients: ".bcc-recipients",
            ccRecipients: ".cc-recipients",
            toRecipients: ".to-recipients",
            previousMsg: ".previous-msg",
            nextMsg: ".next-msg",
            wrapper: "#outer-wrapper",
            mobileSelect: "#mobileSelect"
        },
        listeners: {},
        jsErrorMessages: {'default': 'Server Error'},
        markMessagesAsRead: true,
        fluidPagerSummaryOverride:"Page %currentPage. Viewing emails %first to %last of %total emails."
    });

})(jQuery, fluid);
