package org.hibernate.service.config.internal;

import java.util.Collections;
import java.util.Map;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;
import org.hibernate.service.config.spi.ConfigurationService;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.jboss.logging.Logger;

public class ConfigurationServiceImpl implements ConfigurationService, ServiceRegistryAwareService {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ConfigurationServiceImpl.class.getName());
   private final Map settings;
   private ServiceRegistryImplementor serviceRegistry;

   public ConfigurationServiceImpl(Map settings) {
      this.settings = Collections.unmodifiableMap(settings);
   }

   public Map getSettings() {
      return this.settings;
   }

   public void injectServices(ServiceRegistryImplementor serviceRegistry) {
      this.serviceRegistry = serviceRegistry;
   }

   public Object getSetting(String name, ConfigurationService.Converter converter) {
      return this.getSetting(name, (ConfigurationService.Converter)converter, (Object)null);
   }

   public Object getSetting(String name, ConfigurationService.Converter converter, Object defaultValue) {
      Object value = this.settings.get(name);
      return value == null ? defaultValue : converter.convert(value);
   }

   public Object getSetting(String name, Class expected, Object defaultValue) {
      Object value = this.settings.get(name);
      T target = (T)this.cast(expected, value);
      return target != null ? target : defaultValue;
   }

   public Object cast(Class expected, Object candidate) {
      if (candidate == null) {
         return null;
      } else if (expected.isInstance(candidate)) {
         return candidate;
      } else {
         Class<T> target;
         if (Class.class.isInstance(candidate)) {
            target = (Class)Class.class.cast(candidate);
         } else {
            try {
               target = ((ClassLoaderService)this.serviceRegistry.getService(ClassLoaderService.class)).classForName(candidate.toString());
            } catch (ClassLoadingException var5) {
               LOG.debugf("Unable to locate %s implementation class %s", expected.getName(), candidate.toString());
               target = null;
            }
         }

         if (target != null) {
            try {
               return target.newInstance();
            } catch (Exception var6) {
               LOG.debugf("Unable to instantiate %s class %s", expected.getName(), target.getName());
            }
         }

         return null;
      }
   }
}
