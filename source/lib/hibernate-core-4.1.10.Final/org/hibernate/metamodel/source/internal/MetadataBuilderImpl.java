package org.hibernate.metamodel.source.internal;

import javax.persistence.SharedCacheMode;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.EJB3NamingStrategy;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.metamodel.Metadata;
import org.hibernate.metamodel.MetadataBuilder;
import org.hibernate.metamodel.MetadataSourceProcessingOrder;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.config.spi.ConfigurationService;

public class MetadataBuilderImpl implements MetadataBuilder {
   private final MetadataSources sources;
   private final OptionsImpl options;

   public MetadataBuilderImpl(MetadataSources sources) {
      this.sources = sources;
      this.options = new OptionsImpl(sources.getServiceRegistry());
   }

   public MetadataBuilder with(NamingStrategy namingStrategy) {
      this.options.namingStrategy = namingStrategy;
      return this;
   }

   public MetadataBuilder with(MetadataSourceProcessingOrder metadataSourceProcessingOrder) {
      this.options.metadataSourceProcessingOrder = metadataSourceProcessingOrder;
      return this;
   }

   public MetadataBuilder with(SharedCacheMode sharedCacheMode) {
      this.options.sharedCacheMode = sharedCacheMode;
      return this;
   }

   public MetadataBuilder with(AccessType accessType) {
      this.options.defaultCacheAccessType = accessType;
      return this;
   }

   public MetadataBuilder withNewIdentifierGeneratorsEnabled(boolean enabled) {
      this.options.useNewIdentifierGenerators = enabled;
      return this;
   }

   public Metadata buildMetadata() {
      return new MetadataImpl(this.sources, this.options);
   }

   private static class OptionsImpl implements Metadata.Options {
      private MetadataSourceProcessingOrder metadataSourceProcessingOrder;
      private NamingStrategy namingStrategy;
      private SharedCacheMode sharedCacheMode;
      private AccessType defaultCacheAccessType;
      private boolean useNewIdentifierGenerators;
      private boolean globallyQuotedIdentifiers;
      private String defaultSchemaName;
      private String defaultCatalogName;

      public OptionsImpl(ServiceRegistry serviceRegistry) {
         this.metadataSourceProcessingOrder = MetadataSourceProcessingOrder.HBM_FIRST;
         this.namingStrategy = EJB3NamingStrategy.INSTANCE;
         this.sharedCacheMode = SharedCacheMode.ENABLE_SELECTIVE;
         ConfigurationService configService = (ConfigurationService)serviceRegistry.getService(ConfigurationService.class);
         this.defaultCacheAccessType = (AccessType)configService.getSetting("hibernate.cache.default_cache_concurrency_strategy", new ConfigurationService.Converter() {
            public AccessType convert(Object value) {
               return AccessType.fromExternalName(value.toString());
            }
         });
         this.useNewIdentifierGenerators = (Boolean)configService.getSetting("hibernate.id.new_generator_mappings", (ConfigurationService.Converter)(new ConfigurationService.Converter() {
            public Boolean convert(Object value) {
               return Boolean.parseBoolean(value.toString());
            }
         }), false);
         this.defaultSchemaName = (String)configService.getSetting("hibernate.default_schema", (ConfigurationService.Converter)(new ConfigurationService.Converter() {
            public String convert(Object value) {
               return value.toString();
            }
         }), (Object)null);
         this.defaultCatalogName = (String)configService.getSetting("hibernate.default_catalog", (ConfigurationService.Converter)(new ConfigurationService.Converter() {
            public String convert(Object value) {
               return value.toString();
            }
         }), (Object)null);
         this.globallyQuotedIdentifiers = (Boolean)configService.getSetting("hibernate.globally_quoted_identifiers", (ConfigurationService.Converter)(new ConfigurationService.Converter() {
            public Boolean convert(Object value) {
               return Boolean.parseBoolean(value.toString());
            }
         }), false);
      }

      public MetadataSourceProcessingOrder getMetadataSourceProcessingOrder() {
         return this.metadataSourceProcessingOrder;
      }

      public NamingStrategy getNamingStrategy() {
         return this.namingStrategy;
      }

      public AccessType getDefaultAccessType() {
         return this.defaultCacheAccessType;
      }

      public SharedCacheMode getSharedCacheMode() {
         return this.sharedCacheMode;
      }

      public boolean useNewIdentifierGenerators() {
         return this.useNewIdentifierGenerators;
      }

      public boolean isGloballyQuotedIdentifiers() {
         return this.globallyQuotedIdentifiers;
      }

      public String getDefaultSchemaName() {
         return this.defaultSchemaName;
      }

      public String getDefaultCatalogName() {
         return this.defaultCatalogName;
      }
   }
}
