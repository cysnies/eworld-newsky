package org.hibernate.cache.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.transform.CacheableResultTransformer;
import org.hibernate.type.Type;

public class QueryKey implements Serializable {
   private final String sqlQueryString;
   private final Type[] positionalParameterTypes;
   private final Object[] positionalParameterValues;
   private final Map namedParameters;
   private final Integer firstRow;
   private final Integer maxRows;
   private final String tenantIdentifier;
   private final Set filterKeys;
   private final CacheableResultTransformer customTransformer;
   private transient int hashCode;

   public static QueryKey generateQueryKey(String queryString, QueryParameters queryParameters, Set filterKeys, SessionImplementor session, CacheableResultTransformer customTransformer) {
      int positionalParameterCount = queryParameters.getPositionalParameterTypes().length;
      Type[] types = new Type[positionalParameterCount];
      Object[] values = new Object[positionalParameterCount];

      for(int i = 0; i < positionalParameterCount; ++i) {
         types[i] = queryParameters.getPositionalParameterTypes()[i];
         values[i] = types[i].disassemble(queryParameters.getPositionalParameterValues()[i], session, (Object)null);
      }

      Map<String, TypedValue> namedParameters;
      if (queryParameters.getNamedParameters() == null) {
         namedParameters = null;
      } else {
         namedParameters = CollectionHelper.mapOfSize(queryParameters.getNamedParameters().size());

         for(Map.Entry namedParameterEntry : queryParameters.getNamedParameters().entrySet()) {
            namedParameters.put(namedParameterEntry.getKey(), new TypedValue(((TypedValue)namedParameterEntry.getValue()).getType(), ((TypedValue)namedParameterEntry.getValue()).getType().disassemble(((TypedValue)namedParameterEntry.getValue()).getValue(), session, (Object)null)));
         }
      }

      RowSelection selection = queryParameters.getRowSelection();
      Integer maxRows;
      Integer firstRow;
      if (selection != null) {
         firstRow = selection.getFirstRow();
         maxRows = selection.getMaxRows();
      } else {
         firstRow = null;
         maxRows = null;
      }

      return new QueryKey(queryString, types, values, namedParameters, firstRow, maxRows, filterKeys, session.getTenantIdentifier(), customTransformer);
   }

   QueryKey(String sqlQueryString, Type[] positionalParameterTypes, Object[] positionalParameterValues, Map namedParameters, Integer firstRow, Integer maxRows, Set filterKeys, String tenantIdentifier, CacheableResultTransformer customTransformer) {
      this.sqlQueryString = sqlQueryString;
      this.positionalParameterTypes = positionalParameterTypes;
      this.positionalParameterValues = positionalParameterValues;
      this.namedParameters = namedParameters;
      this.firstRow = firstRow;
      this.maxRows = maxRows;
      this.tenantIdentifier = tenantIdentifier;
      this.filterKeys = filterKeys;
      this.customTransformer = customTransformer;
      this.hashCode = this.generateHashCode();
   }

   public CacheableResultTransformer getResultTransformer() {
      return this.customTransformer;
   }

   public Map getNamedParameters() {
      return Collections.unmodifiableMap(this.namedParameters);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      this.hashCode = this.generateHashCode();
   }

   private int generateHashCode() {
      int result = 13;
      result = 37 * result + (this.firstRow == null ? 0 : this.firstRow.hashCode());
      result = 37 * result + (this.maxRows == null ? 0 : this.maxRows.hashCode());

      for(int i = 0; i < this.positionalParameterValues.length; ++i) {
         result = 37 * result + (this.positionalParameterValues[i] == null ? 0 : this.positionalParameterTypes[i].getHashCode(this.positionalParameterValues[i]));
      }

      result = 37 * result + (this.namedParameters == null ? 0 : this.namedParameters.hashCode());
      result = 37 * result + (this.filterKeys == null ? 0 : this.filterKeys.hashCode());
      result = 37 * result + (this.customTransformer == null ? 0 : this.customTransformer.hashCode());
      result = 37 * result + (this.tenantIdentifier == null ? 0 : this.tenantIdentifier.hashCode());
      result = 37 * result + this.sqlQueryString.hashCode();
      return result;
   }

   public boolean equals(Object other) {
      if (!(other instanceof QueryKey)) {
         return false;
      } else {
         QueryKey that = (QueryKey)other;
         if (!this.sqlQueryString.equals(that.sqlQueryString)) {
            return false;
         } else if (EqualsHelper.equals(this.firstRow, that.firstRow) && EqualsHelper.equals(this.maxRows, that.maxRows)) {
            if (!EqualsHelper.equals(this.customTransformer, that.customTransformer)) {
               return false;
            } else {
               if (this.positionalParameterTypes == null) {
                  if (that.positionalParameterTypes != null) {
                     return false;
                  }
               } else {
                  if (that.positionalParameterTypes == null) {
                     return false;
                  }

                  if (this.positionalParameterTypes.length != that.positionalParameterTypes.length) {
                     return false;
                  }

                  for(int i = 0; i < this.positionalParameterTypes.length; ++i) {
                     if (this.positionalParameterTypes[i].getReturnedClass() != that.positionalParameterTypes[i].getReturnedClass()) {
                        return false;
                     }

                     if (!this.positionalParameterTypes[i].isEqual(this.positionalParameterValues[i], that.positionalParameterValues[i])) {
                        return false;
                     }
                  }
               }

               return EqualsHelper.equals(this.filterKeys, that.filterKeys) && EqualsHelper.equals(this.namedParameters, that.namedParameters) && EqualsHelper.equals(this.tenantIdentifier, that.tenantIdentifier);
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      return this.hashCode;
   }

   public String toString() {
      StringBuilder buffer = (new StringBuilder("sql: ")).append(this.sqlQueryString);
      if (this.positionalParameterValues != null) {
         buffer.append("; parameters: ");

         for(Object positionalParameterValue : this.positionalParameterValues) {
            buffer.append(positionalParameterValue).append(", ");
         }
      }

      if (this.namedParameters != null) {
         buffer.append("; named parameters: ").append(this.namedParameters);
      }

      if (this.filterKeys != null) {
         buffer.append("; filterKeys: ").append(this.filterKeys);
      }

      if (this.firstRow != null) {
         buffer.append("; first row: ").append(this.firstRow);
      }

      if (this.maxRows != null) {
         buffer.append("; max rows: ").append(this.maxRows);
      }

      if (this.customTransformer != null) {
         buffer.append("; transformer: ").append(this.customTransformer);
      }

      return buffer.toString();
   }
}
