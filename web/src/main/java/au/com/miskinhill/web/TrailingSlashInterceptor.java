package au.com.miskinhill.web;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import au.com.miskinhill.web.exception.RedirectException;

@Component
@Aspect
public class TrailingSlashInterceptor {
    
    private static final Logger LOG = Logger.getLogger(TrailingSlashInterceptor.class.getName());
    
    @Around("@annotation(au.com.miskinhill.web.TrailingSlash)")
    public Object intercept(ProceedingJoinPoint joinpoint) throws Throwable {
        HttpServletRequest request = findArgOfType(joinpoint.getArgs(), HttpServletRequest.class);
        String requestUrl = request.getRequestURL().toString();
        if (!requestUrl.endsWith("/")) {
            LOG.fine("Redirecting " + requestUrl);
            throw new RedirectException(requestUrl + "/", HttpStatus.MOVED_PERMANENTLY);
        } else {
            return joinpoint.proceed();
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T findArgOfType(Object[] args, Class<T> clazz) {
        for (Object arg: args) {
            if (clazz.isInstance(arg)) return (T) arg;
        }
        throw new IllegalStateException("Advised controller method must accept an arg of type " + clazz.getName());
    }

}
