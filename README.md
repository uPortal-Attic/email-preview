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

## Configuration

See also the [legacy documentation in the external wiki][].

### Java Properties

Some configuration settings for the Email Preview portlet are managed in Java properties files that
are loaded by a Spring `PropertySourcesPlaceholderConfigurer`.  (Other settings are data, managed in
the "portlet publication record" a.k.a. `portlet-definition.xml` file

The properties files that are sourced by Spring are:

  - `classpath:configuration.properties`
  - `file:${portal.home}/global.properties`
  - `file:${portal.home}/email-preview.properties`

For a definitive, comprehensive list of these settings you must look inside `configuration.properties`.
(This `README` may be incomplete and/or out of date.)

#### The `portal.home` Directory

uPortal version 5 uses a directory called `portal.home` for properties files that live outside of
-- and have the ability to _override_ properties files within-- the webapp in Tommcat.  Please
review the [README file for uPortal-Start][] for more information on this sytem.

The Notification portlet sources the shared `global.properties` file, as well as it's own (private)
file called `email-preview.properties` in the `portal.home` directory.

#### Using Encrypted Property Values

Within the properties files that are sourced by Spring, you may optionally provide sensitive
configuration items -- such as database passwords -- in encrypted format.  Use the
[Jasypt CLI Tools][] to encrypt the sensitive value, then include it in a `.properties` file
like this:

```
hibernate.connection.password=ENC(9ffpQXJi/EPih9o+Xshm5g==)
```

Specify the encryption key using the `UP_JASYPT_KEY` environment variable.

## Contributing

This project follows the [uPortal Contributing Guidelines](https://github.com/Jasig/uPortal/blob/master/CONTRIBUTING.md).
