package org.hibernate.metamodel.relational;

import java.util.Collections;
import org.hibernate.dialect.Dialect;

public class InLineView extends AbstractTableSpecification {
   private final Schema schema;
   private final String logicalName;
   private final String select;

   public InLineView(Schema schema, String logicalName, String select) {
      this.schema = schema;
      this.logicalName = logicalName;
      this.select = select;
   }

   public Schema getSchema() {
      return this.schema;
   }

   public String getSelect() {
      return this.select;
   }

   public String getLoggableValueQualifier() {
      return this.logicalName;
   }

   public Iterable getIndexes() {
      return Collections.emptyList();
   }

   public Index getOrCreateIndex(String name) {
      throw new UnsupportedOperationException("Cannot create index on inline view");
   }

   public Iterable getUniqueKeys() {
      return Collections.emptyList();
   }

   public UniqueKey getOrCreateUniqueKey(String name) {
      throw new UnsupportedOperationException("Cannot create unique-key on inline view");
   }

   public Iterable getCheckConstraints() {
      return Collections.emptyList();
   }

   public void addCheckConstraint(String checkCondition) {
      throw new UnsupportedOperationException("Cannot create check constraint on inline view");
   }

   public Iterable getComments() {
      return Collections.emptyList();
   }

   public void addComment(String comment) {
      throw new UnsupportedOperationException("Cannot comment on inline view");
   }

   public String getQualifiedName(Dialect dialect) {
      return (new StringBuilder(this.select.length() + 4)).append("( ").append(this.select).append(" )").toString();
   }

   public String toLoggableString() {
      return "{inline-view}";
   }
}
