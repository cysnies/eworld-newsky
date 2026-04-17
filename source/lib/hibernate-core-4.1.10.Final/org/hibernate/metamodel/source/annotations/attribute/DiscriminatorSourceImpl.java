package org.hibernate.metamodel.source.annotations.attribute;

import org.hibernate.metamodel.source.annotations.entity.EntityClass;
import org.hibernate.metamodel.source.binder.DiscriminatorSource;
import org.hibernate.metamodel.source.binder.RelationalValueSource;

public class DiscriminatorSourceImpl implements DiscriminatorSource {
   private final EntityClass entityClass;

   public DiscriminatorSourceImpl(EntityClass entityClass) {
      this.entityClass = entityClass;
   }

   public boolean isForced() {
      return this.entityClass.isDiscriminatorForced();
   }

   public boolean isInserted() {
      return this.entityClass.isDiscriminatorIncludedInSql();
   }

   public RelationalValueSource getDiscriminatorRelationalValueSource() {
      return (RelationalValueSource)(this.entityClass.getDiscriminatorFormula() != null ? new DerivedValueSourceImpl(this.entityClass.getDiscriminatorFormula()) : new ColumnValuesSourceImpl(this.entityClass.getDiscriminatorColumnValues()));
   }

   public String getExplicitHibernateTypeName() {
      return this.entityClass.getDiscriminatorType().getName();
   }
}
