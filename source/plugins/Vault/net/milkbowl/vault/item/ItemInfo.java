package net.milkbowl.vault.item;

import [[Ljava.lang.String;;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemInfo {
   public final Material material;
   public final short subTypeId;
   public final String name;
   public final String[][] search;

   public ItemInfo(String name, String[][] search, Material material) {
      this.material = material;
      this.name = name;
      this.subTypeId = 0;
      this.search = (String[][])((String;)search).clone();
   }

   public ItemInfo(String name, String[][] search, Material material, short subTypeId) {
      this.name = name;
      this.material = material;
      this.subTypeId = subTypeId;
      this.search = (String[][])((String;)search).clone();
   }

   public Material getType() {
      return this.material;
   }

   public short getSubTypeId() {
      return this.subTypeId;
   }

   public int getStackSize() {
      return this.material.getMaxStackSize();
   }

   public int getId() {
      return this.material.getId();
   }

   public boolean isEdible() {
      return this.material.isEdible();
   }

   public boolean isBlock() {
      return this.material.isBlock();
   }

   public String getName() {
      return this.name;
   }

   public int hashCode() {
      int hash = 7;
      hash = 17 * hash + this.getId();
      hash = 17 * hash + this.subTypeId;
      return hash;
   }

   public boolean isDurable() {
      return this.material.getMaxDurability() > 0;
   }

   public ItemStack toStack() {
      return new ItemStack(this.material, 1, this.subTypeId);
   }

   public String toString() {
      return String.format("%s[%d:%d]", this.name, this.material.getId(), this.subTypeId);
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (this == obj) {
         return true;
      } else if (!(obj instanceof ItemInfo)) {
         return false;
      } else {
         return ((ItemInfo)obj).material == this.material && ((ItemInfo)obj).subTypeId == this.subTypeId;
      }
   }
}
