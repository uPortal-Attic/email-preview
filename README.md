# Email Preview Portlet

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jasig.portlet/email-preview/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jasig.portlet/email-preview)
[![Linux Build Status](https://travis-ci.org/Jasig/email-preview.svg?branch=master)](https://travis-ci.org/Jasig/email-preview)
[![Windows Build status](https://ci.appveyor.com/api/projects/status/ehlvw8wivw0k0hea/branch/master?svg=true)](https://ci.appveyor.com/project/ChristianMurphy/email-preview/branch/master)

The Email Preview Portlet is a JSR-268 read-only email portlet. This portlet allows a user to connect to an IMAP, POP3, or Exchange Web Services email store and view and manage contents of INBOX and other mail folders.

This is a [Sponsored Portlet](https://wiki.jasig.org/display/PLT/Jasig+Sponsored+Portlets) in the Apereo uPortal project.

Issue Tracker: <https://issues.jasig.org/browse/EMAILPLT>

License: [Apache Software License, version 2.0](LICENSE)

## Features

*   Display total message count & unread message count
*   Display the contents of your Inbox or any folder on the mail server
*   Display messages in a table, with the following information for each message
    +   Subject
    +   Sender
    +   Date Sent
    +   Whether the message has been previously viewed
    +   Whether the message contains attachments
*   Page through your messages & choose page size
*   Check for new messages ("Refresh")
*   Click on a message to view it's content
*   Display customizable Welcome message
*   Display customizable Help text
*   Provide authentication via cached credential replay or user-specified preferences
*   Uses Antisamy to protect against cross-site scripting (XSS)

## Administrator Settings

These may be configured by the administrator or omitted. The following connection settings apply to IMAP/POP integration. **Exchange Web Services support is also available.**

*   Protocol - IMAP(S) or POP(S)
*   Mail Server (host)
*   Port
*   Inbox Folder Name
*   Timeout
*   Connection Timeout
*   Inbox URL (for click-through into the webmail client)
*   Link Service
*   Authentication Service (cachePassword and/or portletPreferences)
*   Which settings, if any, users may configure (see below)

## User Settings

These may be set by users if permitted by the administrator. If any field is defined by both the admin and the user, the user's value is used.

*   Protocol (not including Exchange Web Services)
*   Mail Server (host)
*   Port
*   Mailbox folder name
*   Mark Messages Read
*   Username (portletPreferences authentication only)
*   Password (portletPreferences authentication only)
*   Show rollup (smaller) or preview (larger) on login
*   Focus on Preview

## Contributing

This project follows the [uPortal Contributing Guidelines](https://github.com/Jasig/uPortal/blob/master/CONTRIBUTING.md).
