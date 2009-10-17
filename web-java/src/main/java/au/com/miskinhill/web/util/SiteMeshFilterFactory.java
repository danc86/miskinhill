package au.com.miskinhill.web.util;

import javax.servlet.Filter;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.webapp.SiteMeshFilter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component("sitemesh")
public class SiteMeshFilterFactory implements FactoryBean<Filter> {
    
    @Override
    public Filter getObject() throws Exception {
        return new SiteMeshFilterBuilder()
                .addDecoratorPath("/*", "/WEB-INF/decorators/_commonwrapper.html")
                .create();
    }

    @Override
    public Class<? extends Filter> getObjectType() {
        return SiteMeshFilter.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
