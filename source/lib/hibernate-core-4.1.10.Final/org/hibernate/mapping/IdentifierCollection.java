package org.hibernate.mapping;

import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.engine.spi.Mapping;

public abstract class IdentifierCollection extends Collection {
   public static final String DEFAULT_IDENTIFIER_COLUMN_NAME = "id";
   private KeyValue identifier;

   public IdentifierCollection(Mappings mappings, PersistentClass owner) {
      super(mappings, owner);
   }

   public KeyValue getIdentifier() {
      return this.identifier;
   }

   public void setIdentifier(KeyValue identifier) {
      this.identifier = identifier;
   }

   public final boolean isIdentified() {
      return true;
   }

   void createPrimaryKey() {
      if (!this.isOneToMany()) {
         PrimaryKey pk = new PrimaryKey();
         pk.addColumns(this.getIdentifier().getColumnIterator());
         this.getCollectionTable().setPrimaryKey(pk);
      }

   }

   public void validate(Mapping mapping) throws MappingException {
      super.validate(mapping);
      if (!this.getIdentifier().isValid(mapping)) {
         throw new MappingException("collection id mapping has wrong number of columns: " + this.getRole() + " type: " + this.getIdentifier().getType().getName());
      }
   }
}
