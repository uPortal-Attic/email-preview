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

<c:set var="includeJQuery" value="${renderRequest.preferences.map['includeJQuery'][0]}"/>
<c:if test="${includeJQuery}">
    <script src="<rs:resourceURL value="/rs/jquery/1.3.2/jquery-1.3.2.min.js"/>" type="text/javascript"></script>
    <script src="<rs:resourceURL value="/rs/jqueryui/1.7.2/jquery-ui-1.7.2-v2.min.js"/>" type="text/javascript"></script>
</c:if>
<script src="<rs:resourceURL value="/rs/fluid/1.1.3/js/fluid-all-1.1.3.min.js"/>" type="text/javascript"></script>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/email.css"/>"/>

<c:set var="n"><portlet:namespace/></c:set>

<form id="plt-email-form" action="<portlet:actionURL><portlet:param name="action" value="updatePreferences"/></portlet:actionURL>" method="POST">

    <h2><spring:message code="editPreferences.emailSettings.title"/></h2>

    <!-- Error message will always be displayed if rendered in the markup -->
    <c:if test="${errorMessage ne null}">
        <div id="plt-email-submission-error"><p><c:out value="${errorMessage}"/></p></div>
    </c:if>

    <div class="fieldset plt-email-fieldset-settings">
        <div class="plt-email-row">
            <label><spring:message code="editPreferences.emailSettings.serverProtocol"/></label>
            <select name="protocol" id="plt-email-input-protocol" title="<spring:message code="editPreferences.emailSettings.serverProtocol.tooltip"/>">
                <c:forEach items="${protocols}" var="protocol">
                    <option<c:if test="${form.protocol eq protocol}"> selected="selected"</c:if> value="<c:out value="${protocol}"/>"><c:out value="${protocol}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="plt-email-row">
            <label><spring:message code="editPreferences.emailSettings.serverName"/></label>
            <input type="text" name="host" id="plt-email-input-server" title="<spring:message code="editPreferences.emailSettings.serverName.tooltip"/>" value="<c:out value="${form.host}"/>"/>
        </div>
        <div class="plt-email-row">
            <label><spring:message code="editPreferences.emailSettings.serverPort"/></label>
            <input type="text" name="port" id="plt-email-input-port" title="<spring:message code="editPreferences.emailSettings.serverPort.toolTip"/>" value="<c:out value="${form.port}"/>"/>
        </div>
        <div class="plt-email-row">
            <label><spring:message code="editPreferences.preferences.markMessagesAsRead"/></label>
            <input type="checkbox" name="markMessagesAsRead" title="<spring:message code="editPreferences.preferences.markMessagesAsRead.tooltip"/>" value="true"<c:if test="${form.markMessagesAsRead}"> checked="checked"</c:if>/>
        </div>
    </div>          

    <!-- Show radio buttons if multiple authenticationServices are in use -->
    <c:if test="${fn:length(authenticationServices) > 1}">
        <div class="fieldset plt-email-fieldset-verify">
            <c:if test="${authenticationServices.cachedPassword ne null}">
                <label>
                    <input id="authtype_cache" type="radio" name="authenticationServiceKey" value="cachedPassword"<c:if test="${form.authenticationServiceKey eq 'cachedPassword'}"> checked="checked"</c:if>> <spring:message code="editPreferences.emailSettings.cachedPasswordAuthN.description"/> 
                </label>
            </c:if>
            <c:if test="${authenticationServices.portletPreferences ne null}">
                <label>
                    <input id="authtype_preferences" type="radio" name="authenticationServiceKey" value="portletPreferences"<c:if test="${form.authenticationServiceKey eq 'portletPreferences'}"> checked="checked"</c:if>> <spring:message code="editPreferences.emailSettings.portletPreferencesAuthN.description"/>
                </label>
            </c:if>
        </div>
    </c:if>

    <c:if test="${authenticationServices.portletPreferences ne null}">
        <!-- Show these fields if the authService is currently 'portletPreferences' -->
        <c:set var="displayStyle" value="${form.authenticationServiceKey eq 'portletPreferences' ? '' : 'display: none;'}" />
        <div class="fieldset plt-email-fieldset-authparams plt-email-fieldset-ppauth" style="${displayStyle}">
            <div class="plt-email-row">
                <label><spring:message code="editPreferences.emailSettings.portletPreferencesAuthN.emailAddress"/></label>
                <input type="text" name="username" id="plt-email-input-email" title="<spring:message code="editPreferences.emailSettings.portletPreferencesAuthN.emailAddress.tooltip"/>" value="<c:out value="${form.additionalProperties.username}"/>"/>
                <span class="plt-email-address-suffix"><c:out value="${form.usernameSuffix}"/></span>
            </div>
            <div class="plt-email-row">
                <label><spring:message code="editPreferences.emailSettings.portletPreferencesAuthN.password"/></label>
                <input type="password" name="ppauth_password" id="plt-email-input-password" title="<spring:message code="editPreferences.emailSettings.portletPreferencesAuthN.password.tooltip"/>" value="<c:out value="${unchangedPassword}"/>"/>
            </div>
        </div>
    </c:if>
            
    <h2><spring:message code="editPreferences.preferences.title"/></h2>

    <div class="fieldset plt-email-fieldset-settings">
        <div class="plt-email-row">
            <label><spring:message code="editPreferences.preferences.show"/></label>
            <select name="defaultView" title="<spring:message code="editPreferences.preferences.show.tooltip"/>">
                <option<c:if test="${renderRequest.preferences.map['defaultView'][0] eq 'rollup'}"> selected="selected"</c:if> value="rollup"><spring:message code="editPreferences.preferences.show.rollup"/></option>
                <option<c:if test="${renderRequest.preferences.map['defaultView'][0] eq 'preview'}"> selected="selected"</c:if> value="preview"><spring:message code="editPreferences.preferences.show.preview"/></option>
            </select>
            <label><spring:message code="editPreferences.preferences.onLogin"/></label>
        </div>
        <div class="plt-email-row">
            <label><spring:message code="editPreferences.preferences.focusOnPreview"/></label>
            <input type="checkbox" name="focusOnPreview" title="<spring:message code="editPreferences.preferences.focusOnPreview.tooltip"/>" value="true"<c:if test="${renderRequest.preferences.map['focusOnPreview'][0] eq 'true'}"> checked="checked"</c:if>/>
        </div>
    </div>          

    <input type="submit" name="submit_email" value=" <spring:message code="editPreferences.buttonGroup.saveSettings"/> " id="plt-email-input-submit"/>
    <a id="plt-email-input-cancel" href="<portlet:renderURL portletMode="view"/>"><spring:message code="editPreferences.buttonGroup.cancel"/></a>

</form>

<script type="text/javascript">

    var ${n} = {};
    ${n}.jQuery = jQuery<c:if test="${includeJQuery}">.noConflict(true)</c:if>;
    ${n}.fluid = fluid;
    fluid = null;
    fluid_1_1 = null;

    (function ($, fluid) {
        ${n}.pltEmailForm = function (container, options) {
    
            var that = fluid.initView("${n}.pltEmailForm", container, options);
    
            /*
             * Binds the events needed and puts the focus on the email input. 
             * It also shows the error message if it is included in the markup. Animated with a delay to make sure the users sees the message.
             */
            var initialize = function () {
                bindEvents();
                $(that.options.selectors.input_email).focus();
                
                if(that.options.disableProtocol){
                    $('#plt-email-input-protocol').addClass('disabled').attr('disabled',true);
                }
                if(that.options.disableHost){
                    $('#plt-email-input-server').addClass('disabled').attr('disabled',true);
                }
                if(that.options.disablePort){
                    $('#plt-email-input-port').addClass('disabled').attr('disabled',true);
                }
                if(that.options.disableAuthService){
                    $('#authtype_cache').addClass('disabled').attr('disabled',true);
                    $('#authtype_preferences').addClass('disabled').attr('disabled',true);
                }
                
                setTimeout(
                    function(){
                        $(that.options.selectors.submission_error).slideDown();
                    },
                    500
                );
            };//end:function
            
            /*
             * Function binds events and listeners for form submission and help.
             */
            bindEvents = function () {
                $(that.options.selectors.help).click(function() {
                    $(this).find('span').fadeIn(500);   
                });
                
                $(that.options.selectors.submit_button).click(function() {
                    if(validateForm()){
                        $(that.container).submit(); 
                    } else {
                        return false; 
                    }
                });
                
                $(that.options.selectors.authtype_cache).click(function() {
                    $(that.options.selectors.fieldset_preferences).slideUp(400);
                    $(that.options.selectors.input_email).val($(that.options.selectors.input_current_email).val());
                    $(that.options.selectors.input_password).val($(that.options.selectors.input_current_password).val());
                });
                
                $(that.options.selectors.authtype_preferences).click(function() {
                    $(that.options.selectors.fieldset_preferences).slideDown(400);
                });
                
            };//end:function
            
            var validateForm = function (){
                
                var error_msg;
                var culprit;
                
                /* Tests for existance of at least one valid character */
                var validRegExp = /^([a-zA-Z0-9_.-])+$/;
                                    
                /* Hide error bar initially in case a previous error occured */
                $(that.options.selectors.input_error).slideUp(200);
                
                /*
                 * Stuff we always check
                 */
                
                /* Check for empty incoming server */
                if ($(that.options.selectors.input_imap).search(validRegExp) == '-1'){
                    error_msg = 'Please provide your incoming email server.';
                    culprit = that.options.selectors.input_imap;    
                }            
                /* Check for empty port */
                if ($(that.options.selectors.input_port).val().search(/^\d+$/) == '-1'){
                    error_msg = 'Please specify a valid port number for your email account.';
                    culprit = that.options.selectors.input_port;    
                }

                /*
                 * Stuff we check when preferences auth
                 */
                 
                if ($("input[@name='authtype']:checked").val()=="portletPreferences") {
                    /* Validate Email */
                    if ($(that.options.selectors.input_email).val().search(validRegExp) == '-1'){
                        error_msg = 'Please input a valid email address.';
                        culprit = that.options.selectors.input_email;   
                    }
                }

                /* Show error */
                if (error_msg) {
                    displayError(error_msg,culprit);
                    return false;
                }
                else {
                    /* Else Submit Form */
                    return true;
                } 

            };//end:function
            
            /*
             * Function gets passed a text message and the id of the input that caused the problem. The error box is given the message and is animated.
             * In addition, the "culprit" is highlighted to help the user identify their error. Focus is given to that element for quick fixes.
             */
            var displayError = function (error_msg, culprit) {
                
                $(that.options.selectors.input_error).remove();
                
                var error_html = '<div class="'+that.options.selectors.input_error+'">' + error_msg + '</div>';
                
                $(that.container).find('input').removeClass(that.options.selectors.input_error_highlight);
                $(culprit).addClass(that.options.selectors.input_error_highlight);
                
                $(culprit).focus();
                
                if(culprit == that.options.selectors.input_email || culprit == that.options.selectors.input_password){
                    $(that.options.selectors.fieldset_preferences).slideDown(400);
                }   
                                            
                $(that.container).prepend(error_html);
                $(that.options.selectors.input_error).slideDown(400);
                            
            };//end:function
            
            initialize();
            return that; 
    
        };//function:end
    
        fluid.defaults("${n}.pltEmailForm", {
            // verify_email : "true",
            selectors: {
                submit_button: "#plt-email-input-submit",
                cancel_button: "#plt-email-input-cancel",
                input_email: "#plt-email-input-email",
                input_password: "#plt-email-input-password",
                input_port: "#plt-email-input-port",
                input_imap: "#plt-email-input-server",
                help: ".help",
                input_error: ".plt-email-input-error",
                input_error_highlight: "plt-email-input-error-highlight",  // No initial '.' b/c this one is used with addClass/removeClass
                submission_error: "#plt-email-submission-error",
                authtype_cache: "#authtype_cache",
                authtype_preferences: "#authtype_preferences",
                fieldset_preferences: ".plt-email-fieldset-ppauth"
            },
            disableProtocol: false,
            disableHost: false,
            disablePort: false,
            disableAuthService: false
        });
    
    })(${n}.jQuery,${n}.fluid);

    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;
        var fluid = ${n}.fluid;
        var options = {
            disableProtocol: <c:out value="${disableProtocol}"/>,
            disableHost: <c:out value="${disableHost}"/>,
            disablePort: <c:out value="${disablePort}"/>,
            disableAuthService: <c:out value="${disableAuthService}"/>
        };
        ${n}.pltEmailForm($('#plt-email-form'), options);
    });

</script>
