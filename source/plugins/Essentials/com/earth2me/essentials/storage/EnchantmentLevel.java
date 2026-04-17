package com.earth2me.essentials.storage;

import java.util.Map;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentLevel implements Map.Entry {
   private Enchantment enchantment;
   private int level;

   public EnchantmentLevel(Enchantment enchantment, int level) {
      this.enchantment = enchantment;
      this.level = level;
   }

   public Enchantment getEnchantment() {
      return this.enchantment;
   }

   public void setEnchantment(Enchantment enchantment) {
      this.enchantment = enchantment;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public Enchantment getKey() {
      return this.enchantment;
   }

   public Integer getValue() {
      return this.level;
   }

   public Integer setValue(Integer v) {
      int t = this.level;
      this.level = v;
      return t;
   }

   public int hashCode() {
      return this.enchantment.hashCode() ^ this.level;
   }

   public boolean equals(Object obj) {
      if (obj instanceof Map.Entry) {
         Map.Entry entry = (Map.Entry)obj;
         if (entry.getKey() instanceof Enchantment && entry.getValue() instanceof Integer) {
            Enchantment enchantment = (Enchantment)entry.getKey();
            Integer level = (Integer)entry.getValue();
            return this.enchantment.equals(enchantment) && this.level == level;
         }
      }

      return false;
   }
}
