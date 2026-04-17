package com.earth2me.essentials;

import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

public class Worth implements IConf {
   private static final Logger logger = Logger.getLogger("Minecraft");
   private final EssentialsConf config;

   public Worth(File dataFolder) {
      this.config = new EssentialsConf(new File(dataFolder, "worth.yml"));
      this.config.setTemplateName("/worth.yml");
      this.config.load();
   }

   public double getPrice(ItemStack itemStack) {
      String itemname = itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
      double result = this.config.getDouble("worth." + itemname + "." + itemStack.getDurability(), Double.NaN);
      if (Double.isNaN(result)) {
         result = this.config.getDouble("worth." + itemname + ".0", Double.NaN);
      }

      if (Double.isNaN(result)) {
         result = this.config.getDouble("worth." + itemname, Double.NaN);
      }

      if (Double.isNaN(result)) {
         result = this.config.getDouble("worth-" + itemStack.getTypeId(), Double.NaN);
      }

      return result;
   }

   public void setPrice(ItemStack itemStack, double price) {
      if (itemStack.getType().getData() == null) {
         this.config.setProperty("worth." + itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", ""), (Object)price);
      } else {
         this.config.setProperty("worth." + itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "") + "." + itemStack.getDurability(), (Object)price);
      }

      this.config.removeProperty("worth-" + itemStack.getTypeId());
      this.config.save();
   }

   public void reloadConfig() {
      this.config.load();
   }
}
