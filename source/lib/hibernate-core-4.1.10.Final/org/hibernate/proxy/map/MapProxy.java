package org.hibernate.proxy.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public class MapProxy implements HibernateProxy, Map, Serializable {
   private MapLazyInitializer li;

   MapProxy(MapLazyInitializer li) {
      this.li = li;
   }

   public Object writeReplace() {
      return this;
   }

   public LazyInitializer getHibernateLazyInitializer() {
      return this.li;
   }

   public int size() {
      return this.li.getMap().size();
   }

   public void clear() {
      this.li.getMap().clear();
   }

   public boolean isEmpty() {
      return this.li.getMap().isEmpty();
   }

   public boolean containsKey(Object key) {
      return this.li.getMap().containsKey(key);
   }

   public boolean containsValue(Object value) {
      return this.li.getMap().containsValue(value);
   }

   public Collection values() {
      return this.li.getMap().values();
   }

   public void putAll(Map t) {
      this.li.getMap().putAll(t);
   }

   public Set entrySet() {
      return this.li.getMap().entrySet();
   }

   public Set keySet() {
      return this.li.getMap().keySet();
   }

   public Object get(Object key) {
      return this.li.getMap().get(key);
   }

   public Object remove(Object key) {
      return this.li.getMap().remove(key);
   }

   public Object put(Object key, Object value) {
      return this.li.getMap().put(key, value);
   }
}
