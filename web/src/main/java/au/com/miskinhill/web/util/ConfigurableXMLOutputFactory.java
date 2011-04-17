package au.com.miskinhill.web.util;

import javax.xml.stream.XMLOutputFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;

// SIGH!
public final class ConfigurableXMLOutputFactory implements InitializingBean,
        FactoryBean<XMLOutputFactory> {
    
    private XMLOutputFactory instance;
    private boolean repairingNamespaces = true;
    
    public void setRepairingNamespaces(boolean repairingNamespaces) {
        this.repairingNamespaces = repairingNamespaces;
    }
    
    public boolean isRepairingNamespaces() {
        return repairingNamespaces;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        instance = XMLOutputFactory.newInstance();
        instance.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES,
                repairingNamespaces);
    }

    @Override
    public XMLOutputFactory getObject() throws Exception {
        if (instance == null)
            throw new FactoryBeanNotInitializedException();
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return XMLOutputFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
    
}
