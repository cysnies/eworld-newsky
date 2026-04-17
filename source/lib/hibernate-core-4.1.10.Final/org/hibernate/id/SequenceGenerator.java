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
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class SequenceGenerator implements PersistentIdentifierGenerator, BulkInsertionCapableIdentifierGenerator, Configurable {
   private static final Logger LOG = Logger.getLogger(SequenceGenerator.class.getName());
   public static final String SEQUENCE = "sequence";
   public static final String PARAMETERS = "parameters";
   private String sequenceName;
   private String parameters;
   private Type identifierType;
   private String sql;

   protected Type getIdentifierType() {
      return this.identifierType;
   }

   public Object generatorKey() {
      return this.getSequenceName();
   }

   public String getSequenceName() {
      return this.sequenceName;
   }

   public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
      ObjectNameNormalizer normalizer = (ObjectNameNormalizer)params.get("identifier_normalizer");
      this.sequenceName = normalizer.normalizeIdentifierQuoting(ConfigurationHelper.getString("sequence", params, "hibernate_sequence"));
      this.parameters = params.getProperty("parameters");
      if (this.sequenceName.indexOf(46) < 0) {
         String schemaName = normalizer.normalizeIdentifierQuoting(params.getProperty("schema"));
         String catalogName = normalizer.normalizeIdentifierQuoting(params.getProperty("catalog"));
         this.sequenceName = Table.qualify(dialect.quote(catalogName), dialect.quote(schemaName), dialect.quote(this.sequenceName));
      }

      this.identifierType = type;
      this.sql = dialect.getSequenceNextValString(this.sequenceName);
   }

   public Serializable generate(SessionImplementor session, Object obj) {
      return this.generateHolder(session).makeValue();
   }

   protected IntegralDataTypeHolder generateHolder(SessionImplementor session) {
      try {
         PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.sql);

         IntegralDataTypeHolder var5;
         try {
            ResultSet rs = st.executeQuery();

            try {
               rs.next();
               IntegralDataTypeHolder result = this.buildHolder();
               result.initialize(rs, 1L);
               LOG.debugf("Sequence identifier generated: %s", result);
               var5 = result;
            } finally {
               rs.close();
            }
         } finally {
            st.close();
         }

         return var5;
      } catch (SQLException sqle) {
         throw session.getFactory().getSQLExceptionHelper().convert(sqle, "could not get next sequence value", this.sql);
      }
   }

   protected IntegralDataTypeHolder buildHolder() {
      return IdentifierGeneratorHelper.getIntegralDataTypeHolder(this.identifierType.getReturnedClass());
   }

   public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
      String[] ddl = dialect.getCreateSequenceStrings(this.sequenceName);
      if (this.parameters != null) {
         ddl[ddl.length - 1] = ddl[ddl.length - 1] + ' ' + this.parameters;
      }

      return ddl;
   }

   public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
      return dialect.getDropSequenceStrings(this.sequenceName);
   }

   public boolean supportsBulkInsertionIdentifierGeneration() {
      return true;
   }

   public String determineBulkInsertionIdentifierGenerationSelectFragment(Dialect dialect) {
      return dialect.getSelectSequenceNextValString(this.getSequenceName());
   }
}
