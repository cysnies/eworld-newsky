package org.hibernate.engine.spi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.type.Type;

public class FilterDefinition implements Serializable {
   private final String filterName;
   private final String defaultFilterCondition;
   private final Map parameterTypes = new HashMap();

   public FilterDefinition(String name, String defaultCondition, Map parameterTypes) {
      this.filterName = name;
      this.defaultFilterCondition = defaultCondition;
      this.parameterTypes.putAll(parameterTypes);
   }

   public String getFilterName() {
      return this.filterName;
   }

   public Set getParameterNames() {
      return this.parameterTypes.keySet();
   }

   public Type getParameterType(String parameterName) {
      return (Type)this.parameterTypes.get(parameterName);
   }

   public String getDefaultFilterCondition() {
      return this.defaultFilterCondition;
   }

   public Map getParameterTypes() {
      return this.parameterTypes;
   }
}
