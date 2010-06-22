package au.com.miskinhill.web.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@Component
public class WebApplicationExceptionResolver implements HandlerExceptionResolver {
    
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        try {
            if (doResolve(ex, response)) return new ModelAndView();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    
    private boolean doResolve(Exception ex, HttpServletResponse response) throws IOException {
        if (ex instanceof RedirectException) {
            RedirectException rex = (RedirectException) ex;
            response.setStatus(rex.getStatus().value());
            response.setHeader("Location", rex.getLocation());
            return true;
        } else if (ex instanceof WebApplicationException) {
            response.sendError(((WebApplicationException) ex).getStatus().value());
            return true;
        }
        return false;
    }

}
