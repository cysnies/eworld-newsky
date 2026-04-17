package org.hibernate.mapping;

import java.util.Iterator;
import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.engine.spi.Mapping;

public abstract class IndexedCollection extends Collection {
   public static final String DEFAULT_INDEX_COLUMN_NAME = "idx";
   private Value index;
   private String indexNodeName;

   public IndexedCollection(Mappings mappings, PersistentClass owner) {
      super(mappings, owner);
   }

   public Value getIndex() {
      return this.index;
   }

   public void setIndex(Value index) {
      this.index = index;
   }

   public final boolean isIndexed() {
      return true;
   }

   void createPrimaryKey() {
      if (!this.isOneToMany()) {
         PrimaryKey pk = new PrimaryKey();
         pk.addColumns(this.getKey().getColumnIterator());
         boolean isFormula = false;
         Iterator iter = this.getIndex().getColumnIterator();

         while(iter.hasNext()) {
            if (((Selectable)iter.next()).isFormula()) {
               isFormula = true;
            }
         }

         if (isFormula) {
            pk.addColumns(this.getElement().getColumnIterator());
         } else {
            pk.addColumns(this.getIndex().getColumnIterator());
         }

         this.getCollectionTable().setPrimaryKey(pk);
      }

   }

   public void validate(Mapping mapping) throws MappingException {
      super.validate(mapping);
      if (!this.getIndex().isValid(mapping)) {
         throw new MappingException("collection index mapping has wrong number of columns: " + this.getRole() + " type: " + this.getIndex().getType().getName());
      } else if (this.indexNodeName != null && !this.indexNodeName.startsWith("@")) {
         throw new MappingException("index node must be an attribute: " + this.indexNodeName);
      }
   }

   public boolean isList() {
      return false;
   }

   public String getIndexNodeName() {
      return this.indexNodeName;
   }

   public void setIndexNodeName(String indexNodeName) {
      this.indexNodeName = indexNodeName;
   }
}
