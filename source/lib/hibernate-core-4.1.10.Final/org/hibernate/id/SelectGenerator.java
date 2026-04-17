package org.hibernate.id;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.ValueInclusion;
import org.hibernate.id.insert.AbstractSelectingDelegate;
import org.hibernate.id.insert.IdentifierGeneratingInsert;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.type.Type;

public class SelectGenerator extends AbstractPostInsertGenerator implements Configurable {
   private String uniqueKeyPropertyName;

   public void configure(Type type, Properties params, Dialect d) throws MappingException {
      this.uniqueKeyPropertyName = params.getProperty("key");
   }

   public InsertGeneratedIdentifierDelegate getInsertGeneratedIdentifierDelegate(PostInsertIdentityPersister persister, Dialect dialect, boolean isGetGeneratedKeysEnabled) throws HibernateException {
      return new SelectGeneratorDelegate(persister, dialect, this.uniqueKeyPropertyName);
   }

   private static String determineNameOfPropertyToUse(PostInsertIdentityPersister persister, String supplied) {
      if (supplied != null) {
         return supplied;
      } else {
         int[] naturalIdPropertyIndices = persister.getNaturalIdentifierProperties();
         if (naturalIdPropertyIndices == null) {
            throw new IdentifierGenerationException("no natural-id property defined; need to specify [key] in generator parameters");
         } else if (naturalIdPropertyIndices.length > 1) {
            throw new IdentifierGenerationException("select generator does not currently support composite natural-id properties; need to specify [key] in generator parameters");
         } else {
            ValueInclusion inclusion = persister.getPropertyInsertGenerationInclusions()[naturalIdPropertyIndices[0]];
            if (inclusion != ValueInclusion.NONE) {
               throw new IdentifierGenerationException("natural-id also defined as insert-generated; need to specify [key] in generator parameters");
            } else {
               return persister.getPropertyNames()[naturalIdPropertyIndices[0]];
            }
         }
      }
   }

   public static class SelectGeneratorDelegate extends AbstractSelectingDelegate implements InsertGeneratedIdentifierDelegate {
      private final PostInsertIdentityPersister persister;
      private final Dialect dialect;
      private final String uniqueKeyPropertyName;
      private final Type uniqueKeyType;
      private final Type idType;
      private final String idSelectString;

      private SelectGeneratorDelegate(PostInsertIdentityPersister persister, Dialect dialect, String suppliedUniqueKeyPropertyName) {
         super(persister);
         this.persister = persister;
         this.dialect = dialect;
         this.uniqueKeyPropertyName = SelectGenerator.determineNameOfPropertyToUse(persister, suppliedUniqueKeyPropertyName);
         this.idSelectString = persister.getSelectByUniqueKeyString(this.uniqueKeyPropertyName);
         this.uniqueKeyType = persister.getPropertyType(this.uniqueKeyPropertyName);
         this.idType = persister.getIdentifierType();
      }

      public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert() {
         return new IdentifierGeneratingInsert(this.dialect);
      }

      protected String getSelectSQL() {
         return this.idSelectString;
      }

      protected void bindParameters(SessionImplementor session, PreparedStatement ps, Object entity) throws SQLException {
         Object uniqueKeyValue = this.persister.getPropertyValue(entity, this.uniqueKeyPropertyName);
         this.uniqueKeyType.nullSafeSet(ps, uniqueKeyValue, 1, session);
      }

      protected Serializable getResult(SessionImplementor session, ResultSet rs, Object entity) throws SQLException {
         if (!rs.next()) {
            throw new IdentifierGenerationException("the inserted row could not be located by the unique key: " + this.uniqueKeyPropertyName);
         } else {
            return (Serializable)this.idType.nullSafeGet(rs, this.persister.getRootTableKeyColumnNames(), session, entity);
         }
      }
   }
}
