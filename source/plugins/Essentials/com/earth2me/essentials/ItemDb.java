package com.earth2me.essentials;

import com.earth2me.essentials.api.IItemDb;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemDb implements IConf, IItemDb {
   private final transient IEssentials ess;
   private final transient Map items = new HashMap();
   private final transient Map names = new HashMap();
   private final transient Map durabilities = new HashMap();
   private final transient ManagedFile file;
   private final transient Pattern splitPattern = Pattern.compile("[:+',;.]");

   public ItemDb(IEssentials ess) {
      this.ess = ess;
      this.file = new ManagedFile("items.csv", ess);
   }

   public void reloadConfig() {
      List<String> lines = this.file.getLines();
      if (!lines.isEmpty()) {
         this.durabilities.clear();
         this.items.clear();
         this.names.clear();

         for(String line : lines) {
            line = line.trim().toLowerCase(Locale.ENGLISH);
            if (line.length() <= 0 || line.charAt(0) != '#') {
               String[] parts = line.split("[^a-z0-9]");
               if (parts.length >= 2) {
                  int numeric = Integer.parseInt(parts[1]);
                  short data = parts.length > 2 && !parts[2].equals("0") ? Short.parseShort(parts[2]) : 0;
                  String itemName = parts[0].toLowerCase(Locale.ENGLISH);
                  this.durabilities.put(itemName, data);
                  this.items.put(itemName, numeric);
                  ItemData itemData = new ItemData(numeric, data);
                  if (this.names.containsKey(itemData)) {
                     List<String> nameList = (List)this.names.get(itemData);
                     nameList.add(itemName);
                     Collections.sort(nameList, new LengthCompare());
                  } else {
                     List<String> nameList = new ArrayList();
                     nameList.add(itemName);
                     this.names.put(itemData, nameList);
                  }
               }
            }
         }

      }
   }

   public ItemStack get(String id, int quantity) throws Exception {
      ItemStack retval = this.get(id.toLowerCase(Locale.ENGLISH));
      retval.setAmount(quantity);
      return retval;
   }

   public ItemStack get(String id) throws Exception {
      int itemid = 0;
      String itemname = null;
      short metaData = 0;
      String[] parts = this.splitPattern.split(id);
      if (id.matches("^\\d+[:+',;.]\\d+$")) {
         itemid = Integer.parseInt(parts[0]);
         metaData = Short.parseShort(parts[1]);
      } else if (Util.isInt(id)) {
         itemid = Integer.parseInt(id);
      } else if (id.matches("^[^:+',;.]+[:+',;.]\\d+$")) {
         itemname = parts[0].toLowerCase(Locale.ENGLISH);
         metaData = Short.parseShort(parts[1]);
      } else {
         itemname = id.toLowerCase(Locale.ENGLISH);
      }

      if (itemname != null) {
         if (this.items.containsKey(itemname)) {
            itemid = (Integer)this.items.get(itemname);
            if (this.durabilities.containsKey(itemname) && metaData == 0) {
               metaData = (Short)this.durabilities.get(itemname);
            }
         } else {
            if (Material.getMaterial(itemname.toUpperCase(Locale.ENGLISH)) == null) {
               throw new Exception(I18n._("unknownItemName", id));
            }

            itemid = Material.getMaterial(itemname.toUpperCase(Locale.ENGLISH)).getId();
            metaData = 0;
         }
      }

      Material mat = Material.getMaterial(itemid);
      if (mat == null) {
         throw new Exception(I18n._("unknownItemId", itemid));
      } else {
         ItemStack retval = new ItemStack(mat);
         retval.setAmount(mat.getMaxStackSize());
         retval.setDurability(metaData);
         return retval;
      }
   }

   public String names(ItemStack item) {
      ItemData itemData = new ItemData(item.getTypeId(), item.getDurability());
      List<String> nameList = (List)this.names.get(itemData);
      if (nameList == null) {
         itemData = new ItemData(item.getTypeId(), (short)0);
         nameList = (List)this.names.get(itemData);
         if (nameList == null) {
            return null;
         }
      }

      if (nameList.size() > 15) {
         nameList = nameList.subList(0, 14);
      }

      return Util.joinList(", ", nameList);
   }

   class ItemData {
      private final int itemNo;
      private final short itemData;

      ItemData(int itemNo, short itemData) {
         this.itemNo = itemNo;
         this.itemData = itemData;
      }

      public int getItemNo() {
         return this.itemNo;
      }

      public short getItemData() {
         return this.itemData;
      }

      public int hashCode() {
         return 31 * this.itemNo ^ this.itemData;
      }

      public boolean equals(Object o) {
         if (o == null) {
            return false;
         } else if (!(o instanceof ItemData)) {
            return false;
         } else {
            ItemData pairo = (ItemData)o;
            return this.itemNo == pairo.getItemNo() && this.itemData == pairo.getItemData();
         }
      }
   }

   class LengthCompare implements Comparator {
      public LengthCompare() {
      }

      public int compare(String s1, String s2) {
         return s1.length() - s2.length();
      }
   }
}
