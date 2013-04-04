/**
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
package org.jasig.portlet.emailpreview;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Describes an Exchange folder.  This is a minimal implementation needed for Email Preview to 
 * be able to identify and list the user's email folders in the UI.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

public class ExchangeFolderDto extends Folder {

    final String id;
    int unreadMessageCount;
    String foldername;
    int messageCount;
    int childFolderCount;

    public ExchangeFolderDto(String id, String foldername, int messageCount, int unreadMessageCount) {
        super(null);
        this.id = id;
        this.foldername = foldername;
        this.messageCount = messageCount;
        this.unreadMessageCount = unreadMessageCount;
    }

    @Override
    public String getName() {
        return foldername;
    }

    public void setFoldername(String foldername) {
        this.foldername = foldername;
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public Folder getParent() throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public boolean exists() throws MessagingException {
        return true;
    }

    @Override
    public Folder[] list(String pattern) throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public char getSeparator() throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public int getType() throws MessagingException {
        return Folder.HOLDS_MESSAGES | (childFolderCount > 0 ? HOLDS_FOLDERS : 0);
    }

    @Override
    public boolean create(int type) throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public boolean hasNewMessages() throws MessagingException {
        return unreadMessageCount > 0;
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public boolean delete(boolean recurse) throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public boolean renameTo(Folder f) throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void open(int mode) throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void close(boolean expunge) throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public Flags getPermanentFlags() {
        return null;
    }

    @Override
    public int getMessageCount() throws MessagingException {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    @Override
    public Message getMessage(int msgnum) throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public Message[] expunge() throws MessagingException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public synchronized int getUnreadMessageCount() throws MessagingException {
        return unreadMessageCount;
    }

    public String getId() {
        return id;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public int getChildFolderCount() {
        return childFolderCount;
    }

    public void setChildFolderCount(int childFolderCount) {
        this.childFolderCount = childFolderCount;
    }

}
