package org.hibernate.mapping;

import org.hibernate.cfg.Mappings;
import org.hibernate.type.CollectionType;

public class Bag extends Collection {
   public Bag(Mappings mappings, PersistentClass owner) {
      super(mappings, owner);
   }

   public CollectionType getDefaultCollectionType() {
      return this.getMappings().getTypeResolver().getTypeFactory().bag(this.getRole(), this.getReferencedPropertyName());
   }

   void createPrimaryKey() {
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }
}
