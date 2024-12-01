package com.gs.pdfGeneration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFEncryption;

import com.google.gson.Gson;
import com.lowagie.text.DocumentException;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

public class AccDetailsPdfService {
	
	private static Properties prop;
	public static Logger log = Logger.getLogger(AccDetailsPdfService.class);
		
	public AccDetailsPdfService(){
		try{
			PropertyReader pr = new PropertyReader();
	    	prop = pr.loadPropertyFile();
	    	
			String pathSep = System.getProperty("file.separator");
	        String logpath = prop.getProperty("LOG4J_FILE_PATH");
	        String activityRoot = prop.getProperty("LOG_PATH");
			String logPropertyFile =logpath+pathSep+"log4j.properties"; 
	
			PropertyConfigurator.configure(logPropertyFile);
			PropertyReader.loadLogConfiguration(logPropertyFile, activityRoot+"/AccDetailsPdfService/", "AccDetailsPdfService.log");
		}catch(Exception e){
			System.out.println("Error : " +e.fillInStackTrace());
			log.info("Error : " +e.fillInStackTrace());
		}	
	}
	
	public String generatePdf(String accDetailsJson) {

		try {
			AccountDetail accountDetail = new Gson().fromJson(accDetailsJson, AccountDetail.class);
				
			Context context = getContext(accountDetail);
			String html = loadAndFillTemplate(context, "account_details_pdf_template");
			
	        return renderPdf(html, accountDetail.getCustomerIdentificationNumber());
        
		}catch (Exception e) {
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			
			e.printStackTrace();
			log.info("Exception occured : " +sw.toString());
			
			return "Exception occured : " +sw.toString();
		}
    }
	
	private String renderPdf(String html, String password) throws IOException, DocumentException {

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer(20f * 4f / 3f, 20);
        renderer.setDocumentFromString(html);
        renderer.layout();
        
        PDFEncryption pdfEncryption  = new PDFEncryption();
        pdfEncryption.setUserPassword(password.getBytes());
        renderer.setPDFEncryption(pdfEncryption);
        
        renderer.createPDF(pdfStream);
        pdfStream.close();
        
        byte[] encoded = Base64.encodeBase64(pdfStream.toByteArray());
		String encodedString = new String(encoded);
		
        return encodedString;
    }

    private Context getContext(AccountDetail data) {
        Context context = new Context();
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");  
        Date date = new Date(); 
        
        context.setVariable("createdDate", formatter.format(date));
        context.setVariable("salutation", data.getSalutation());
        context.setVariable("customerFullName", data.getCustomerFullName());        
        context.setVariable("addressLine1", data.getAddressLine1());
        context.setVariable("addressLine2", data.getAddressLine2());
        context.setVariable("addressLine3", data.getAddressLine3());
        context.setVariable("addressLine4", data.getAddressLine4());
        context.setVariable("city", data.getCity());
        context.setVariable("country", data.getCountry());
        context.setVariable("accountType", data.getAccountType());
        context.setVariable("currency", data.getCurrency());
        context.setVariable("accountNumber", data.getAccountNumber());
        context.setVariable("branch", data.getBranch());
        context.setVariable("branchCode", data.getBranchCode());
                      
        return context;
    }

    private String loadAndFillTemplate(Context context, String template) {
    	
    	ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCheckExistence(true);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine.process(template, context);
              
    }
           
    public static void main(String[] args) {
    	try {
    		
    		AccDetailsPdfService asd= new AccDetailsPdfService();
    		String accountDetailjson = "{'salutation':'Mr','customerFullName':'Gihan Liyanage','customerIdentificationNumber':'920904211V','branch':'MOR','branchCode':'0120','addressLine1':'11/1/2','addressLine2':'Katubadda','addressLine3':'Moratuwa','city':'Mota','country':'Sri Lanka','accountType':'SAVINGS ACCOUNT-RUPEE','currency':'LKR','accountNumber':'1745476'}";
    		System.out.println(asd.generatePdf(accountDetailjson));
    		
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

}
