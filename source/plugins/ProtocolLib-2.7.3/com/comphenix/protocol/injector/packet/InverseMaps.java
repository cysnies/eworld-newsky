package com.comphenix.protocol.injector.packet;

import com.comphenix.protocol.reflect.FieldUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.lang.reflect.Field;
import java.util.Map;

public class InverseMaps {
   private InverseMaps() {
   }

   public static Multimap inverseMultimap(final Map map, final Predicate filter) {
      final MapContainer container = new MapContainer(map);
      return new ForwardingMultimap() {
         private Multimap inverseMultimap;

         protected Multimap delegate() {
            if (container.hasChanged()) {
               this.inverseMultimap = HashMultimap.create();

               for(Map.Entry entry : map.entrySet()) {
                  if (filter.apply(entry)) {
                     this.inverseMultimap.put(entry.getValue(), entry.getKey());
                  }
               }

               container.setChanged(false);
            }

            return this.inverseMultimap;
         }
      };
   }

   public static Map inverseMap(final Map map, final Predicate filter) {
      final MapContainer container = new MapContainer(map);
      return new ForwardingMap() {
         private Map inverseMap;

         protected Map delegate() {
            if (container.hasChanged()) {
               this.inverseMap = Maps.newHashMap();

               for(Map.Entry entry : map.entrySet()) {
                  if (filter.apply(entry)) {
                     this.inverseMap.put(entry.getValue(), entry.getKey());
                  }
               }

               container.setChanged(false);
            }

            return this.inverseMap;
         }
      };
   }

   private static class MapContainer {
      private Field modCountField;
      private int lastModCount;
      private Object source;
      private boolean changed;

      public MapContainer(Object source) {
         this.source = source;
         this.changed = true;
         this.modCountField = FieldUtils.getField(source.getClass(), "modCount", true);
      }

      public boolean hasChanged() {
         this.checkChanged();
         return this.changed;
      }

      public void setChanged(boolean changed) {
         this.changed = changed;
      }

      protected void checkChanged() {
         if (!this.changed && this.getModificationCount() != this.lastModCount) {
            this.lastModCount = this.getModificationCount();
            this.changed = true;
         }

      }

      private int getModificationCount() {
         try {
            return this.modCountField != null ? this.modCountField.getInt(this.source) : this.lastModCount + 1;
         } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve modCount.", e);
         }
      }
   }
}
