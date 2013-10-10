/**
 * 
 */
package org.jasig.portlet.emailpreview.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author GIP RECIA 2013 - Maxime BOSSARD.
 *
 */
public class MessageUtilsTestJ {

	private static final String MESSAGE_BODY_WITHOUD_TARGET = "<html><head>" +
			"<link rel=\"shortcut icon\" href=\"/sites/all/themes/jasig3/favicon.ico\" type=\"image/x-icon\">" +
			"</head><body>" +
			"<h1>Page from jasig.org</h1>" +
			"<span class='field-content'><a href='/news/uportal-security-release'>uPortal Security Release</a></span>" +
	        "<span class='field-content'><a href='/news/aprils-uportal-community-call-now-s'>April's uPortal Community Call is now on Slideshare</a></span>" +
	        "<span class='field-content'>http://www.jasig.org/news/first-annual-general-meeting-apereo</span>" +
	        "<span class='field-content'><a href='/news/2013-apereo-jasig-fellows-call-nomi' target='test'>2013 Apereo Jasig Fellows call for nominations</a></span>" +
	        "<span class='field-content'><a href='/news/2013-apereo-jasig-fellows-call-nomi2' target=\"test\">2013 Apereo Jasig Fellows call for nominations</a></span>" +
	        "<span class='field-content'><a href=\"/news/first-annual-general-meeting-apereo2\">First Annual General Meeting of the Apereo Foundation</a></span>" +
			"</body></html>";
	
	private static final String EXPECTED_ESSAGE_BODY = "<html><head>" +
			"<link rel=\"shortcut icon\" href=\"/sites/all/themes/jasig3/favicon.ico\" type=\"image/x-icon\">" +
			"</head><body>" +
			"<h1>Page from jasig.org</h1>" +
			"<span class='field-content'><a href='/news/uportal-security-release' target=\"_new\">uPortal Security Release</a></span>" +
	        "<span class='field-content'><a href='/news/aprils-uportal-community-call-now-s' target=\"_new\">April's uPortal Community Call is now on Slideshare</a></span>" +
	        "<span class='field-content'>http://www.jasig.org/news/first-annual-general-meeting-apereo</span>" +
	        "<span class='field-content'><a href='/news/2013-apereo-jasig-fellows-call-nomi' target='test'>2013 Apereo Jasig Fellows call for nominations</a></span>" +
	        "<span class='field-content'><a href='/news/2013-apereo-jasig-fellows-call-nomi2' target=\"test\">2013 Apereo Jasig Fellows call for nominations</a></span>" +
	        "<span class='field-content'><a href=\"/news/first-annual-general-meeting-apereo2\" target=\"_new\">First Annual General Meeting of the Apereo Foundation</a></span>" +
			"</body></html>";
	
	  @Test
	  public void testAddMissingTargetToAnchors() throws Exception {
		  String result = MessageUtils.addMissingTargetToAnchors(MESSAGE_BODY_WITHOUD_TARGET);
		  
		  Assert.assertEquals(EXPECTED_ESSAGE_BODY, result);
	  }

}
