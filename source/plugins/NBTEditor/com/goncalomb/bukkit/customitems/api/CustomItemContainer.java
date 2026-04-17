package com.goncalomb.bukkit.customitems.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

class CustomItemContainer {
   private HashMap _customItems = new HashMap();

   public static void removeFromHashMap(HashMap map, Plugin plugin) {
      Iterator<Map.Entry<String, T>> it = map.entrySet().iterator();

      while(it.hasNext()) {
         if (((CustomItem)((Map.Entry)it.next()).getValue())._owner == plugin) {
            it.remove();
         }
      }

   }

   public boolean put(CustomItem customItem) {
      HashMap<String, T> itemMap = (HashMap)this._customItems.get(customItem.getMaterial());
      if (itemMap == null) {
         itemMap = new HashMap();
         this._customItems.put(customItem.getMaterial(), itemMap);
      }

      itemMap.put(customItem.getName(), customItem);
      return true;
   }

   public final CustomItem get(ItemStack item) {
      String name = CustomItem.getItemName(item);
      if (name != null) {
         MaterialData data = item.getData();
         if (data.getItemType().getMaxDurability() > 0) {
            data.setData((byte)0);
         }

         HashMap<String, T> itemMap = (HashMap)this._customItems.get(data);
         if (itemMap != null) {
            T customItem = (T)((CustomItem)itemMap.get(name));
            if (customItem != null) {
               return customItem;
            }
         }
      }

      return null;
   }

   public final int size() {
      return this._customItems.size();
   }

   public final void remove(Plugin plugin) {
      for(HashMap map : this._customItems.values()) {
         removeFromHashMap(map, plugin);
      }

   }

   public final void clear() {
      this._customItems.clear();
   }
}
