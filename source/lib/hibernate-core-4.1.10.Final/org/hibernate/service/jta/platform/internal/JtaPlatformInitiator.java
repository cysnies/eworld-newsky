package org.hibernate.service.jta.platform.internal;

import java.util.Map;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.jndi.JndiHelper;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.config.spi.ConfigurationService;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.service.jta.platform.spi.JtaPlatformException;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.transaction.TransactionManagerLookup;
import org.jboss.logging.Logger;

public class JtaPlatformInitiator implements BasicServiceInitiator {
   public static final JtaPlatformInitiator INSTANCE = new JtaPlatformInitiator();
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JtaPlatformInitiator.class.getName());

   public Class getServiceInitiated() {
      return JtaPlatform.class;
   }

   public JtaPlatform initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      Object platform = this.getConfiguredPlatform(configurationValues, registry);
      return (JtaPlatform)(platform == null ? new NoJtaPlatform() : (JtaPlatform)((ConfigurationService)registry.getService(ConfigurationService.class)).cast(JtaPlatform.class, platform));
   }

   private Object getConfiguredPlatform(Map configVales, ServiceRegistryImplementor registry) {
      Object platform = configVales.get("hibernate.transaction.jta.platform");
      if (platform == null) {
         String transactionManagerLookupImplName = (String)configVales.get("hibernate.transaction.manager_lookup_class");
         if (transactionManagerLookupImplName != null) {
            LOG.deprecatedTransactionManagerStrategy(TransactionManagerLookup.class.getName(), "hibernate.transaction.manager_lookup_class", JtaPlatform.class.getName(), "hibernate.transaction.jta.platform");
            platform = this.mapLegacyClasses(transactionManagerLookupImplName, configVales, registry);
            LOG.debugf("Mapped %s -> %s", transactionManagerLookupImplName, platform);
         }
      }

      return platform;
   }

   private JtaPlatform mapLegacyClasses(String tmlImplName, Map configVales, ServiceRegistryImplementor registry) {
      if (tmlImplName == null) {
         return null;
      } else {
         LOG.legacyTransactionManagerStrategy(JtaPlatform.class.getName(), "hibernate.transaction.jta.platform");
         if ("org.hibernate.transaction.BESTransactionManagerLookup".equals(tmlImplName)) {
            return new BorlandEnterpriseServerJtaPlatform();
         } else if ("org.hibernate.transaction.BTMTransactionManagerLookup".equals(tmlImplName)) {
            return new BitronixJtaPlatform();
         } else if ("org.hibernate.transaction.JBossTransactionManagerLookup".equals(tmlImplName)) {
            return new JBossAppServerJtaPlatform();
         } else if ("org.hibernate.transaction.JBossTSStandaloneTransactionManagerLookup".equals(tmlImplName)) {
            return new JBossStandAloneJtaPlatform();
         } else if ("org.hibernate.transaction.JOnASTransactionManagerLookup".equals(tmlImplName)) {
            return new JOnASJtaPlatform();
         } else if ("org.hibernate.transaction.JOTMTransactionManagerLookup".equals(tmlImplName)) {
            return new JOTMJtaPlatform();
         } else if ("org.hibernate.transaction.JRun4TransactionManagerLookup".equals(tmlImplName)) {
            return new JRun4JtaPlatform();
         } else if ("org.hibernate.transaction.OC4JTransactionManagerLookup".equals(tmlImplName)) {
            return new OC4JJtaPlatform();
         } else if ("org.hibernate.transaction.OrionTransactionManagerLookup".equals(tmlImplName)) {
            return new OrionJtaPlatform();
         } else if ("org.hibernate.transaction.ResinTransactionManagerLookup".equals(tmlImplName)) {
            return new ResinJtaPlatform();
         } else if ("org.hibernate.transaction.SunONETransactionManagerLookup".equals(tmlImplName)) {
            return new SunOneJtaPlatform();
         } else if ("org.hibernate.transaction.WeblogicTransactionManagerLookup".equals(tmlImplName)) {
            return new WeblogicJtaPlatform();
         } else if ("org.hibernate.transaction.WebSphereTransactionManagerLookup".equals(tmlImplName)) {
            return new WebSphereJtaPlatform();
         } else if ("org.hibernate.transaction.WebSphereExtendedJTATransactionLookup".equals(tmlImplName)) {
            return new WebSphereExtendedJtaPlatform();
         } else {
            try {
               TransactionManagerLookup lookup = (TransactionManagerLookup)((ClassLoaderService)registry.getService(ClassLoaderService.class)).classForName(tmlImplName).newInstance();
               return new TransactionManagerLookupBridge(lookup, JndiHelper.extractJndiProperties(configVales));
            } catch (Exception var5) {
               throw new JtaPlatformException("Unable to build " + TransactionManagerLookupBridge.class.getName() + " from specified " + TransactionManagerLookup.class.getName() + " implementation: " + tmlImplName);
            }
         }
      }
   }
}
