package org.hibernate.metamodel.source.hbm;

import java.util.Collections;
import java.util.Map;
import org.hibernate.FetchMode;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbCacheElement;
import org.hibernate.internal.jaxb.mapping.hbm.PluralAttributeElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.binding.Caching;
import org.hibernate.metamodel.binding.CustomSQL;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.binder.AttributeSourceContainer;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.PluralAttributeElementSource;
import org.hibernate.metamodel.source.binder.PluralAttributeKeySource;
import org.hibernate.metamodel.source.binder.PluralAttributeSource;

public abstract class AbstractPluralAttributeSourceImpl implements PluralAttributeSource {
   private final PluralAttributeElement pluralAttributeElement;
   private final AttributeSourceContainer container;
   private final ExplicitHibernateTypeSource typeInformation;
   private final PluralAttributeKeySource keySource;
   private final PluralAttributeElementSource elementSource;

   protected AbstractPluralAttributeSourceImpl(final PluralAttributeElement pluralAttributeElement, AttributeSourceContainer container) {
      this.pluralAttributeElement = pluralAttributeElement;
      this.container = container;
      this.keySource = new PluralAttributeKeySourceImpl(pluralAttributeElement.getKey(), container);
      this.elementSource = this.interpretElementType();
      this.typeInformation = new ExplicitHibernateTypeSource() {
         public String getName() {
            return pluralAttributeElement.getCollectionType();
         }

         public Map getParameters() {
            return Collections.emptyMap();
         }
      };
   }

   private PluralAttributeElementSource interpretElementType() {
      if (this.pluralAttributeElement.getElement() != null) {
         return new BasicPluralAttributeElementSourceImpl(this.pluralAttributeElement.getElement(), this.container.getLocalBindingContext());
      } else if (this.pluralAttributeElement.getCompositeElement() != null) {
         return new CompositePluralAttributeElementSourceImpl(this.pluralAttributeElement.getCompositeElement(), this.container.getLocalBindingContext());
      } else if (this.pluralAttributeElement.getOneToMany() != null) {
         return new OneToManyPluralAttributeElementSourceImpl(this.pluralAttributeElement.getOneToMany(), this.container.getLocalBindingContext());
      } else if (this.pluralAttributeElement.getManyToMany() != null) {
         return new ManyToManyPluralAttributeElementSourceImpl(this.pluralAttributeElement.getManyToMany(), this.container.getLocalBindingContext());
      } else if (this.pluralAttributeElement.getManyToAny() != null) {
         throw new NotYetImplementedException("Support for many-to-any not yet implemented");
      } else {
         throw new MappingException("Unexpected collection element type : " + this.pluralAttributeElement.getName(), this.bindingContext().getOrigin());
      }
   }

   public PluralAttributeElement getPluralAttributeElement() {
      return this.pluralAttributeElement;
   }

   protected AttributeSourceContainer container() {
      return this.container;
   }

   protected LocalBindingContext bindingContext() {
      return this.container().getLocalBindingContext();
   }

   public PluralAttributeKeySource getKeySource() {
      return this.keySource;
   }

   public PluralAttributeElementSource getElementSource() {
      return this.elementSource;
   }

   public String getExplicitSchemaName() {
      return this.pluralAttributeElement.getSchema();
   }

   public String getExplicitCatalogName() {
      return this.pluralAttributeElement.getCatalog();
   }

   public String getExplicitCollectionTableName() {
      return this.pluralAttributeElement.getTable();
   }

   public String getCollectionTableComment() {
      return this.pluralAttributeElement.getComment();
   }

   public String getCollectionTableCheck() {
      return this.pluralAttributeElement.getCheck();
   }

   public Caching getCaching() {
      JaxbCacheElement cache = this.pluralAttributeElement.getCache();
      if (cache == null) {
         return null;
      } else {
         String region = cache.getRegion() != null ? cache.getRegion() : StringHelper.qualify(this.container().getPath(), this.getName());
         AccessType accessType = (AccessType)Enum.valueOf(AccessType.class, cache.getUsage());
         boolean cacheLazyProps = !"non-lazy".equals(cache.getInclude());
         return new Caching(region, accessType, cacheLazyProps);
      }
   }

   public String getWhere() {
      return this.pluralAttributeElement.getWhere();
   }

   public String getName() {
      return this.pluralAttributeElement.getName();
   }

   public boolean isSingular() {
      return false;
   }

   public ExplicitHibernateTypeSource getTypeInformation() {
      return this.typeInformation;
   }

   public String getPropertyAccessorName() {
      return this.pluralAttributeElement.getAccess();
   }

   public boolean isIncludedInOptimisticLocking() {
      return this.pluralAttributeElement.isOptimisticLock();
   }

   public boolean isInverse() {
      return this.pluralAttributeElement.isInverse();
   }

   public String getCustomPersisterClassName() {
      return this.pluralAttributeElement.getPersister();
   }

   public String getCustomLoaderName() {
      return this.pluralAttributeElement.getLoader() == null ? null : this.pluralAttributeElement.getLoader().getQueryRef();
   }

   public CustomSQL getCustomSqlInsert() {
      return Helper.buildCustomSql(this.pluralAttributeElement.getSqlInsert());
   }

   public CustomSQL getCustomSqlUpdate() {
      return Helper.buildCustomSql(this.pluralAttributeElement.getSqlUpdate());
   }

   public CustomSQL getCustomSqlDelete() {
      return Helper.buildCustomSql(this.pluralAttributeElement.getSqlDelete());
   }

   public CustomSQL getCustomSqlDeleteAll() {
      return Helper.buildCustomSql(this.pluralAttributeElement.getSqlDeleteAll());
   }

   public Iterable metaAttributes() {
      return Helper.buildMetaAttributeSources(this.pluralAttributeElement.getMeta());
   }

   public Iterable getCascadeStyles() {
      return Helper.interpretCascadeStyles(this.pluralAttributeElement.getCascade(), this.bindingContext());
   }

   public FetchTiming getFetchTiming() {
      String fetchSelection = this.pluralAttributeElement.getFetch() != null ? this.pluralAttributeElement.getFetch().value() : null;
      String lazySelection = this.pluralAttributeElement.getLazy() != null ? this.pluralAttributeElement.getLazy().value() : null;
      String outerJoinSelection = this.pluralAttributeElement.getOuterJoin() != null ? this.pluralAttributeElement.getOuterJoin().value() : null;
      if (lazySelection == null) {
         if (!"join".equals(fetchSelection) && !"true".equals(outerJoinSelection)) {
            if ("false".equals(outerJoinSelection)) {
               return FetchTiming.DELAYED;
            } else {
               return this.bindingContext().getMappingDefaults().areAssociationsLazy() ? FetchTiming.DELAYED : FetchTiming.IMMEDIATE;
            }
         } else {
            return FetchTiming.IMMEDIATE;
         }
      } else if ("extra".equals(lazySelection)) {
         return FetchTiming.EXTRA_LAZY;
      } else if ("true".equals(lazySelection)) {
         return FetchTiming.DELAYED;
      } else if ("false".equals(lazySelection)) {
         return FetchTiming.IMMEDIATE;
      } else {
         throw new MappingException(String.format("Unexpected lazy selection [%s] on '%s'", lazySelection, this.pluralAttributeElement.getName()), this.bindingContext().getOrigin());
      }
   }

   public FetchStyle getFetchStyle() {
      String fetchSelection = this.pluralAttributeElement.getFetch() != null ? this.pluralAttributeElement.getFetch().value() : null;
      String outerJoinSelection = this.pluralAttributeElement.getOuterJoin() != null ? this.pluralAttributeElement.getOuterJoin().value() : null;
      int batchSize = Helper.getIntValue(this.pluralAttributeElement.getBatchSize(), -1);
      if (fetchSelection == null) {
         if (outerJoinSelection == null) {
            return batchSize > 1 ? FetchStyle.BATCH : FetchStyle.SELECT;
         } else if ("auto".equals(outerJoinSelection)) {
            return this.bindingContext().getMappingDefaults().areAssociationsLazy() ? FetchStyle.SELECT : FetchStyle.JOIN;
         } else {
            return "true".equals(outerJoinSelection) ? FetchStyle.JOIN : FetchStyle.SELECT;
         }
      } else if ("subselect".equals(fetchSelection)) {
         return FetchStyle.SUBSELECT;
      } else {
         return "join".equals(fetchSelection) ? FetchStyle.JOIN : FetchStyle.SELECT;
      }
   }

   public FetchMode getFetchMode() {
      return this.pluralAttributeElement.getFetch() == null ? FetchMode.DEFAULT : FetchMode.valueOf(this.pluralAttributeElement.getFetch().value());
   }
}
