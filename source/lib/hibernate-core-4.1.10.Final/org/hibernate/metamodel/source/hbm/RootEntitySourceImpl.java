package org.hibernate.metamodel.source.hbm;

import org.hibernate.EntityMode;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbCacheElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbHibernateMapping;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.binding.Caching;
import org.hibernate.metamodel.binding.IdGenerator;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.binder.DiscriminatorSource;
import org.hibernate.metamodel.source.binder.IdentifierSource;
import org.hibernate.metamodel.source.binder.RelationalValueSource;
import org.hibernate.metamodel.source.binder.RootEntitySource;
import org.hibernate.metamodel.source.binder.SimpleIdentifierSource;
import org.hibernate.metamodel.source.binder.SingularAttributeSource;
import org.hibernate.metamodel.source.binder.TableSource;

public class RootEntitySourceImpl extends AbstractEntitySourceImpl implements RootEntitySource {
   protected RootEntitySourceImpl(MappingDocument sourceMappingDocument, JaxbHibernateMapping.JaxbClass entityElement) {
      super(sourceMappingDocument, entityElement);
   }

   protected JaxbHibernateMapping.JaxbClass entityElement() {
      return (JaxbHibernateMapping.JaxbClass)super.entityElement();
   }

   public IdentifierSource getIdentifierSource() {
      return this.entityElement().getId() != null ? new SimpleIdentifierSource() {
         public SingularAttributeSource getIdentifierAttributeSource() {
            return new SingularIdentifierAttributeSourceImpl(RootEntitySourceImpl.this.entityElement().getId(), RootEntitySourceImpl.this.sourceMappingDocument().getMappingLocalBindingContext());
         }

         public IdGenerator getIdentifierGeneratorDescriptor() {
            if (RootEntitySourceImpl.this.entityElement().getId().getGenerator() != null) {
               String generatorName = RootEntitySourceImpl.this.entityElement().getId().getGenerator().getClazz();
               IdGenerator idGenerator = RootEntitySourceImpl.this.sourceMappingDocument().getMappingLocalBindingContext().getMetadataImplementor().getIdGenerator(generatorName);
               if (idGenerator == null) {
                  idGenerator = new IdGenerator(RootEntitySourceImpl.this.getEntityName() + generatorName, generatorName, Helper.extractParameters(RootEntitySourceImpl.this.entityElement().getId().getGenerator().getParam()));
               }

               return idGenerator;
            } else {
               return null;
            }
         }

         public IdentifierSource.Nature getNature() {
            return IdentifierSource.Nature.SIMPLE;
         }
      } : null;
   }

   public SingularAttributeSource getVersioningAttributeSource() {
      if (this.entityElement().getVersion() != null) {
         return new VersionAttributeSourceImpl(this.entityElement().getVersion(), this.sourceMappingDocument().getMappingLocalBindingContext());
      } else {
         return this.entityElement().getTimestamp() != null ? new TimestampAttributeSourceImpl(this.entityElement().getTimestamp(), this.sourceMappingDocument().getMappingLocalBindingContext()) : null;
      }
   }

   public EntityMode getEntityMode() {
      return this.determineEntityMode();
   }

   public boolean isMutable() {
      return this.entityElement().isMutable();
   }

   public boolean isExplicitPolymorphism() {
      return "explicit".equals(this.entityElement().getPolymorphism());
   }

   public String getWhere() {
      return this.entityElement().getWhere();
   }

   public String getRowId() {
      return this.entityElement().getRowid();
   }

   public OptimisticLockStyle getOptimisticLockStyle() {
      String optimisticLockModeString = Helper.getStringValue(this.entityElement().getOptimisticLock(), "version");

      try {
         return OptimisticLockStyle.valueOf(optimisticLockModeString.toUpperCase());
      } catch (Exception var3) {
         throw new MappingException("Unknown optimistic-lock value : " + optimisticLockModeString, this.sourceMappingDocument().getOrigin());
      }
   }

   public Caching getCaching() {
      JaxbCacheElement cache = this.entityElement().getCache();
      if (cache == null) {
         return null;
      } else {
         String region = cache.getRegion() != null ? cache.getRegion() : this.getEntityName();
         AccessType accessType = (AccessType)Enum.valueOf(AccessType.class, cache.getUsage());
         boolean cacheLazyProps = !"non-lazy".equals(cache.getInclude());
         return new Caching(region, accessType, cacheLazyProps);
      }
   }

   public TableSource getPrimaryTable() {
      return new TableSource() {
         public String getExplicitSchemaName() {
            return RootEntitySourceImpl.this.entityElement().getSchema();
         }

         public String getExplicitCatalogName() {
            return RootEntitySourceImpl.this.entityElement().getCatalog();
         }

         public String getExplicitTableName() {
            return RootEntitySourceImpl.this.entityElement().getTable();
         }

         public String getLogicalName() {
            return null;
         }
      };
   }

   public String getDiscriminatorMatchValue() {
      return this.entityElement().getDiscriminatorValue();
   }

   public DiscriminatorSource getDiscriminatorSource() {
      final JaxbHibernateMapping.JaxbClass.JaxbDiscriminator discriminatorElement = this.entityElement().getDiscriminator();
      return discriminatorElement == null ? null : new DiscriminatorSource() {
         public RelationalValueSource getDiscriminatorRelationalValueSource() {
            if (StringHelper.isNotEmpty(discriminatorElement.getColumnAttribute())) {
               return new ColumnAttributeSourceImpl((String)null, discriminatorElement.getColumnAttribute(), discriminatorElement.isInsert(), discriminatorElement.isInsert());
            } else if (StringHelper.isNotEmpty(discriminatorElement.getFormulaAttribute())) {
               return new FormulaImpl((String)null, discriminatorElement.getFormulaAttribute());
            } else if (discriminatorElement.getColumn() != null) {
               return new ColumnSourceImpl((String)null, discriminatorElement.getColumn(), discriminatorElement.isInsert(), discriminatorElement.isInsert());
            } else if (StringHelper.isNotEmpty(discriminatorElement.getFormula())) {
               return new FormulaImpl((String)null, discriminatorElement.getFormula());
            } else {
               throw new MappingException("could not determine source of discriminator mapping", RootEntitySourceImpl.this.getOrigin());
            }
         }

         public String getExplicitHibernateTypeName() {
            return discriminatorElement.getType();
         }

         public boolean isForced() {
            return discriminatorElement.isForce();
         }

         public boolean isInserted() {
            return discriminatorElement.isInsert();
         }
      };
   }
}
