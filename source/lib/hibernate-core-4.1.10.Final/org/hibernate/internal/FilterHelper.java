package org.hibernate.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.sql.Template;

public class FilterHelper {
   private final String[] filterNames;
   private final String[] filterConditions;
   private final boolean[] filterAutoAliasFlags;
   private final Map[] filterAliasTableMaps;

   public FilterHelper(List filters, SessionFactoryImplementor factory) {
      int filterCount = filters.size();
      this.filterNames = new String[filterCount];
      this.filterConditions = new String[filterCount];
      this.filterAutoAliasFlags = new boolean[filterCount];
      this.filterAliasTableMaps = new Map[filterCount];
      Iterator iter = filters.iterator();

      for(int var6 = 0; iter.hasNext(); ++var6) {
         this.filterAutoAliasFlags[var6] = false;
         FilterConfiguration filter = (FilterConfiguration)iter.next();
         this.filterNames[var6] = filter.getName();
         this.filterConditions[var6] = filter.getCondition();
         this.filterAliasTableMaps[var6] = filter.getAliasTableMap(factory);
         if ((this.filterAliasTableMaps[var6].isEmpty() || isTableFromPersistentClass(this.filterAliasTableMaps[var6])) && filter.useAutoAliasInjection()) {
            this.filterConditions[var6] = Template.renderWhereStringTemplate(filter.getCondition(), "$FILTER_PLACEHOLDER$", factory.getDialect(), factory.getSqlFunctionRegistry());
            this.filterAutoAliasFlags[var6] = true;
         }

         this.filterConditions[var6] = StringHelper.replace(this.filterConditions[var6], ":", ":" + this.filterNames[var6] + ".");
      }

   }

   private static boolean isTableFromPersistentClass(Map aliasTableMap) {
      return aliasTableMap.size() == 1 && aliasTableMap.containsKey((Object)null);
   }

   public boolean isAffectedBy(Map enabledFilters) {
      int i = 0;

      for(int max = this.filterNames.length; i < max; ++i) {
         if (enabledFilters.containsKey(this.filterNames[i])) {
            return true;
         }
      }

      return false;
   }

   public String render(FilterAliasGenerator aliasGenerator, Map enabledFilters) {
      StringBuilder buffer = new StringBuilder();
      this.render(buffer, aliasGenerator, enabledFilters);
      return buffer.toString();
   }

   public void render(StringBuilder buffer, FilterAliasGenerator aliasGenerator, Map enabledFilters) {
      if (this.filterNames != null && this.filterNames.length > 0) {
         int i = 0;

         for(int max = this.filterNames.length; i < max; ++i) {
            if (enabledFilters.containsKey(this.filterNames[i])) {
               String condition = this.filterConditions[i];
               if (StringHelper.isNotEmpty(condition)) {
                  buffer.append(" and ").append(this.render(aliasGenerator, i));
               }
            }
         }
      }

   }

   private String render(FilterAliasGenerator aliasGenerator, int filterIndex) {
      Map<String, String> aliasTableMap = this.filterAliasTableMaps[filterIndex];
      String condition = this.filterConditions[filterIndex];
      if (this.filterAutoAliasFlags[filterIndex]) {
         return StringHelper.replace(condition, "$FILTER_PLACEHOLDER$", aliasGenerator.getAlias((String)aliasTableMap.get((Object)null)));
      } else if (isTableFromPersistentClass(aliasTableMap)) {
         return condition.replace("{alias}", aliasGenerator.getAlias((String)aliasTableMap.get((Object)null)));
      } else {
         for(Map.Entry entry : aliasTableMap.entrySet()) {
            condition = condition.replace("{" + (String)entry.getKey() + "}", aliasGenerator.getAlias((String)entry.getValue()));
         }

         return condition;
      }
   }
}
