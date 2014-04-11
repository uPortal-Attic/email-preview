package org.jasig.portlet.emailpreview.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import javax.mail.MessagingException;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.jasig.portlet.emailpreview.dao.javamail.JavamailAccountDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

@RequestMapping(value="VIEW")
@Controller
public class AttachmentStoreController  {
	
	@Autowired
    private JavamailAccountDaoImpl	dao;
	
	@ResourceMapping(value = "fileDownload")
	public void getFile(ResourceRequest request, ResourceResponse response) throws IOException, MessagingException {
		
		//Get param
		String fileName = request.getParameter("fileName");
		String partNumber = request.getParameter("partNumber");
		
		byte[] bytes = new byte[0];
		String contentType = "";
		String charset = "";
		//name of the dummy files from resources
		if("demo".equals(partNumber)){				
			if("emailpreview.pdf".equals(fileName)){
				contentType = "application/pdf";
			}
			if("EmailPreviewPortlet.png".equals(fileName)){
				contentType = "image/png";
			}		
	        File file = new File(getClass().getResource("/".concat(fileName)).getFile());
	        FileInputStream fin = null;
			
			try {
	            //convert file into array of bytes
				 fin = new FileInputStream(file);
				 bytes = new byte[(int) file.length()];
				 fin.read(bytes);
				 fin.close();

	        }catch(Exception e){
	        	e.printStackTrace();
	        }			
		}else{
			int msgNumber = Integer.valueOf(request.getParameter("msgNumber"));
			HashMap<String, Object> attachmentInfos = new HashMap<String, Object>();
			
			attachmentInfos = dao.getAttachmentInfos((PortletRequest) request,fileName, partNumber, msgNumber);
	
			
			if(attachmentInfos.get("content") instanceof String){
				bytes= attachmentInfos.get("content").toString().getBytes();
			}else{
				bytes = (byte[]) attachmentInfos.get("content");
			}
			contentType = (String) attachmentInfos.get("contentType");
	        if(attachmentInfos.get("contentCharset")!=null){
	        	charset = (String) attachmentInfos.get("contentCharset");
	        }
        }
		response.setContentType(contentType);
        response.addProperty("Content-Disposition","attachment; filename=".concat(fileName));
        response.addProperty("charset",charset);

        OutputStream out = response.getPortletOutputStream();
        out.write(bytes);
        out.flush();
        out.close();}

public static void main( String[] args )
{
	FileInputStream fileInputStream=null;

    File file = new File("src/main/resources/emailpreview.pdf");

    byte[] bFile = new byte[(int) file.length()];
	System.out.print("legth" + file.length());
    try {
        //convert file into array of bytes
    fileInputStream = new FileInputStream(file);
    fileInputStream.read(bFile);
    fileInputStream.close();

    for (int i = 0; i < bFile.length; i++) {
       	System.out.print((char)bFile[i]);
        }

    System.out.println("Done");
    }catch(Exception e){
    	e.printStackTrace();
    }
}
}	
