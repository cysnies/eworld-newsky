package org.hibernate.mapping;

import java.util.HashSet;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.util.StringHelper;

public class SimpleAuxiliaryDatabaseObject extends AbstractAuxiliaryDatabaseObject {
   private final String sqlCreateString;
   private final String sqlDropString;

   public SimpleAuxiliaryDatabaseObject(String sqlCreateString, String sqlDropString) {
      this.sqlCreateString = sqlCreateString;
      this.sqlDropString = sqlDropString;
   }

   public SimpleAuxiliaryDatabaseObject(String sqlCreateString, String sqlDropString, HashSet dialectScopes) {
      super(dialectScopes);
      this.sqlCreateString = sqlCreateString;
      this.sqlDropString = sqlDropString;
   }

   public String sqlCreateString(Dialect dialect, Mapping p, String defaultCatalog, String defaultSchema) throws HibernateException {
      return this.injectCatalogAndSchema(this.sqlCreateString, defaultCatalog, defaultSchema);
   }

   public String sqlDropString(Dialect dialect, String defaultCatalog, String defaultSchema) {
      return this.injectCatalogAndSchema(this.sqlDropString, defaultCatalog, defaultSchema);
   }

   private String injectCatalogAndSchema(String ddlString, String defaultCatalog, String defaultSchema) {
      String rtn = StringHelper.replace(ddlString, "${catalog}", defaultCatalog);
      rtn = StringHelper.replace(rtn, "${schema}", defaultSchema);
      return rtn;
   }
}
