package org.hibernate.metamodel.binding;

import org.hibernate.metamodel.relational.SimpleValue;

public class EntityDiscriminator {
   private final HibernateTypeDescriptor explicitHibernateTypeDescriptor = new HibernateTypeDescriptor();
   private SimpleValue boundValue;
   private boolean forced;
   private boolean inserted = true;

   public SimpleValue getBoundValue() {
      return this.boundValue;
   }

   public void setBoundValue(SimpleValue boundValue) {
      this.boundValue = boundValue;
   }

   public HibernateTypeDescriptor getExplicitHibernateTypeDescriptor() {
      return this.explicitHibernateTypeDescriptor;
   }

   public boolean isForced() {
      return this.forced;
   }

   public void setForced(boolean forced) {
      this.forced = forced;
   }

   public boolean isInserted() {
      return this.inserted;
   }

   public void setInserted(boolean inserted) {
      this.inserted = inserted;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("EntityDiscriminator");
      sb.append("{boundValue=").append(this.boundValue);
      sb.append(", forced=").append(this.forced);
      sb.append(", inserted=").append(this.inserted);
      sb.append('}');
      return sb.toString();
   }
}
