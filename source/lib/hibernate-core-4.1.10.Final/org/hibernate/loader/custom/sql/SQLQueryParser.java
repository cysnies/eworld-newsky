package org.hibernate.loader.custom.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.QueryException;
import org.hibernate.engine.query.spi.ParameterParser;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.SQLLoadableCollection;
import org.hibernate.persister.entity.SQLLoadable;

public class SQLQueryParser {
   private static final String HIBERNATE_PLACEHOLDER_PREFIX = "h-";
   private static final String DOMAIN_PLACEHOLDER = "h-domain";
   private static final String CATALOG_PLACEHOLDER = "h-catalog";
   private static final String SCHEMA_PLACEHOLDER = "h-schema";
   private final SessionFactoryImplementor factory;
   private final String originalQueryString;
   private final ParserContext context;
   private final Map namedParameters = new HashMap();
   private long aliasesFound = 0L;

   public SQLQueryParser(String queryString, ParserContext context, SessionFactoryImplementor factory) {
      this.originalQueryString = queryString;
      this.context = context;
      this.factory = factory;
   }

   public Map getNamedParameters() {
      return this.namedParameters;
   }

   public boolean queryHasAliases() {
      return this.aliasesFound > 0L;
   }

   public String process() {
      String processedSql = this.substituteBrackets(this.originalQueryString);
      processedSql = this.substituteParams(processedSql);
      return processedSql;
   }

   private String substituteBrackets(String sqlQuery) throws QueryException {
      StringBuilder result = new StringBuilder(sqlQuery.length() + 20);

      int right;
      for(int curr = 0; curr < sqlQuery.length(); curr = right + 1) {
         int left;
         if ((left = sqlQuery.indexOf(123, curr)) < 0) {
            result.append(sqlQuery.substring(curr));
            break;
         }

         result.append(sqlQuery.substring(curr, left));
         if ((right = sqlQuery.indexOf(125, left + 1)) < 0) {
            throw new QueryException("Unmatched braces for alias path", sqlQuery);
         }

         String aliasPath = sqlQuery.substring(left + 1, right);
         boolean isPlaceholder = aliasPath.startsWith("h-");
         if (isPlaceholder) {
            if ("h-domain".equals(aliasPath)) {
               String catalogName = this.factory.getSettings().getDefaultCatalogName();
               if (catalogName != null) {
                  result.append(catalogName);
                  result.append(".");
               }

               String schemaName = this.factory.getSettings().getDefaultSchemaName();
               if (schemaName != null) {
                  result.append(schemaName);
                  result.append(".");
               }
            } else if ("h-schema".equals(aliasPath)) {
               String schemaName = this.factory.getSettings().getDefaultSchemaName();
               if (schemaName != null) {
                  result.append(schemaName);
                  result.append(".");
               }
            } else {
               if (!"h-catalog".equals(aliasPath)) {
                  throw new QueryException("Unknown placeholder ", aliasPath);
               }

               String catalogName = this.factory.getSettings().getDefaultCatalogName();
               if (catalogName != null) {
                  result.append(catalogName);
                  result.append(".");
               }
            }
         } else {
            int firstDot = aliasPath.indexOf(46);
            if (firstDot == -1) {
               if (this.context.isEntityAlias(aliasPath)) {
                  result.append(aliasPath);
                  ++this.aliasesFound;
               } else {
                  result.append('{').append(aliasPath).append('}');
               }
            } else {
               String aliasName = aliasPath.substring(0, firstDot);
               if (this.context.isCollectionAlias(aliasName)) {
                  String propertyName = aliasPath.substring(firstDot + 1);
                  result.append(this.resolveCollectionProperties(aliasName, propertyName));
                  ++this.aliasesFound;
               } else if (this.context.isEntityAlias(aliasName)) {
                  String propertyName = aliasPath.substring(firstDot + 1);
                  result.append(this.resolveProperties(aliasName, propertyName));
                  ++this.aliasesFound;
               } else {
                  result.append('{').append(aliasPath).append('}');
               }
            }
         }
      }

      return result.toString();
   }

   private String resolveCollectionProperties(String aliasName, String propertyName) {
      Map fieldResults = this.context.getPropertyResultsMapByAlias(aliasName);
      SQLLoadableCollection collectionPersister = this.context.getCollectionPersisterByAlias(aliasName);
      String collectionSuffix = this.context.getCollectionSuffixByAlias(aliasName);
      if ("*".equals(propertyName)) {
         if (!fieldResults.isEmpty()) {
            throw new QueryException("Using return-propertys together with * syntax is not supported.");
         } else {
            String selectFragment = collectionPersister.selectFragment(aliasName, collectionSuffix);
            ++this.aliasesFound;
            return selectFragment + ", " + this.resolveProperties(aliasName, propertyName);
         }
      } else if ("element.*".equals(propertyName)) {
         return this.resolveProperties(aliasName, "*");
      } else {
         String[] columnAliases = (String[])fieldResults.get(propertyName);
         if (columnAliases == null) {
            columnAliases = collectionPersister.getCollectionPropertyColumnAliases(propertyName, collectionSuffix);
         }

         if (columnAliases != null && columnAliases.length != 0) {
            if (columnAliases.length != 1) {
               throw new QueryException("SQL queries only support properties mapped to a single column - property [" + propertyName + "] is mapped to " + columnAliases.length + " columns.", this.originalQueryString);
            } else {
               ++this.aliasesFound;
               return columnAliases[0];
            }
         } else {
            throw new QueryException("No column name found for property [" + propertyName + "] for alias [" + aliasName + "]", this.originalQueryString);
         }
      }
   }

   private String resolveProperties(String aliasName, String propertyName) {
      Map fieldResults = this.context.getPropertyResultsMapByAlias(aliasName);
      SQLLoadable persister = this.context.getEntityPersisterByAlias(aliasName);
      String suffix = this.context.getEntitySuffixByAlias(aliasName);
      if ("*".equals(propertyName)) {
         if (!fieldResults.isEmpty()) {
            throw new QueryException("Using return-propertys together with * syntax is not supported.");
         } else {
            ++this.aliasesFound;
            return persister.selectFragment(aliasName, suffix);
         }
      } else {
         String[] columnAliases = (String[])fieldResults.get(propertyName);
         if (columnAliases == null) {
            columnAliases = persister.getSubclassPropertyColumnAliases(propertyName, suffix);
         }

         if (columnAliases != null && columnAliases.length != 0) {
            if (columnAliases.length != 1) {
               throw new QueryException("SQL queries only support properties mapped to a single column - property [" + propertyName + "] is mapped to " + columnAliases.length + " columns.", this.originalQueryString);
            } else {
               ++this.aliasesFound;
               return columnAliases[0];
            }
         } else {
            throw new QueryException("No column name found for property [" + propertyName + "] for alias [" + aliasName + "]", this.originalQueryString);
         }
      }
   }

   private String substituteParams(String sqlString) {
      ParameterSubstitutionRecognizer recognizer = new ParameterSubstitutionRecognizer();
      ParameterParser.parse(sqlString, recognizer);
      this.namedParameters.clear();
      this.namedParameters.putAll(recognizer.namedParameterBindPoints);
      return recognizer.result.toString();
   }

   public static class ParameterSubstitutionRecognizer implements ParameterParser.Recognizer {
      StringBuilder result = new StringBuilder();
      Map namedParameterBindPoints = new HashMap();
      int parameterCount = 0;

      public void outParameter(int position) {
         this.result.append('?');
      }

      public void ordinalParameter(int position) {
         this.result.append('?');
      }

      public void namedParameter(String name, int position) {
         this.addNamedParameter(name);
         this.result.append('?');
      }

      public void jpaPositionalParameter(String name, int position) {
         this.namedParameter(name, position);
      }

      public void other(char character) {
         this.result.append(character);
      }

      private void addNamedParameter(String name) {
         Integer loc = this.parameterCount++;
         Object o = this.namedParameterBindPoints.get(name);
         if (o == null) {
            this.namedParameterBindPoints.put(name, loc);
         } else if (o instanceof Integer) {
            ArrayList list = new ArrayList(4);
            list.add(o);
            list.add(loc);
            this.namedParameterBindPoints.put(name, list);
         } else {
            ((List)o).add(loc);
         }

      }
   }

   interface ParserContext {
      boolean isEntityAlias(String var1);

      SQLLoadable getEntityPersisterByAlias(String var1);

      String getEntitySuffixByAlias(String var1);

      boolean isCollectionAlias(String var1);

      SQLLoadableCollection getCollectionPersisterByAlias(String var1);

      String getCollectionSuffixByAlias(String var1);

      Map getPropertyResultsMapByAlias(String var1);
   }
}
