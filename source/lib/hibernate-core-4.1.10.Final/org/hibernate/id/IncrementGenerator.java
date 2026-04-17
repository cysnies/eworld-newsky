package org.hibernate.id;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class IncrementGenerator implements IdentifierGenerator, Configurable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, IncrementGenerator.class.getName());
   private Class returnClass;
   private String sql;
   private IntegralDataTypeHolder previousValueHolder;

   public synchronized Serializable generate(SessionImplementor session, Object object) throws HibernateException {
      if (this.sql != null) {
         this.initializePreviousValueHolder(session);
      }

      return this.previousValueHolder.makeValueThenIncrement();
   }

   public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
      this.returnClass = type.getReturnedClass();
      ObjectNameNormalizer normalizer = (ObjectNameNormalizer)params.get("identifier_normalizer");
      String column = params.getProperty("column");
      if (column == null) {
         column = params.getProperty("target_column");
      }

      column = dialect.quote(normalizer.normalizeIdentifierQuoting(column));
      String tableList = params.getProperty("tables");
      if (tableList == null) {
         tableList = params.getProperty("identity_tables");
      }

      String[] tables = StringHelper.split(", ", tableList);
      String schema = dialect.quote(normalizer.normalizeIdentifierQuoting(params.getProperty("schema")));
      String catalog = dialect.quote(normalizer.normalizeIdentifierQuoting(params.getProperty("catalog")));
      StringBuilder buf = new StringBuilder();

      for(int i = 0; i < tables.length; ++i) {
         String tableName = dialect.quote(normalizer.normalizeIdentifierQuoting(tables[i]));
         if (tables.length > 1) {
            buf.append("select max(").append(column).append(") as mx from ");
         }

         buf.append(Table.qualify(catalog, schema, tableName));
         if (i < tables.length - 1) {
            buf.append(" union ");
         }
      }

      if (tables.length > 1) {
         buf.insert(0, "( ").append(" ) ids_");
         column = "ids_.mx";
      }

      this.sql = "select max(" + column + ") from " + buf.toString();
   }

   private void initializePreviousValueHolder(SessionImplementor session) {
      this.previousValueHolder = IdentifierGeneratorHelper.getIntegralDataTypeHolder(this.returnClass);
      LOG.debugf("Fetching initial value: %s", this.sql);

      try {
         PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.sql);

         try {
            ResultSet rs = st.executeQuery();

            try {
               if (rs.next()) {
                  this.previousValueHolder.initialize(rs, 0L).increment();
               } else {
                  this.previousValueHolder.initialize(1L);
               }

               this.sql = null;
               if (LOG.isDebugEnabled()) {
                  LOG.debugf("First free id: %s", this.previousValueHolder.makeValue());
               }
            } finally {
               rs.close();
            }
         } finally {
            st.close();
         }

      } catch (SQLException sqle) {
         throw session.getFactory().getSQLExceptionHelper().convert(sqle, "could not fetch initial value for increment generator", this.sql);
      }
   }
}
