package org.hibernate.metamodel.relational;

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;

public class Sequence implements Exportable {
   private final Schema schema;
   private final String name;
   private final String qualifiedName;
   private int initialValue;
   private int incrementSize;

   public Sequence(Schema schema, String name) {
      this.initialValue = 1;
      this.incrementSize = 1;
      this.schema = schema;
      this.name = name;
      this.qualifiedName = (new ObjectName(schema, name)).toText();
   }

   public Sequence(Schema schema, String name, int initialValue, int incrementSize) {
      this(schema, name);
      this.initialValue = initialValue;
      this.incrementSize = incrementSize;
   }

   public Schema getSchema() {
      return this.schema;
   }

   public String getName() {
      return this.name;
   }

   public String getExportIdentifier() {
      return this.qualifiedName;
   }

   public int getInitialValue() {
      return this.initialValue;
   }

   public int getIncrementSize() {
      return this.incrementSize;
   }

   public String[] sqlCreateStrings(Dialect dialect) throws MappingException {
      return dialect.getCreateSequenceStrings(this.name, this.initialValue, this.incrementSize);
   }

   public String[] sqlDropStrings(Dialect dialect) throws MappingException {
      return dialect.getDropSequenceStrings(this.name);
   }
}
