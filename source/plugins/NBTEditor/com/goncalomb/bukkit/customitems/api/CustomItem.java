package com.goncalomb.bukkit.customitems.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

public abstract class CustomItem {
   Plugin _owner;
   private String _slug;
   private String _name;
   private MaterialData _material;
   private ItemStack _item;
   LinkedHashMap _defaultConfig = new LinkedHashMap();
   private boolean _enabled;
   private HashSet _allowedWorlds = new HashSet();
   private HashSet _blockedWorlds = new HashSet();

   protected CustomItem(String slug, String name, MaterialData material) {
      this._slug = slug;
      this._name = name;
      this._material = material;
      this._item = this._material.toItemStack(1);
      if (this._material.getItemType().getMaxDurability() > 0) {
         this._material.setData((byte)0);
      }

      this.setDefaultConfig("enabled", true);
      this.setDefaultConfig("name", this._name);
      this.setDefaultConfig("lore", new ArrayList());
      this.setDefaultConfig("allowed-worlds", new ArrayList());
      this.setDefaultConfig("blocked-worlds", new ArrayList());
   }

   protected final void setLore(List lore) {
      this.setDefaultConfig("lore", lore);
   }

   protected final void setLore(String... lore) {
      this.setDefaultConfig("lore", Arrays.asList(lore));
   }

   protected final void addEnchantment(Enchantment enchantment, int level) {
      if (this._owner == null) {
         ItemMeta meta = this._item.getItemMeta();
         meta.addEnchant(enchantment, level, true);
         this._item.setItemMeta(meta);
      }

   }

   protected final void setDefaultConfig(String name, Object value) {
      if (this._owner == null) {
         this._defaultConfig.put(name, value);
      }

   }

   public final Plugin getPlugin() {
      return this._owner;
   }

   public final String getSlug() {
      return this._slug;
   }

   public final String getName() {
      return this._name;
   }

   public final MaterialData getMaterial() {
      return this._material;
   }

   public ItemStack getItem() {
      return this._item.clone();
   }

   protected void applyConfig(ConfigurationSection section) {
      this._enabled = section.getBoolean("enabled");
      this._name = section.getString("name");
      ItemMeta meta = this._item.getItemMeta();
      if (meta instanceof BookMeta) {
         ((BookMeta)meta).setTitle(this._name);
      } else {
         meta.setDisplayName(this._name);
      }

      meta.setLore(section.getStringList("lore"));
      this._item.setItemMeta(meta);
      List<String> allowedWorlds = section.getStringList("allowed-worlds");
      if (allowedWorlds != null && allowedWorlds.size() != 0) {
         this._allowedWorlds = new HashSet(allowedWorlds);
      } else {
         List<String> blockedWorlds = section.getStringList("blocked-worlds");
         this._blockedWorlds = blockedWorlds.size() > 0 ? new HashSet(blockedWorlds) : null;
         this._allowedWorlds = null;
      }

   }

   public void onLeftClick(PlayerInteractEvent event, PlayerDetails details) {
   }

   public void onRightClick(PlayerInteractEvent event, PlayerDetails details) {
   }

   public void onAttack(EntityDamageByEntityEvent event, PlayerDetails details) {
   }

   public void onInteractEntity(PlayerInteractEntityEvent event, PlayerDetails details) {
   }

   public void onPickup(PlayerPickupItemEvent event) {
   }

   public void onDrop(PlayerDropItemEvent event) {
   }

   public void onDespawn(ItemDespawnEvent event) {
   }

   public void onDropperPickup(InventoryPickupItemEvent event) {
   }

   public void onDispense(BlockDispenseEvent event, DispenserDetails details) {
   }

   public void onPlayerDeath(PlayerDeathEvent event, PlayerInventoryDetails details) {
   }

   public final boolean isEnabled() {
      return this._enabled;
   }

   public final boolean isValidWorld(World world) {
      String wName = world.getName();
      if (this._allowedWorlds == null) {
         return this._blockedWorlds == null || !this._blockedWorlds.contains(wName);
      } else {
         return this._allowedWorlds.contains(wName);
      }
   }

   static String getItemName(ItemStack item) {
      if (item != null) {
         ItemMeta meta = item.getItemMeta();
         if (meta != null) {
            if (meta instanceof BookMeta) {
               return ((BookMeta)meta).getTitle();
            }

            return meta.getDisplayName();
         }
      }

      return null;
   }
}
