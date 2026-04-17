package org.hibernate.engine.spi;

import java.util.Map;
import java.util.Set;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.PropertyMapping;

public class SubselectFetch {
   private final Set resultingEntityKeys;
   private final String queryString;
   private final String alias;
   private final Loadable loadable;
   private final QueryParameters queryParameters;
   private final Map namedParameterLocMap;

   public SubselectFetch(String alias, Loadable loadable, QueryParameters queryParameters, Set resultingEntityKeys, Map namedParameterLocMap) {
      this.resultingEntityKeys = resultingEntityKeys;
      this.queryParameters = queryParameters;
      this.namedParameterLocMap = namedParameterLocMap;
      this.loadable = loadable;
      this.alias = alias;
      String queryString = queryParameters.getFilteredSQL();
      int fromIndex = queryString.indexOf(" from ");
      int orderByIndex = queryString.lastIndexOf("order by");
      this.queryString = orderByIndex > 0 ? queryString.substring(fromIndex, orderByIndex) : queryString.substring(fromIndex);
   }

   public QueryParameters getQueryParameters() {
      return this.queryParameters;
   }

   public Set getResult() {
      return this.resultingEntityKeys;
   }

   public String toSubselectString(String ukname) {
      String[] joinColumns = ukname == null ? StringHelper.qualify(this.alias, this.loadable.getIdentifierColumnNames()) : ((PropertyMapping)this.loadable).toColumns(this.alias, ukname);
      return "select " + StringHelper.join(", ", joinColumns) + this.queryString;
   }

   public String toString() {
      return "SubselectFetch(" + this.queryString + ')';
   }

   public Map getNamedParameterLocMap() {
      return this.namedParameterLocMap;
   }
}
