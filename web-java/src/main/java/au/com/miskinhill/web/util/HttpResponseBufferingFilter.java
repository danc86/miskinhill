package au.com.miskinhill.web.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/** Based on ContentBufferingFilter from SiteMesh 3. */
public abstract class HttpResponseBufferingFilter implements Filter {
    
    private final class BufferingHttpServletResponse extends HttpServletResponseWrapper {
        
        private StringWriter writer;
        private PrintWriter printWriter;
        private ByteArrayOutputStream outputStream;
        private ServletOutputStream servletOutputStream;
        private int status = HttpServletResponse.SC_OK;
        private String contentType;
        
        public BufferingHttpServletResponse(HttpServletResponse original) {
            super(original);
        }
        
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (writer != null)
                throw new IllegalStateException("Already called getWriter()");
            if (servletOutputStream == null) {
                servletOutputStream = new ServletOutputStream() {
                    private void maybeInitialiseUnderlying() {
                        if (outputStream == null) outputStream = new ByteArrayOutputStream();
                    }
                    @Override
                    public void write(int b) throws IOException {
                        maybeInitialiseUnderlying();
                        outputStream.write(b);
                    }
                    @Override
                    public void write(byte[] b) throws IOException {
                        maybeInitialiseUnderlying();
                        outputStream.write(b);
                    }
                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        maybeInitialiseUnderlying();
                        outputStream.write(b, off, len);
                    }
                    @Override
                    public void flush() throws IOException {
                        maybeInitialiseUnderlying();
                        outputStream.flush();
                    }
                };
            }
            return servletOutputStream;
        }
        
        @Override
        public PrintWriter getWriter() throws IOException {
            if (outputStream != null)
                throw new IllegalStateException("Already called getOutputStream()");
            if (writer == null)
                writer = new StringWriter();
            if (printWriter == null)
                printWriter = new PrintWriter(writer);
            return printWriter;
        }
        
        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            status = sc;
        }
        
        @Override
        public void setStatus(int sc, String sm) {
            super.setStatus(sc, sm);
            status = sc;
        }
        
        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            status = sc;
        }
        
        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            status = sc;
        }
        
        @Override
        public void sendRedirect(String location) throws IOException {
            super.sendRedirect(location);
            status = HttpServletResponse.SC_TEMPORARY_REDIRECT;
        }
        
        @Override
        public void setContentType(String type) {
            super.setContentType(type);
            contentType = type;
        }
        
        @Override
        public void setCharacterEncoding(String charset) {
            if (!charset.equals("UTF-8"))
                throw new UnsupportedOperationException("Unacceptable charset " + charset);
            super.setCharacterEncoding(charset);
        }
        
        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, value);
            if (name.equalsIgnoreCase("Content-Type"))
                contentType = value;
        }
        
        public String getBufferedResponse() {
            if (writer != null)
                return writer.toString();
            if (outputStream != null) {
                try {
                    return outputStream.toString("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
        
        public void writeThrough(String response) throws IOException {
            byte[] encodedResponse = response.getBytes("UTF-8");
            super.setContentLength(encodedResponse.length);
            if (outputStream != null) {
                super.getOutputStream().write(encodedResponse);
            } else {
                super.getWriter().write(response);
            }
        }
        
        public int getStatus() {
            return status;
        }
        
        @Override
        public String getContentType() {
            return contentType;
        }
        
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (internalShouldBuffer(request, response)) {
            BufferingHttpServletResponse wrappedResponse = new BufferingHttpServletResponse((HttpServletResponse) response);
            chain.doFilter(request, wrappedResponse);
            String bufferedResponse = wrappedResponse.getBufferedResponse();
            if (bufferedResponse != null) {
                if (shouldPostprocessResponse(wrappedResponse.getContentType(), wrappedResponse.getStatus())) {
                    wrappedResponse.writeThrough(postprocessResponse(bufferedResponse));
                } else {
                    wrappedResponse.writeThrough(bufferedResponse);
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }
    
    private boolean internalShouldBuffer(ServletRequest request, ServletResponse response) {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse))
            return false;
        return shouldBuffer((HttpServletRequest) request);
    }
    
    /**
     * Returns true if this request should be buffered. Returning false means
     * that {@link #postprocess()} will never be called for this request.
     */
    protected abstract boolean shouldBuffer(HttpServletRequest request);

    /**
     * Returns true if post-processing should be applied to this response.
     * Returning false means that {@link #postprocess()} will never be called
     * for this request.
     */
    protected abstract boolean shouldPostprocessResponse(String contentType, int status);
    
    /**
     * Returns a post-processed version of the given buffered response.
     */
    protected abstract String postprocessResponse(String responseBody);

}
