package org.hibernate.id.enhanced;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class SequenceStructure implements DatabaseStructure {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SequenceStructure.class.getName());
   private final String sequenceName;
   private final int initialValue;
   private final int incrementSize;
   private final Class numberType;
   private final String sql;
   private boolean applyIncrementSizeToSourceValues;
   private int accessCounter;

   public SequenceStructure(Dialect dialect, String sequenceName, int initialValue, int incrementSize, Class numberType) {
      this.sequenceName = sequenceName;
      this.initialValue = initialValue;
      this.incrementSize = incrementSize;
      this.numberType = numberType;
      this.sql = dialect.getSequenceNextValString(sequenceName);
   }

   public String getName() {
      return this.sequenceName;
   }

   public int getIncrementSize() {
      return this.incrementSize;
   }

   public int getTimesAccessed() {
      return this.accessCounter;
   }

   public int getInitialValue() {
      return this.initialValue;
   }

   public AccessCallback buildCallback(final SessionImplementor session) {
      return new AccessCallback() {
         public IntegralDataTypeHolder getNextValue() {
            SequenceStructure.this.accessCounter++;

            try {
               PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(SequenceStructure.this.sql);

               IntegralDataTypeHolder var4;
               try {
                  ResultSet rs = st.executeQuery();

                  try {
                     rs.next();
                     IntegralDataTypeHolder value = IdentifierGeneratorHelper.getIntegralDataTypeHolder(SequenceStructure.this.numberType);
                     value.initialize(rs, 1L);
                     if (SequenceStructure.LOG.isDebugEnabled()) {
                        SequenceStructure.LOG.debugf("Sequence value obtained: %s", value.makeValue());
                     }

                     var4 = value;
                  } finally {
                     try {
                        rs.close();
                     } catch (Throwable var19) {
                     }

                  }
               } finally {
                  st.close();
               }

               return var4;
            } catch (SQLException sqle) {
               throw session.getFactory().getSQLExceptionHelper().convert(sqle, "could not get next sequence value", SequenceStructure.this.sql);
            }
         }
      };
   }

   public void prepare(Optimizer optimizer) {
      this.applyIncrementSizeToSourceValues = optimizer.applyIncrementSizeToSourceValues();
   }

   public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
      int sourceIncrementSize = this.applyIncrementSizeToSourceValues ? this.incrementSize : 1;
      return dialect.getCreateSequenceStrings(this.sequenceName, this.initialValue, sourceIncrementSize);
   }

   public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
      return dialect.getDropSequenceStrings(this.sequenceName);
   }

   public boolean isPhysicalSequence() {
      return true;
   }
}
