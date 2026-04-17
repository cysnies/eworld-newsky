package com.comphenix.protocol.reflect;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class IntEnum {
   protected BiMap members = HashBiMap.create();

   public IntEnum() {
      this.registerAll();
   }

   protected void registerAll() {
      try {
         for(Field entry : this.getClass().getFields()) {
            if (entry.getType().equals(Integer.TYPE)) {
               this.registerMember(entry.getInt(this), entry.getName());
            }
         }
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }

   }

   protected void registerMember(int id, String name) {
      this.members.put(id, name);
   }

   public boolean hasMember(int id) {
      return this.members.containsKey(id);
   }

   public Integer valueOf(String name) {
      return (Integer)this.members.inverse().get(name);
   }

   public String getDeclaredName(Integer id) {
      return (String)this.members.get(id);
   }

   public Set values() {
      return new HashSet(this.members.keySet());
   }
}
