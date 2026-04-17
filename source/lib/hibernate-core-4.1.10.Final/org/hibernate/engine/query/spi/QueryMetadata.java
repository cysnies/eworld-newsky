package org.hibernate.engine.query.spi;

import java.io.Serializable;
import java.util.Set;
import org.hibernate.type.Type;

public class QueryMetadata implements Serializable {
   private final String sourceQuery;
   private final ParameterMetadata parameterMetadata;
   private final String[] returnAliases;
   private final Type[] returnTypes;
   private final Set querySpaces;

   public QueryMetadata(String sourceQuery, ParameterMetadata parameterMetadata, String[] returnAliases, Type[] returnTypes, Set querySpaces) {
      this.sourceQuery = sourceQuery;
      this.parameterMetadata = parameterMetadata;
      this.returnAliases = returnAliases;
      this.returnTypes = returnTypes;
      this.querySpaces = querySpaces;
   }

   public String getSourceQuery() {
      return this.sourceQuery;
   }

   public ParameterMetadata getParameterMetadata() {
      return this.parameterMetadata;
   }

   public String[] getReturnAliases() {
      return this.returnAliases;
   }

   public Type[] getReturnTypes() {
      return this.returnTypes;
   }

   public Set getQuerySpaces() {
      return this.querySpaces;
   }
}
