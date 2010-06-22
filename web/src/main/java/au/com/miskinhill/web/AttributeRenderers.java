package au.com.miskinhill.web;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Component;

import au.id.djc.stringtemplate.AttributeRendererMethod;

@Component
public class AttributeRenderers {
    
    @AttributeRendererMethod(format = "xml-escaped")
    public String escapeXml(String raw) {
        return StringEscapeUtils.escapeXml(raw);
    }

}
