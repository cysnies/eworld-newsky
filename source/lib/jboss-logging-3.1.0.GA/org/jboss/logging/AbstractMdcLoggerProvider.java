package org.jboss.logging;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractMdcLoggerProvider extends AbstractLoggerProvider {
   private final ThreadLocal mdcMap = new ThreadLocal();

   public Object getMdc(String key) {
      return this.mdcMap.get() == null ? null : ((Map)this.mdcMap.get()).get(key);
   }

   public Map getMdcMap() {
      return (Map)this.mdcMap.get();
   }

   public Object putMdc(String key, Object value) {
      Map<String, Object> map = (Map)this.mdcMap.get();
      if (map == null) {
         map = new HashMap();
         this.mdcMap.set(map);
      }

      return map.put(key, value);
   }

   public void removeMdc(String key) {
      Map<String, Object> map = (Map)this.mdcMap.get();
      if (map != null) {
         map.remove(key);
      }
   }
}
