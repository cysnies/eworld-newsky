package com.sk89q.worldedit.blocks;

import java.util.HashMap;
import java.util.Map;

public class BaseItem {
   private int id;
   private short data;
   private final Map enchantments = new HashMap();

   public BaseItem(int id) {
      this.id = id;
      this.data = 0;
   }

   public BaseItem(int id, short data) {
      this.id = id;
      this.data = data;
   }

   public int getType() {
      return this.id;
   }

   public void setType(int id) {
      this.id = id;
   }

   /** @deprecated */
   @Deprecated
   public short getDamage() {
      return this.data;
   }

   public short getData() {
      return this.data;
   }

   /** @deprecated */
   @Deprecated
   public void setDamage(short data) {
      this.data = data;
   }

   public void setData(short data) {
      this.data = data;
   }

   public Map getEnchantments() {
      return this.enchantments;
   }
}
