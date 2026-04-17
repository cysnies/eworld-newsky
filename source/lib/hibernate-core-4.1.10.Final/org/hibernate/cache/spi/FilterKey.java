package org.hibernate.cache.spi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hibernate.Filter;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.FilterImpl;
import org.hibernate.type.Type;

public final class FilterKey implements Serializable {
   private String filterName;
   private Map filterParameters = new HashMap();

   public FilterKey(String name, Map params, Map types) {
      this.filterName = name;

      for(Map.Entry paramEntry : params.entrySet()) {
         Type type = (Type)types.get(paramEntry.getKey());
         this.filterParameters.put(paramEntry.getKey(), new TypedValue(type, paramEntry.getValue()));
      }

   }

   public int hashCode() {
      int result = 13;
      result = 37 * result + this.filterName.hashCode();
      result = 37 * result + this.filterParameters.hashCode();
      return result;
   }

   public boolean equals(Object other) {
      if (!(other instanceof FilterKey)) {
         return false;
      } else {
         FilterKey that = (FilterKey)other;
         if (!that.filterName.equals(this.filterName)) {
            return false;
         } else {
            return that.filterParameters.equals(this.filterParameters);
         }
      }
   }

   public String toString() {
      return "FilterKey[" + this.filterName + this.filterParameters + ']';
   }

   public static Set createFilterKeys(Map enabledFilters) {
      if (enabledFilters.size() == 0) {
         return null;
      } else {
         Set<FilterKey> result = new HashSet();

         for(Filter filter : enabledFilters.values()) {
            FilterKey key = new FilterKey(filter.getName(), ((FilterImpl)filter).getParameters(), filter.getFilterDefinition().getParameterTypes());
            result.add(key);
         }

         return result;
      }
   }
}
