package org.hibernate.metamodel.source.internal;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.config.spi.ConfigurationService;

public class IdentifierGeneratorResolver {
   private final MetadataImplementor metadata;

   IdentifierGeneratorResolver(MetadataImplementor metadata) {
      this.metadata = metadata;
   }

   void resolve() {
      for(EntityBinding entityBinding : this.metadata.getEntityBindings()) {
         if (entityBinding.isRoot()) {
            Properties properties = new Properties();
            properties.putAll(((ConfigurationService)this.metadata.getServiceRegistry().getService(ConfigurationService.class)).getSettings());
            if (!properties.contains("hibernate.id.optimizer.pooled.prefer_lo")) {
               properties.put("hibernate.id.optimizer.pooled.prefer_lo", "false");
            }

            if (!properties.contains("identifier_normalizer")) {
               properties.put("identifier_normalizer", new ObjectNameNormalizerImpl(this.metadata));
            }

            entityBinding.getHierarchyDetails().getEntityIdentifier().createIdentifierGenerator(this.metadata.getIdentifierGeneratorFactory(), properties);
         }
      }

   }

   private static class ObjectNameNormalizerImpl extends ObjectNameNormalizer implements Serializable {
      private final boolean useQuotedIdentifiersGlobally;
      private final NamingStrategy namingStrategy;

      private ObjectNameNormalizerImpl(MetadataImplementor metadata) {
         this.useQuotedIdentifiersGlobally = metadata.isGloballyQuotedIdentifiers();
         this.namingStrategy = metadata.getNamingStrategy();
      }

      protected boolean isUseQuotedIdentifiersGlobally() {
         return this.useQuotedIdentifiersGlobally;
      }

      protected NamingStrategy getNamingStrategy() {
         return this.namingStrategy;
      }
   }
}
