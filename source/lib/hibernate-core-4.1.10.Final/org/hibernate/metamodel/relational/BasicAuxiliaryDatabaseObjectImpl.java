package org.hibernate.metamodel.relational;

import java.util.Set;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;

public class BasicAuxiliaryDatabaseObjectImpl extends AbstractAuxiliaryDatabaseObject {
   private static final String CATALOG_NAME_PLACEHOLDER = "${catalog}";
   private static final String SCHEMA_NAME_PLACEHOLDER = "${schema}";
   private final Schema defaultSchema;
   private final String createString;
   private final String dropString;

   public BasicAuxiliaryDatabaseObjectImpl(Schema defaultSchema, String createString, String dropString, Set dialectScopes) {
      super(dialectScopes);
      this.defaultSchema = defaultSchema;
      this.createString = createString;
      this.dropString = dropString;
   }

   public String[] sqlCreateStrings(Dialect dialect) {
      return new String[]{injectCatalogAndSchema(this.createString, this.defaultSchema)};
   }

   public String[] sqlDropStrings(Dialect dialect) {
      return new String[]{injectCatalogAndSchema(this.dropString, this.defaultSchema)};
   }

   private static String injectCatalogAndSchema(String ddlString, Schema schema) {
      String rtn = StringHelper.replace(ddlString, "${catalog}", schema.getName().getCatalog().getName());
      rtn = StringHelper.replace(rtn, "${schema}", schema.getName().getSchema().getName());
      return rtn;
   }
}
