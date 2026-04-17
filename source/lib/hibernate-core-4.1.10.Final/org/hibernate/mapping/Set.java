package org.hibernate.mapping;

import java.util.Iterator;
import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.type.CollectionType;

public class Set extends Collection {
   public void validate(Mapping mapping) throws MappingException {
      super.validate(mapping);
   }

   public Set(Mappings mappings, PersistentClass owner) {
      super(mappings, owner);
   }

   public boolean isSet() {
      return true;
   }

   public CollectionType getDefaultCollectionType() {
      if (this.isSorted()) {
         return this.getMappings().getTypeResolver().getTypeFactory().sortedSet(this.getRole(), this.getReferencedPropertyName(), this.getComparator());
      } else {
         return this.hasOrder() ? this.getMappings().getTypeResolver().getTypeFactory().orderedSet(this.getRole(), this.getReferencedPropertyName()) : this.getMappings().getTypeResolver().getTypeFactory().set(this.getRole(), this.getReferencedPropertyName());
      }
   }

   void createPrimaryKey() {
      if (!this.isOneToMany()) {
         PrimaryKey pk = new PrimaryKey();
         pk.addColumns(this.getKey().getColumnIterator());
         Iterator iter = this.getElement().getColumnIterator();

         while(iter.hasNext()) {
            Object selectable = iter.next();
            if (selectable instanceof Column) {
               Column col = (Column)selectable;
               if (!col.isNullable()) {
                  pk.addColumn(col);
               }
            }
         }

         if (pk.getColumnSpan() != this.getKey().getColumnSpan()) {
            this.getCollectionTable().setPrimaryKey(pk);
         }
      }

   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }
}
