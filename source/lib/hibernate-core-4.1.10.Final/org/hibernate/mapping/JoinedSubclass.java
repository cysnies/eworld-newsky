package org.hibernate.mapping;

import java.util.Iterator;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;

public class JoinedSubclass extends Subclass implements TableOwner {
   private Table table;
   private KeyValue key;

   public JoinedSubclass(PersistentClass superclass) {
      super(superclass);
   }

   public Table getTable() {
      return this.table;
   }

   public void setTable(Table table) {
      this.table = table;
      this.getSuperclass().addSubclassTable(table);
   }

   public KeyValue getKey() {
      return this.key;
   }

   public void setKey(KeyValue key) {
      this.key = key;
   }

   public void validate(Mapping mapping) throws MappingException {
      super.validate(mapping);
      if (this.key != null && !this.key.isValid(mapping)) {
         throw new MappingException("subclass key mapping has wrong number of columns: " + this.getEntityName() + " type: " + this.key.getType().getName());
      }
   }

   public Iterator getReferenceablePropertyIterator() {
      return this.getPropertyIterator();
   }

   public Object accept(PersistentClassVisitor mv) {
      return mv.accept(this);
   }
}
