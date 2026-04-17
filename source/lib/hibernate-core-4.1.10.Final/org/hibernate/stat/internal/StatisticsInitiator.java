package org.hibernate.stat.internal;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.config.spi.ConfigurationService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceInitiator;
import org.hibernate.stat.spi.StatisticsFactory;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.jboss.logging.Logger;

public class StatisticsInitiator implements SessionFactoryServiceInitiator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, StatisticsInitiator.class.getName());
   public static final StatisticsInitiator INSTANCE = new StatisticsInitiator();
   public static final String STATS_BUILDER = "hibernate.stats.factory";
   private static StatisticsFactory DEFAULT_STATS_BUILDER = new StatisticsFactory() {
      public StatisticsImplementor buildStatistics(SessionFactoryImplementor sessionFactory) {
         return new ConcurrentStatisticsImpl(sessionFactory);
      }
   };

   public Class getServiceInitiated() {
      return StatisticsImplementor.class;
   }

   public StatisticsImplementor initiateService(SessionFactoryImplementor sessionFactory, Configuration configuration, ServiceRegistryImplementor registry) {
      Object configValue = configuration.getProperties().get("hibernate.stats.factory");
      return this.initiateServiceInternal(sessionFactory, configValue, registry);
   }

   public StatisticsImplementor initiateService(SessionFactoryImplementor sessionFactory, MetadataImplementor metadata, ServiceRegistryImplementor registry) {
      ConfigurationService configurationService = (ConfigurationService)registry.getService(ConfigurationService.class);
      Object configValue = configurationService.getSetting("hibernate.stats.factory", (ConfigurationService.Converter)null);
      return this.initiateServiceInternal(sessionFactory, configValue, registry);
   }

   private StatisticsImplementor initiateServiceInternal(SessionFactoryImplementor sessionFactory, Object configValue, ServiceRegistryImplementor registry) {
      StatisticsFactory statisticsFactory;
      if (configValue == null) {
         statisticsFactory = DEFAULT_STATS_BUILDER;
      } else if (StatisticsFactory.class.isInstance(configValue)) {
         statisticsFactory = (StatisticsFactory)configValue;
      } else {
         ClassLoaderService classLoaderService = (ClassLoaderService)registry.getService(ClassLoaderService.class);

         try {
            statisticsFactory = (StatisticsFactory)classLoaderService.classForName(configValue.toString()).newInstance();
         } catch (HibernateException e) {
            throw e;
         } catch (Exception e) {
            throw new HibernateException("Unable to instantiate specified StatisticsFactory implementation [" + configValue.toString() + "]", e);
         }
      }

      StatisticsImplementor statistics = statisticsFactory.buildStatistics(sessionFactory);
      boolean enabled = sessionFactory.getSettings().isStatisticsEnabled();
      statistics.setStatisticsEnabled(enabled);
      LOG.debugf("Statistics initialized [enabled=%s]", enabled);
      return statistics;
   }
}
