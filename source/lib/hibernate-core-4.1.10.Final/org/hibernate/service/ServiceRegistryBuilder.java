package org.hibernate.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.hibernate.cfg.Environment;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.IntegratorService;
import org.hibernate.integrator.spi.ServiceContributingIntegrator;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.jaxb.SourceType;
import org.hibernate.internal.jaxb.cfg.JaxbHibernateConfiguration;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.internal.util.config.ConfigurationException;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.internal.BootstrapServiceRegistryImpl;
import org.hibernate.service.internal.JaxbProcessor;
import org.hibernate.service.internal.ProvidedService;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.jboss.logging.Logger;

public class ServiceRegistryBuilder {
   private static final Logger log = Logger.getLogger(ServiceRegistryBuilder.class);
   public static final String DEFAULT_CFG_RESOURCE_NAME = "hibernate.cfg.xml";
   private final Map settings;
   private final List initiators;
   private final List providedServices;
   private final BootstrapServiceRegistry bootstrapServiceRegistry;
   private ValueHolder jaxbProcessorHolder;

   public ServiceRegistryBuilder() {
      this(new BootstrapServiceRegistryImpl());
   }

   public ServiceRegistryBuilder(BootstrapServiceRegistry bootstrapServiceRegistry) {
      this.initiators = standardInitiatorList();
      this.providedServices = new ArrayList();
      this.jaxbProcessorHolder = new ValueHolder(new ValueHolder.DeferredInitializer() {
         public JaxbProcessor initialize() {
            return new JaxbProcessor((ClassLoaderService)ServiceRegistryBuilder.this.bootstrapServiceRegistry.getService(ClassLoaderService.class));
         }
      });
      this.settings = Environment.getProperties();
      this.bootstrapServiceRegistry = bootstrapServiceRegistry;
   }

   private static List standardInitiatorList() {
      List<BasicServiceInitiator> initiators = new ArrayList();
      initiators.addAll(StandardServiceInitiators.LIST);
      return initiators;
   }

   public ServiceRegistryBuilder loadProperties(String resourceName) {
      InputStream stream = ((ClassLoaderService)this.bootstrapServiceRegistry.getService(ClassLoaderService.class)).locateResourceStream(resourceName);

      try {
         Properties properties = new Properties();
         properties.load(stream);
         this.settings.putAll(properties);
      } catch (IOException e) {
         throw new ConfigurationException("Unable to apply settings from properties file [" + resourceName + "]", e);
      } finally {
         try {
            stream.close();
         } catch (IOException e) {
            log.debug(String.format("Unable to close properties file [%s] stream", resourceName), e);
         }

      }

      return this;
   }

   public ServiceRegistryBuilder configure() {
      return this.configure("hibernate.cfg.xml");
   }

   public ServiceRegistryBuilder configure(String resourceName) {
      InputStream stream = ((ClassLoaderService)this.bootstrapServiceRegistry.getService(ClassLoaderService.class)).locateResourceStream(resourceName);
      JaxbHibernateConfiguration configurationElement = ((JaxbProcessor)this.jaxbProcessorHolder.getValue()).unmarshal(stream, new Origin(SourceType.RESOURCE, resourceName));

      for(JaxbHibernateConfiguration.JaxbSessionFactory.JaxbProperty xmlProperty : configurationElement.getSessionFactory().getProperty()) {
         this.settings.put(xmlProperty.getName(), xmlProperty.getValue());
      }

      return this;
   }

   public ServiceRegistryBuilder applySetting(String settingName, Object value) {
      this.settings.put(settingName, value);
      return this;
   }

   public ServiceRegistryBuilder applySettings(Map settings) {
      this.settings.putAll(settings);
      return this;
   }

   public ServiceRegistryBuilder addInitiator(BasicServiceInitiator initiator) {
      this.initiators.add(initiator);
      return this;
   }

   public ServiceRegistryBuilder addService(Class serviceRole, Service service) {
      this.providedServices.add(new ProvidedService(serviceRole, service));
      return this;
   }

   public ServiceRegistry buildServiceRegistry() {
      Map<?, ?> settingsCopy = new HashMap();
      settingsCopy.putAll(this.settings);
      Environment.verifyProperties(settingsCopy);
      ConfigurationHelper.resolvePlaceHolders(settingsCopy);

      for(Integrator integrator : ((IntegratorService)this.bootstrapServiceRegistry.getService(IntegratorService.class)).getIntegrators()) {
         if (ServiceContributingIntegrator.class.isInstance(integrator)) {
            ((ServiceContributingIntegrator)ServiceContributingIntegrator.class.cast(integrator)).prepareServices(this);
         }
      }

      return new StandardServiceRegistryImpl(this.bootstrapServiceRegistry, this.initiators, this.providedServices, settingsCopy);
   }

   public static void destroy(ServiceRegistry serviceRegistry) {
      ((StandardServiceRegistryImpl)serviceRegistry).destroy();
   }
}
