package org.hibernate.tuple;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;

public class DynamicMapInstantiator implements Instantiator {
   public static final String KEY = "$type$";
   private String entityName;
   private Set isInstanceEntityNames = new HashSet();

   public DynamicMapInstantiator() {
      this.entityName = null;
   }

   public DynamicMapInstantiator(PersistentClass mappingInfo) {
      this.entityName = mappingInfo.getEntityName();
      this.isInstanceEntityNames.add(this.entityName);
      if (mappingInfo.hasSubclasses()) {
         Iterator itr = mappingInfo.getSubclassClosureIterator();

         while(itr.hasNext()) {
            PersistentClass subclassInfo = (PersistentClass)itr.next();
            this.isInstanceEntityNames.add(subclassInfo.getEntityName());
         }
      }

   }

   public DynamicMapInstantiator(EntityBinding mappingInfo) {
      this.entityName = mappingInfo.getEntity().getName();
      this.isInstanceEntityNames.add(this.entityName);

      for(EntityBinding subEntityBinding : mappingInfo.getPostOrderSubEntityBindingClosure()) {
         this.isInstanceEntityNames.add(subEntityBinding.getEntity().getName());
      }

   }

   public final Object instantiate(Serializable id) {
      return this.instantiate();
   }

   public final Object instantiate() {
      Map map = this.generateMap();
      if (this.entityName != null) {
         map.put("$type$", this.entityName);
      }

      return map;
   }

   public final boolean isInstance(Object object) {
      if (object instanceof Map) {
         if (this.entityName == null) {
            return true;
         } else {
            String type = (String)((Map)object).get("$type$");
            return type == null || this.isInstanceEntityNames.contains(type);
         }
      } else {
         return false;
      }
   }

   protected Map generateMap() {
      return new HashMap();
   }
}
