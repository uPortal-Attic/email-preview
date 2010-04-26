package org.jasig.portlet.emailpreview;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class EmailMessageContent {

    private String content;
    private boolean isHtml;

    /**
     * Default constructor
     */
    public EmailMessageContent() {
    }

    /**
     * 
     * @param contentString
     * @param isHtml
     */
    public EmailMessageContent(String contentString, boolean isHtml) {
        this.content = contentString;
        this.isHtml = isHtml;
    }

    /**
     * 
     * @return
     */
    public String getContentString() {
        return content;
    }

    /**
     * 
     * @param content
     */
    public void setContentString(String content) {
        this.content = content;
    }

    /**
     * 
     * @return
     */
    public boolean isHtml() {
        return isHtml;
    }

    /**
     * 
     * @param isHtml
     */
    public void setHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }

}
