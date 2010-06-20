package au.com.miskinhill;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import au.com.miskinhill.xhtmldtd.XhtmlEntityResolver;

public class Dom4jDocumentMessageConverter implements HttpMessageConverter<Document> {
    
    private static final List<MediaType> MEDIA_TYPES = Arrays.asList(MediaType.TEXT_HTML,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML);
    
    private final SAXReader reader = new SAXReader();
    
    public Dom4jDocumentMessageConverter() {
        reader.setEntityResolver(new XhtmlEntityResolver());
    }
    
    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return MEDIA_TYPES;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        if (!Document.class.equals(clazz)) return false;
        for (MediaType supported : MEDIA_TYPES) {
            if (supported.includes(mediaType)) return true;
        }
        return false;
    }

    @Override
    public Document read(Class<? extends Document> clazz, HttpInputMessage inputMessage) throws IOException,
            HttpMessageNotReadableException {
        try {
            return reader.read(inputMessage.getBody());
        } catch (DocumentException e) {
            throw new HttpMessageNotReadableException("Failed to read document", e);
        }
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public void write(Document t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException,
            HttpMessageNotWritableException {
        throw new HttpMessageNotWritableException("Not supported");
    }

}
