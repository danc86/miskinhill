package au.com.miskinhill.web;

import java.util.Hashtable;

import javax.servlet.ServletException;

import org.apache.catalina.servlets.DefaultServlet;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.ProxyDirContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PdfFulltextServlet extends DefaultServlet {
    
    private static final long serialVersionUID = 5706442353606756106L;

    @Override
    public void init() throws ServletException {
        ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        FileDirContext fileDirContext = new FileDirContext();
        fileDirContext.setDocBase(applicationContext.getBean("pdfFulltextBase", String.class));
        resources = new ProxyDirContext(new Hashtable<String, Object>(), fileDirContext);
    }

}
