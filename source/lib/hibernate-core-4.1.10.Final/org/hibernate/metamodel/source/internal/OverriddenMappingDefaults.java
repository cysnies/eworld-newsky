package org.hibernate.metamodel.source.internal;

import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.metamodel.source.MappingDefaults;

public class OverriddenMappingDefaults implements MappingDefaults {
   private MappingDefaults overriddenValues;
   private final String packageName;
   private final String schemaName;
   private final String catalogName;
   private final String idColumnName;
   private final String discriminatorColumnName;
   private final String cascade;
   private final String propertyAccess;
   private final Boolean associationLaziness;

   public OverriddenMappingDefaults(MappingDefaults overriddenValues, String packageName, String schemaName, String catalogName, String idColumnName, String discriminatorColumnName, String cascade, String propertyAccess, Boolean associationLaziness) {
      if (overriddenValues == null) {
         throw new IllegalArgumentException("Overridden values cannot be null");
      } else {
         this.overriddenValues = overriddenValues;
         this.packageName = packageName;
         this.schemaName = schemaName;
         this.catalogName = catalogName;
         this.idColumnName = idColumnName;
         this.discriminatorColumnName = discriminatorColumnName;
         this.cascade = cascade;
         this.propertyAccess = propertyAccess;
         this.associationLaziness = associationLaziness;
      }
   }

   public String getPackageName() {
      return this.packageName == null ? this.overriddenValues.getPackageName() : this.packageName;
   }

   public String getSchemaName() {
      return this.schemaName == null ? this.overriddenValues.getSchemaName() : this.schemaName;
   }

   public String getCatalogName() {
      return this.catalogName == null ? this.overriddenValues.getCatalogName() : this.catalogName;
   }

   public String getIdColumnName() {
      return this.idColumnName == null ? this.overriddenValues.getIdColumnName() : this.idColumnName;
   }

   public String getDiscriminatorColumnName() {
      return this.discriminatorColumnName == null ? this.overriddenValues.getDiscriminatorColumnName() : this.discriminatorColumnName;
   }

   public String getCascadeStyle() {
      return this.cascade == null ? this.overriddenValues.getCascadeStyle() : this.cascade;
   }

   public String getPropertyAccessorName() {
      return this.propertyAccess == null ? this.overriddenValues.getPropertyAccessorName() : this.propertyAccess;
   }

   public boolean areAssociationsLazy() {
      return this.associationLaziness == null ? this.overriddenValues.areAssociationsLazy() : this.associationLaziness;
   }

   public AccessType getCacheAccessType() {
      return this.overriddenValues.getCacheAccessType();
   }
}
