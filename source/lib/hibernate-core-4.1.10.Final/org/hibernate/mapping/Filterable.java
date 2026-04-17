package org.hibernate.mapping;

public interface Filterable {
   void addFilter(String var1, String var2, boolean var3, java.util.Map var4, java.util.Map var5);

   java.util.List getFilters();
}
