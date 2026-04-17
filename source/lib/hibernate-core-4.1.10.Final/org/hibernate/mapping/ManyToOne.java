package org.hibernate.mapping;

import java.util.ArrayList;
import java.util.Iterator;
import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public class ManyToOne extends ToOne {
   private boolean ignoreNotFound;
   private boolean isLogicalOneToOne;

   public ManyToOne(Mappings mappings, Table table) {
      super(mappings, table);
   }

   public Type getType() throws MappingException {
      return this.getMappings().getTypeResolver().getTypeFactory().manyToOne(this.getReferencedEntityName(), this.getReferencedPropertyName(), this.isLazy(), this.isUnwrapProxy(), this.isIgnoreNotFound(), this.isLogicalOneToOne);
   }

   public void createForeignKey() throws MappingException {
      if (this.referencedPropertyName == null && !this.hasFormula()) {
         this.createForeignKeyOfEntity(((EntityType)this.getType()).getAssociatedEntityName());
      }

   }

   public void createPropertyRefConstraints(java.util.Map persistentClasses) {
      if (this.referencedPropertyName != null) {
         PersistentClass pc = (PersistentClass)persistentClasses.get(this.getReferencedEntityName());
         Property property = pc.getReferencedProperty(this.getReferencedPropertyName());
         if (property == null) {
            throw new MappingException("Could not find property " + this.getReferencedPropertyName() + " on " + this.getReferencedEntityName());
         }

         if (!this.hasFormula() && !"none".equals(this.getForeignKeyName())) {
            java.util.List refColumns = new ArrayList();
            Iterator iter = property.getColumnIterator();

            while(iter.hasNext()) {
               Column col = (Column)iter.next();
               refColumns.add(col);
            }

            ForeignKey fk = this.getTable().createForeignKey(this.getForeignKeyName(), this.getConstraintColumns(), ((EntityType)this.getType()).getAssociatedEntityName(), refColumns);
            fk.setCascadeDeleteEnabled(this.isCascadeDeleteEnabled());
         }
      }

   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }

   public boolean isIgnoreNotFound() {
      return this.ignoreNotFound;
   }

   public void setIgnoreNotFound(boolean ignoreNotFound) {
      this.ignoreNotFound = ignoreNotFound;
   }

   public void markAsLogicalOneToOne() {
      this.isLogicalOneToOne = true;
   }

   public boolean isLogicalOneToOne() {
      return this.isLogicalOneToOne;
   }
}
