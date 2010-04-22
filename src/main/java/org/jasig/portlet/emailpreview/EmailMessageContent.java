package org.jasig.portlet.emailpreview;

public class EmailMessageContent {

    private String content;
    private boolean isHtml;
    
    public EmailMessageContent() { }
    
    public EmailMessageContent(String content, boolean isHtml) {
        this.content = content;
        this.isHtml = isHtml;
    }
    
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public boolean isHtml() {
        return isHtml;
    }
    public void setHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }
    
    
    
}
