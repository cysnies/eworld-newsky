package org.hibernate.metamodel.relational;

import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.metamodel.ValidationException;
import org.jboss.logging.Logger;

public abstract class AbstractSimpleValue implements SimpleValue {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractSimpleValue.class.getName());
   private final TableSpecification table;
   private final int position;
   private Datatype datatype;

   protected AbstractSimpleValue(TableSpecification table, int position) {
      this.table = table;
      this.position = position;
   }

   public TableSpecification getTable() {
      return this.table;
   }

   public int getPosition() {
      return this.position;
   }

   public Datatype getDatatype() {
      return this.datatype;
   }

   public void setDatatype(Datatype datatype) {
      LOG.debugf("setting datatype for column %s : %s", this.toLoggableString(), datatype);
      if (this.datatype != null && !this.datatype.equals(datatype)) {
         LOG.debugf("overriding previous datatype : %s", this.datatype);
      }

      this.datatype = datatype;
   }

   public void validateJdbcTypes(Value.JdbcCodes typeCodes) {
      if (this.datatype.getTypeCode() != typeCodes.nextJdbcCde()) {
         throw new ValidationException("Mismatched types");
      }
   }
}
