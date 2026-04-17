package org.hibernate.dialect;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.hibernate.MappingException;
import org.hibernate.internal.util.StringHelper;

public class TypeNames {
   private Map weighted = new HashMap();
   private Map defaults = new HashMap();

   public String get(int typecode) throws MappingException {
      String result = (String)this.defaults.get(typecode);
      if (result == null) {
         throw new MappingException("No Dialect mapping for JDBC type: " + typecode);
      } else {
         return result;
      }
   }

   public String get(int typeCode, long size, int precision, int scale) throws MappingException {
      Map<Long, String> map = (Map)this.weighted.get(typeCode);
      if (map != null && map.size() > 0) {
         for(Map.Entry entry : map.entrySet()) {
            if (size <= (Long)entry.getKey()) {
               return replace((String)entry.getValue(), size, precision, scale);
            }
         }
      }

      return replace(this.get(typeCode), size, precision, scale);
   }

   private static String replace(String type, long size, int precision, int scale) {
      type = StringHelper.replaceOnce(type, "$s", Integer.toString(scale));
      type = StringHelper.replaceOnce(type, "$l", Long.toString(size));
      return StringHelper.replaceOnce(type, "$p", Integer.toString(precision));
   }

   public void put(int typecode, long capacity, String value) {
      Map<Long, String> map = (Map)this.weighted.get(typecode);
      if (map == null) {
         map = new TreeMap();
         this.weighted.put(typecode, map);
      }

      map.put(capacity, value);
   }

   public void put(int typecode, String value) {
      this.defaults.put(typecode, value);
   }
}
