package com.earth2me.essentials;

import com.earth2me.essentials.textreader.BookInput;
import com.earth2me.essentials.textreader.BookPager;
import com.earth2me.essentials.textreader.IText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MetaItemStack {
   private final transient Pattern splitPattern = Pattern.compile("[:+',;.]");
   private final ItemStack stack;
   private static final Map colorMap = new HashMap();
   private static final Map fireworkShape = new HashMap();
   private FireworkEffect.Builder builder = FireworkEffect.builder();
   private PotionEffectType pEffectType;
   private PotionEffect pEffect;
   private boolean validFirework = false;
   private boolean validPotionEffect = false;
   private boolean validPotionDuration = false;
   private boolean validPotionPower = false;
   private boolean completePotion = false;
   private int power = 1;
   private int duration = 120;

   public MetaItemStack(ItemStack stack) {
      this.stack = stack.clone();
   }

   public ItemStack getItemStack() {
      return this.stack;
   }

   public boolean isValidFirework() {
      return this.validFirework;
   }

   public boolean isValidPotion() {
      return this.validPotionEffect && this.validPotionDuration && this.validPotionPower;
   }

   public FireworkEffect.Builder getFireworkBuilder() {
      return this.builder;
   }

   public PotionEffect getPotionEffect() {
      return this.pEffect;
   }

   public boolean completePotion() {
      return this.completePotion;
   }

   private void resetPotionMeta() {
      this.pEffect = null;
      this.pEffectType = null;
      this.validPotionEffect = false;
      this.validPotionDuration = false;
      this.validPotionPower = false;
      this.completePotion = true;
   }

   public void parseStringMeta(CommandSender sender, boolean allowUnsafe, String[] string, int fromArg, IEssentials ess) throws Exception {
      for(int i = fromArg; i < string.length; ++i) {
         this.addStringMeta(sender, allowUnsafe, string[i], ess);
      }

      if (this.validFirework) {
         if (!this.hasMetaPermission(sender, "firework", true, true, ess)) {
            throw new Exception(I18n._("noMetaFirework"));
         }

         FireworkEffect effect = this.builder.build();
         FireworkMeta fmeta = (FireworkMeta)this.stack.getItemMeta();
         fmeta.addEffect(effect);
         if (fmeta.getEffects().size() > 1 && !this.hasMetaPermission(sender, "firework-multiple", true, true, ess)) {
            throw new Exception(I18n._("multipleCharges"));
         }

         this.stack.setItemMeta(fmeta);
      }

   }

   private void addStringMeta(CommandSender sender, boolean allowUnsafe, String string, IEssentials ess) throws Exception {
      String[] split = this.splitPattern.split(string, 2);
      if (split.length >= 1) {
         if (split.length > 1 && split[0].equalsIgnoreCase("name") && this.hasMetaPermission(sender, "name", false, true, ess)) {
            String displayName = Util.replaceFormat(split[1].replace('_', ' '));
            ItemMeta meta = this.stack.getItemMeta();
            meta.setDisplayName(displayName);
            this.stack.setItemMeta(meta);
         } else if (split.length > 1 && (split[0].equalsIgnoreCase("lore") || split[0].equalsIgnoreCase("desc")) && this.hasMetaPermission(sender, "lore", false, true, ess)) {
            List<String> lore = new ArrayList();

            for(String line : split[1].split("\\|")) {
               lore.add(Util.replaceFormat(line.replace('_', ' ')));
            }

            ItemMeta meta = this.stack.getItemMeta();
            meta.setLore(lore);
            this.stack.setItemMeta(meta);
         } else if (split.length > 1 && (split[0].equalsIgnoreCase("player") || split[0].equalsIgnoreCase("owner")) && this.stack.getType() == Material.SKULL_ITEM && this.hasMetaPermission(sender, "head", false, true, ess)) {
            if (this.stack.getDurability() != 3) {
               throw new Exception(I18n._("onlyPlayerSkulls"));
            }

            String owner = split[1];
            SkullMeta meta = (SkullMeta)this.stack.getItemMeta();
            meta.setOwner(owner);
            this.stack.setItemMeta(meta);
         } else if (split.length > 1 && split[0].equalsIgnoreCase("book") && this.stack.getType() == Material.WRITTEN_BOOK && this.hasMetaPermission(sender, "book", false, true, ess)) {
            BookMeta meta = (BookMeta)this.stack.getItemMeta();
            IText input = new BookInput("book", true, ess);
            BookPager pager = new BookPager(input);
            if (!this.hasMetaPermission(sender, "chapter", true, true, ess) && !this.hasMetaPermission(sender, "chapter-" + split[1].toLowerCase(Locale.ENGLISH), true, true, ess)) {
               throw new Exception(I18n._("noChapterMeta"));
            }

            List<String> pages = pager.getPages(split[1]);
            meta.setPages(pages);
            this.stack.setItemMeta(meta);
         } else if (split.length > 1 && split[0].equalsIgnoreCase("author") && this.stack.getType() == Material.WRITTEN_BOOK && this.hasMetaPermission(sender, "author", false, true, ess)) {
            String author = split[1];
            BookMeta meta = (BookMeta)this.stack.getItemMeta();
            meta.setAuthor(author);
            this.stack.setItemMeta(meta);
         } else if (split.length > 1 && split[0].equalsIgnoreCase("title") && this.stack.getType() == Material.WRITTEN_BOOK && this.hasMetaPermission(sender, "title", false, true, ess)) {
            String title = Util.replaceFormat(split[1].replace('_', ' '));
            BookMeta meta = (BookMeta)this.stack.getItemMeta();
            meta.setTitle(title);
            this.stack.setItemMeta(meta);
         } else if (split.length > 1 && split[0].equalsIgnoreCase("power") && this.stack.getType() == Material.FIREWORK && this.hasMetaPermission(sender, "firework-power", false, true, ess)) {
            int power = Util.isInt(split[1]) ? Integer.parseInt(split[1]) : 0;
            FireworkMeta meta = (FireworkMeta)this.stack.getItemMeta();
            meta.setPower(power > 3 ? 4 : power);
            this.stack.setItemMeta(meta);
         } else if (this.stack.getType() == Material.FIREWORK) {
            this.addFireworkMeta(sender, false, string, ess);
         } else if (this.stack.getType() == Material.POTION) {
            this.addPotionMeta(sender, false, string, ess);
         } else if (split.length > 1 && (split[0].equalsIgnoreCase("color") || split[0].equalsIgnoreCase("colour")) && (this.stack.getType() == Material.LEATHER_BOOTS || this.stack.getType() == Material.LEATHER_CHESTPLATE || this.stack.getType() == Material.LEATHER_HELMET || this.stack.getType() == Material.LEATHER_LEGGINGS)) {
            String[] color = split[1].split("(\\||,)");
            if (color.length != 3) {
               throw new Exception(I18n._("leatherSyntax"));
            }

            int red = Util.isInt(color[0]) ? Integer.parseInt(color[0]) : 0;
            int green = Util.isInt(color[1]) ? Integer.parseInt(color[1]) : 0;
            int blue = Util.isInt(color[2]) ? Integer.parseInt(color[2]) : 0;
            LeatherArmorMeta meta = (LeatherArmorMeta)this.stack.getItemMeta();
            meta.setColor(Color.fromRGB(red, green, blue));
            this.stack.setItemMeta(meta);
         } else {
            this.parseEnchantmentStrings(sender, allowUnsafe, split, ess);
         }

      }
   }

   public void addFireworkMeta(CommandSender sender, boolean allowShortName, String string, IEssentials ess) throws Exception {
      if (this.stack.getType() == Material.FIREWORK) {
         String[] split = this.splitPattern.split(string, 2);
         if (split.length < 2) {
            return;
         }

         if (split[0].equalsIgnoreCase("color") || split[0].equalsIgnoreCase("colour") || allowShortName && split[0].equalsIgnoreCase("c")) {
            if (this.validFirework) {
               if (!this.hasMetaPermission(sender, "firework", true, true, ess)) {
                  throw new Exception(I18n._("noMetaFirework"));
               }

               FireworkEffect effect = this.builder.build();
               FireworkMeta fmeta = (FireworkMeta)this.stack.getItemMeta();
               fmeta.addEffect(effect);
               if (fmeta.getEffects().size() > 1 && !this.hasMetaPermission(sender, "firework-multiple", true, true, ess)) {
                  throw new Exception(I18n._("multipleCharges"));
               }

               this.stack.setItemMeta(fmeta);
               this.builder = FireworkEffect.builder();
            }

            List<Color> primaryColors = new ArrayList();
            String[] colors = split[1].split(",");

            for(String color : colors) {
               if (!colorMap.containsKey(color.toUpperCase())) {
                  throw new Exception(I18n._("invalidFireworkFormat", split[1], split[0]));
               }

               this.validFirework = true;
               primaryColors.add(((DyeColor)colorMap.get(color.toUpperCase())).getFireworkColor());
            }

            this.builder.withColor(primaryColors);
         } else if (!split[0].equalsIgnoreCase("shape") && !split[0].equalsIgnoreCase("type") && (!allowShortName || !split[0].equalsIgnoreCase("s") && !split[0].equalsIgnoreCase("t"))) {
            if (!split[0].equalsIgnoreCase("fade") && (!allowShortName || !split[0].equalsIgnoreCase("f"))) {
               if (split[0].equalsIgnoreCase("effect") || allowShortName && split[0].equalsIgnoreCase("e")) {
                  String[] effects = split[1].split(",");

                  for(String effect : effects) {
                     if (effect.equalsIgnoreCase("twinkle")) {
                        this.builder.flicker(true);
                     } else {
                        if (!effect.equalsIgnoreCase("trail")) {
                           throw new Exception(I18n._("invalidFireworkFormat", split[1], split[0]));
                        }

                        this.builder.trail(true);
                     }
                  }
               }
            } else {
               List<Color> fadeColors = new ArrayList();
               String[] colors = split[1].split(",");

               for(String color : colors) {
                  if (!colorMap.containsKey(color.toUpperCase())) {
                     throw new Exception(I18n._("invalidFireworkFormat", split[1], split[0]));
                  }

                  fadeColors.add(((DyeColor)colorMap.get(color.toUpperCase())).getFireworkColor());
               }

               if (!fadeColors.isEmpty()) {
                  this.builder.withFade(fadeColors);
               }
            }
         } else {
            FireworkEffect.Type finalEffect = null;
            split[1] = split[1].equalsIgnoreCase("large") ? "BALL_LARGE" : split[1];
            if (!fireworkShape.containsKey(split[1].toUpperCase())) {
               throw new Exception(I18n._("invalidFireworkFormat", split[1], split[0]));
            }

            finalEffect = (FireworkEffect.Type)fireworkShape.get(split[1].toUpperCase());
            if (finalEffect != null) {
               this.builder.with(finalEffect);
            }
         }
      }

   }

   public void addPotionMeta(CommandSender sender, boolean allowShortName, String string, IEssentials ess) throws Exception {
      if (this.stack.getType() == Material.POTION) {
         String[] split = this.splitPattern.split(string, 2);
         if (split.length < 2) {
            return;
         }

         if (!split[0].equalsIgnoreCase("effect") && (!allowShortName || !split[0].equalsIgnoreCase("e"))) {
            if (!split[0].equalsIgnoreCase("power") && (!allowShortName || !split[0].equalsIgnoreCase("p"))) {
               if (split[0].equalsIgnoreCase("duration") || allowShortName && split[0].equalsIgnoreCase("d")) {
                  if (!Util.isInt(split[1])) {
                     throw new Exception(I18n._("invalidPotionMeta", split[1]));
                  }

                  this.validPotionDuration = true;
                  this.duration = Integer.parseInt(split[1]) * 20;
               }
            } else {
               if (!Util.isInt(split[1])) {
                  throw new Exception(I18n._("invalidPotionMeta", split[1]));
               }

               this.validPotionPower = true;
               this.power = Integer.parseInt(split[1]);
               if (this.power > 0 && this.power < 4) {
                  --this.power;
               }
            }
         } else {
            this.pEffectType = Potions.getByName(split[1]);
            if (this.pEffectType == null || this.pEffectType.getName() == null) {
               throw new Exception(I18n._("invalidPotionMeta", split[1]));
            }

            if (!this.hasMetaPermission(sender, "potions." + this.pEffectType.getName().toLowerCase(Locale.ENGLISH), true, false, ess)) {
               throw new Exception(I18n._("noPotionEffectPerm", this.pEffectType.getName().toLowerCase(Locale.ENGLISH)));
            }

            this.validPotionEffect = true;
         }

         if (this.isValidPotion()) {
            PotionMeta pmeta = (PotionMeta)this.stack.getItemMeta();
            this.pEffect = this.pEffectType.createEffect(this.duration, this.power);
            if (pmeta.getCustomEffects().size() > 1 && !this.hasMetaPermission(sender, "potions.multiple", true, false, ess)) {
               throw new Exception(I18n._("multiplePotionEffects"));
            }

            pmeta.addCustomEffect(this.pEffect, true);
            this.stack.setItemMeta(pmeta);
            this.resetPotionMeta();
         }
      }

   }

   private void parseEnchantmentStrings(CommandSender sender, boolean allowUnsafe, String[] split, IEssentials ess) throws Exception {
      Enchantment enchantment = Enchantments.getByName(split[0]);
      if (enchantment != null && this.hasMetaPermission(sender, "enchantments." + enchantment.getName().toLowerCase(Locale.ENGLISH), false, false, ess)) {
         int level = -1;
         if (split.length > 1) {
            try {
               level = Integer.parseInt(split[1]);
            } catch (NumberFormatException var8) {
               level = -1;
            }
         }

         if (level < 0 || !allowUnsafe && level > enchantment.getMaxLevel()) {
            level = enchantment.getMaxLevel();
         }

         this.addEnchantment(sender, allowUnsafe, enchantment, level);
      }
   }

   public void addEnchantment(CommandSender sender, boolean allowUnsafe, Enchantment enchantment, int level) throws Exception {
      try {
         if (this.stack.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta)this.stack.getItemMeta();
            if (level == 0) {
               meta.removeStoredEnchant(enchantment);
            } else {
               meta.addStoredEnchant(enchantment, level, allowUnsafe);
            }

            this.stack.setItemMeta(meta);
         } else if (level == 0) {
            this.stack.removeEnchantment(enchantment);
         } else if (allowUnsafe) {
            this.stack.addUnsafeEnchantment(enchantment, level);
         } else {
            this.stack.addEnchantment(enchantment, level);
         }

      } catch (Exception ex) {
         throw new Exception("Enchantment " + enchantment.getName() + ": " + ex.getMessage(), ex);
      }
   }

   public Enchantment getEnchantment(User user, String name) throws Exception {
      Enchantment enchantment = Enchantments.getByName(name);
      if (enchantment == null) {
         return null;
      } else {
         String enchantmentName = enchantment.getName().toLowerCase(Locale.ENGLISH);
         if (!this.hasMetaPermission(user, "enchantments." + enchantmentName, true, false)) {
            throw new Exception(I18n._("enchantmentPerm", enchantmentName));
         } else {
            return enchantment;
         }
      }
   }

   private boolean hasMetaPermission(CommandSender sender, String metaPerm, boolean graceful, boolean includeBase, IEssentials ess) throws Exception {
      User user = ess.getUser(sender);
      return this.hasMetaPermission(user, metaPerm, graceful, includeBase);
   }

   private boolean hasMetaPermission(User user, String metaPerm, boolean graceful, boolean includeBase) throws Exception {
      String permBase = includeBase ? "essentials.itemspawn.meta-" : "essentials.";
      if (user != null && !user.isAuthorized(permBase + metaPerm)) {
         if (graceful) {
            return false;
         } else {
            throw new Exception(I18n._("noMetaPerm", metaPerm));
         }
      } else {
         return true;
      }
   }

   static {
      for(DyeColor color : DyeColor.values()) {
         colorMap.put(color.name(), color);
      }

      for(FireworkEffect.Type type : Type.values()) {
         fireworkShape.put(type.name(), type);
      }

   }
}
