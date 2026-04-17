package org.hibernate.tuple;

import org.hibernate.engine.spi.IdentifierValue;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PostInsertIdentifierGenerator;
import org.hibernate.type.Type;

public class IdentifierProperty extends Property {
   private boolean virtual;
   private boolean embedded;
   private IdentifierValue unsavedValue;
   private IdentifierGenerator identifierGenerator;
   private boolean identifierAssignedByInsert;
   private boolean hasIdentifierMapper;

   public IdentifierProperty(String name, String node, Type type, boolean embedded, IdentifierValue unsavedValue, IdentifierGenerator identifierGenerator) {
      super(name, node, type);
      this.virtual = false;
      this.embedded = embedded;
      this.hasIdentifierMapper = false;
      this.unsavedValue = unsavedValue;
      this.identifierGenerator = identifierGenerator;
      this.identifierAssignedByInsert = identifierGenerator instanceof PostInsertIdentifierGenerator;
   }

   public IdentifierProperty(Type type, boolean embedded, boolean hasIdentifierMapper, IdentifierValue unsavedValue, IdentifierGenerator identifierGenerator) {
      super((String)null, (String)null, type);
      this.virtual = true;
      this.embedded = embedded;
      this.hasIdentifierMapper = hasIdentifierMapper;
      this.unsavedValue = unsavedValue;
      this.identifierGenerator = identifierGenerator;
      this.identifierAssignedByInsert = identifierGenerator instanceof PostInsertIdentifierGenerator;
   }

   public boolean isVirtual() {
      return this.virtual;
   }

   public boolean isEmbedded() {
      return this.embedded;
   }

   public IdentifierValue getUnsavedValue() {
      return this.unsavedValue;
   }

   public IdentifierGenerator getIdentifierGenerator() {
      return this.identifierGenerator;
   }

   public boolean isIdentifierAssignedByInsert() {
      return this.identifierAssignedByInsert;
   }

   public boolean hasIdentifierMapper() {
      return this.hasIdentifierMapper;
   }
}
