package au.com.miskinhill.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DecoratorServlet extends HttpServlet {
    
    private static final long serialVersionUID = -379349824773494218L;
    
    private String decoratorContent;
    
    @Override
    public void init() throws ServletException {
        try {
            decoratorContent = exhaust(this.getClass().getResource("/au/com/miskinhill/web/decorator/_commonwrapper.html").toURI());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().append(decoratorContent);
    }
    
    private String exhaust(URI file) throws IOException { // sigh
        FileChannel channel = new FileInputStream(new File(file)).getChannel();
        Charset charset = Charset.defaultCharset();
        StringBuffer sb = new StringBuffer();
        ByteBuffer b = ByteBuffer.allocate(8192);
        while (channel.read(b) > 0) {
            b.rewind();
            sb.append(charset.decode(b));
            b.flip();
        }
        return sb.toString();
    }
    
}