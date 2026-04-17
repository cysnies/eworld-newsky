package org.hibernate.metamodel.source;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.metamodel.binding.MetaAttribute;

public class MetaAttributeContext {
   private final MetaAttributeContext parentContext;
   private final ConcurrentHashMap metaAttributeMap;

   public MetaAttributeContext() {
      this((MetaAttributeContext)null);
   }

   public MetaAttributeContext(MetaAttributeContext parentContext) {
      this.metaAttributeMap = new ConcurrentHashMap();
      this.parentContext = parentContext;
   }

   public Iterable getKeys() {
      HashSet<String> keys = new HashSet();
      this.addKeys(keys);
      return keys;
   }

   private void addKeys(Set keys) {
      keys.addAll(this.metaAttributeMap.keySet());
      if (this.parentContext != null) {
         this.parentContext.addKeys(keys);
      }

   }

   public Iterable getLocalKeys() {
      return this.metaAttributeMap.keySet();
   }

   public MetaAttribute getMetaAttribute(String key) {
      MetaAttribute value = this.getLocalMetaAttribute(key);
      if (value == null) {
         value = this.parentContext.getMetaAttribute(key);
      }

      return value;
   }

   public MetaAttribute getLocalMetaAttribute(String key) {
      return (MetaAttribute)this.metaAttributeMap.get(key);
   }

   public void add(MetaAttribute metaAttribute) {
      this.metaAttributeMap.put(metaAttribute.getName(), metaAttribute);
   }
}
