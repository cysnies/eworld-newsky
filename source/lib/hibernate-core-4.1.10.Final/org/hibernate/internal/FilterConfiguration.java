package org.hibernate.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.entity.Joinable;

public class FilterConfiguration {
   private final String name;
   private final String condition;
   private final boolean autoAliasInjection;
   private final Map aliasTableMap;
   private final Map aliasEntityMap;
   private final PersistentClass persistentClass;

   public FilterConfiguration(String name, String condition, boolean autoAliasInjection, Map aliasTableMap, Map aliasEntityMap, PersistentClass persistentClass) {
      this.name = name;
      this.condition = condition;
      this.autoAliasInjection = autoAliasInjection;
      this.aliasTableMap = aliasTableMap;
      this.aliasEntityMap = aliasEntityMap;
      this.persistentClass = persistentClass;
   }

   public String getName() {
      return this.name;
   }

   public String getCondition() {
      return this.condition;
   }

   public boolean useAutoAliasInjection() {
      return this.autoAliasInjection;
   }

   public Map getAliasTableMap(SessionFactoryImplementor factory) {
      Map<String, String> mergedAliasTableMap = this.mergeAliasMaps(factory);
      if (!mergedAliasTableMap.isEmpty()) {
         return mergedAliasTableMap;
      } else if (this.persistentClass != null) {
         String table = this.persistentClass.getTable().getQualifiedName(factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
         return Collections.singletonMap((Object)null, table);
      } else {
         return Collections.emptyMap();
      }
   }

   private Map mergeAliasMaps(SessionFactoryImplementor factory) {
      Map<String, String> ret = new HashMap();
      if (this.aliasTableMap != null) {
         ret.putAll(this.aliasTableMap);
      }

      if (this.aliasEntityMap != null) {
         for(Map.Entry entry : this.aliasEntityMap.entrySet()) {
            ret.put(entry.getKey(), ((Joinable)Joinable.class.cast(factory.getEntityPersister((String)entry.getValue()))).getTableName());
         }
      }

      return ret;
   }
}
