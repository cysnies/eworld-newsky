package com.goncalomb.bukkit;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public abstract class CustomInventory {
   private static Listener _mainListener;
   private static Plugin _plugin;
   private static HashMap _openedInvsByPlayer = new HashMap();
   private static HashMap _openedInvsByPlugin = new HashMap();
   private Plugin _owner;
   protected final Inventory _inventory;

   private static final void bindListener(Plugin plugin) {
      if (_plugin == null) {
         if (_mainListener == null) {
            _mainListener = new Listener() {
               @EventHandler
               private void pluginDisable(PluginDisableEvent event) {
                  HashSet<CustomInventory> invs = (HashSet)CustomInventory._openedInvsByPlugin.remove(event.getPlugin());
                  if (invs != null) {
                     for(CustomInventory inv : invs) {
                        HumanEntity[] var8;
                        for(HumanEntity human : var8 = (HumanEntity[])inv._inventory.getViewers().toArray(new HumanEntity[0])) {
                           CustomInventory._openedInvsByPlayer.remove(human);
                           human.closeInventory();
                        }
                     }
                  }

                  if (CustomInventory._plugin == event.getPlugin()) {
                     CustomInventory._plugin = null;
                     HandlerList.unregisterAll(CustomInventory._mainListener);
                     if (CustomInventory._openedInvsByPlugin.size() > 0) {
                        CustomInventory.bindListener((Plugin)CustomInventory._openedInvsByPlugin.keySet().iterator().next());
                     }
                  }

               }

               @EventHandler
               private void inventoryClick(InventoryClickEvent event) {
                  CustomInventory inv = (CustomInventory)CustomInventory._openedInvsByPlayer.get(event.getWhoClicked());
                  if (inv != null) {
                     inv.inventoryClick(event);
                  }

               }

               @EventHandler
               private void inventoryClose(InventoryCloseEvent event) {
                  CustomInventory inv = (CustomInventory)CustomInventory._openedInvsByPlayer.remove(event.getPlayer());
                  if (inv != null) {
                     ((HashSet)CustomInventory._openedInvsByPlugin.get(inv._owner)).remove(inv);
                     inv.inventoryClose(event);
                  }

               }
            };
         }

         Bukkit.getPluginManager().registerEvents(_mainListener, plugin);
         _plugin = plugin;
      }

   }

   public CustomInventory(Player owner, int size) {
      this._inventory = Bukkit.createInventory(owner, size);
   }

   public CustomInventory(Player owner, int size, String title) {
      this._inventory = Bukkit.createInventory(owner, size, title);
   }

   public final void openInventory(Player player, Plugin owner) {
      if (this._owner == null) {
         player.openInventory(this._inventory);
         _openedInvsByPlayer.put(player, this);
         this._owner = owner;
         HashSet<CustomInventory> set = (HashSet)_openedInvsByPlugin.get(player);
         if (set == null) {
            set = new HashSet();
            _openedInvsByPlugin.put(owner, set);
         }

         set.add(this);
         bindListener(owner);
      }

   }

   public Inventory getInventory() {
      return this._inventory;
   }

   public Plugin getPlugin() {
      return this._owner;
   }

   public final void close() {
      if (this._owner != null) {
         HumanEntity[] var4;
         for(HumanEntity human : var4 = (HumanEntity[])this._inventory.getViewers().toArray(new HumanEntity[0])) {
            human.closeInventory();
         }
      }

   }

   protected abstract void inventoryClick(InventoryClickEvent var1);

   protected abstract void inventoryClose(InventoryCloseEvent var1);
}
