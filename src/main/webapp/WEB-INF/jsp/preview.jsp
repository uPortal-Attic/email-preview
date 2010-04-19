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

<link type="text/css" rel="stylesheet" href="<c:url value="/css/email.css"/>"/>

<portlet:renderURL var="refreshUrl"><portlet:param name="action" value="preview"/></portlet:renderURL>

<c:set var="totalMessages" value="${accountInfo.totalMessageCount}"/>
<c:set var="unreadMessageCount" value="${accountInfo.unreadMessageCount}"/>
<c:set var="unreadMessages" value="${accountInfo.messages}"/>

<div class="portlet-font">

    <p>
        <c:set var="newMessageText">Inbox(${unreadMessageCount} new messages)</c:set>
        <c:choose>
            <c:when test="${empty model.inboxUrl}">
                ${ newMessageText }
            </c:when>
            <c:otherwise>
                <a href="${model.inboxUrl}" target="_blank">${ newMessageText }</a>
            </c:otherwise>
        </c:choose>
        | <a href="${ refreshUrl }">Refresh</a>
    </p>
    
    <c:choose>
        <c:when test="${ totalMessages > 1 }">
            <table cellpadding="3" cellspacing="0" class="email-portlet-table portlet-font">
                <tr>
                    <th class="portlet-section-header">Subject</th>
                    <th class="portlet-section-header">Sender</th>
                    <th class="portlet-section-header">Date Sent</th>
                </tr>
                <c:forEach items="${unreadMessages}" var="message" varStatus="status">
                    <tr class="${ status.count % 2 eq 0 ? 'portlet-section-alternate' : '' } ${ message.unread ? 'unread' : '' }">
                        <c:choose>
                            <c:when test="${empty model.inboxUrl}">
                                <td>${ message.subject }</td>
                            </c:when>
                            <c:otherwise>
                                <td>
                                    <a href="${model.inboxUrl}" target="_blank">${ message.subject }</a>
                                </td>
                            </c:otherwise>
                        </c:choose>
                        <td>${ message.sender }</td>
                        <td>
                            <fmt:formatDate value="${message.sentDate}" pattern="MM/dd/yy hh:mm a"/>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </c:when>
        <c:otherwise>
            <div>
                <p class="portlet-msg-info">
                    You have <strong>no email messages</strong> in your inbox at this time. 
                    <c:if test="${!empty model.inboxUrl}">
                        <a href="${model.inboxUrl}" target="_blank">Go To Your Inbox</a>
                    </c:if>
                </p>
            </div>
        </c:otherwise>
    </c:choose>

</div>