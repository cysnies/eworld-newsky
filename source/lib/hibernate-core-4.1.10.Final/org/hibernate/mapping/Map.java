package org.hibernate.mapping;

import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.type.CollectionType;

public class Map extends IndexedCollection {
   public Map(Mappings mappings, PersistentClass owner) {
      super(mappings, owner);
   }

   public boolean isMap() {
      return true;
   }

   public CollectionType getDefaultCollectionType() {
      if (this.isSorted()) {
         return this.getMappings().getTypeResolver().getTypeFactory().sortedMap(this.getRole(), this.getReferencedPropertyName(), this.getComparator());
      } else {
         return this.hasOrder() ? this.getMappings().getTypeResolver().getTypeFactory().orderedMap(this.getRole(), this.getReferencedPropertyName()) : this.getMappings().getTypeResolver().getTypeFactory().map(this.getRole(), this.getReferencedPropertyName());
      }
   }

   public void createAllKeys() throws MappingException {
      super.createAllKeys();
      if (!this.isInverse()) {
         this.getIndex().createForeignKey();
      }

   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }
}
