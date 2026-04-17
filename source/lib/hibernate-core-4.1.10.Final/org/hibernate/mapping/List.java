package org.hibernate.mapping;

import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.type.CollectionType;

public class List extends IndexedCollection {
   private int baseIndex;

   public boolean isList() {
      return true;
   }

   public List(Mappings mappings, PersistentClass owner) {
      super(mappings, owner);
   }

   public CollectionType getDefaultCollectionType() throws MappingException {
      return this.getMappings().getTypeResolver().getTypeFactory().list(this.getRole(), this.getReferencedPropertyName());
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }

   public int getBaseIndex() {
      return this.baseIndex;
   }

   public void setBaseIndex(int baseIndex) {
      this.baseIndex = baseIndex;
   }
}
