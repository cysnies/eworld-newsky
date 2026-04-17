package org.hibernate.metamodel.source.annotations.entity;

import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.metamodel.binding.Caching;
import org.hibernate.metamodel.source.annotations.attribute.BasicAttribute;
import org.hibernate.metamodel.source.annotations.attribute.DiscriminatorSourceImpl;
import org.hibernate.metamodel.source.annotations.attribute.SimpleIdentifierSourceImpl;
import org.hibernate.metamodel.source.annotations.attribute.SingularAttributeSourceImpl;
import org.hibernate.metamodel.source.binder.DiscriminatorSource;
import org.hibernate.metamodel.source.binder.IdentifierSource;
import org.hibernate.metamodel.source.binder.RootEntitySource;
import org.hibernate.metamodel.source.binder.SingularAttributeSource;

public class RootEntitySourceImpl extends EntitySourceImpl implements RootEntitySource {
   public RootEntitySourceImpl(EntityClass entityClass) {
      super(entityClass);
   }

   public IdentifierSource getIdentifierSource() {
      IdType idType = this.getEntityClass().getIdType();
      switch (idType) {
         case SIMPLE:
            BasicAttribute attribute = (BasicAttribute)this.getEntityClass().getIdAttributes().iterator().next();
            return new SimpleIdentifierSourceImpl(attribute, this.getEntityClass().getAttributeOverrideMap());
         case COMPOSED:
            throw new NotYetImplementedException("Composed ids must still be implemented.");
         case EMBEDDED:
            throw new NotYetImplementedException("Embedded ids must still be implemented.");
         default:
            throw new AssertionFailure("The root entity needs to specify an identifier");
      }
   }

   public SingularAttributeSource getVersioningAttributeSource() {
      SingularAttributeSource attributeSource = null;
      EntityClass entityClass = this.getEntityClass();
      if (entityClass.getVersionAttribute() != null) {
         attributeSource = new SingularAttributeSourceImpl(entityClass.getVersionAttribute());
      }

      return attributeSource;
   }

   public DiscriminatorSource getDiscriminatorSource() {
      DiscriminatorSource discriminatorSource = null;
      if (this.getEntityClass().getDiscriminatorColumnValues() != null) {
         discriminatorSource = new DiscriminatorSourceImpl(this.getEntityClass());
      }

      return discriminatorSource;
   }

   public EntityMode getEntityMode() {
      return EntityMode.POJO;
   }

   public boolean isMutable() {
      return this.getEntityClass().isMutable();
   }

   public boolean isExplicitPolymorphism() {
      return this.getEntityClass().isExplicitPolymorphism();
   }

   public String getWhere() {
      return this.getEntityClass().getWhereClause();
   }

   public String getRowId() {
      return this.getEntityClass().getRowId();
   }

   public OptimisticLockStyle getOptimisticLockStyle() {
      return this.getEntityClass().getOptimisticLockStyle();
   }

   public Caching getCaching() {
      return this.getEntityClass().getCaching();
   }
}
