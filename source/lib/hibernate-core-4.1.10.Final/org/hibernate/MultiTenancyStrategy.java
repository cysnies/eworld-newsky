package org.hibernate;

import java.util.Map;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public enum MultiTenancyStrategy {
   DISCRIMINATOR,
   SCHEMA,
   DATABASE,
   NONE;

   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, MultiTenancyStrategy.class.getName());

   public boolean requiresMultiTenantConnectionProvider() {
      return this == DATABASE || this == SCHEMA;
   }

   public static MultiTenancyStrategy determineMultiTenancyStrategy(Map properties) {
      Object strategy = properties.get("hibernate.multiTenancy");
      if (strategy == null) {
         return NONE;
      } else if (MultiTenancyStrategy.class.isInstance(strategy)) {
         return (MultiTenancyStrategy)strategy;
      } else {
         String strategyName = strategy.toString();

         try {
            return valueOf(strategyName.toUpperCase());
         } catch (RuntimeException var4) {
            LOG.warn("Unknown multi tenancy strategy [ " + strategyName + " ], using MultiTenancyStrategy.NONE.");
            return NONE;
         }
      }
   }
}
