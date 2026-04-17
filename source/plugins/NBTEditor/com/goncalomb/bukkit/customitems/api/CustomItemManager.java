package com.goncalomb.bukkit.customitems.api;

import com.goncalomb.bukkit.betterplugin.BetterPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class CustomItemManager {
   private static List _allowedPlugins = Arrays.asList("NBTEditor");
   private static boolean _isBondToCustomItems = false;
   private static CustomItemManager _instance;
   Plugin _plugin;
   private Listener _mainListener;
   private Logger _logger;
   private Permission _usePermission;
   private Permission _worldOverridePermission;
   private CustomItemContainer _generalContainer = new CustomItemContainer();
   private ListenerForInteractionEvents _interactionEventsListener = new ListenerForInteractionEvents();
   private ListenerForItemEvents _itemEventsListener = new ListenerForItemEvents();
   private ListenerForDispenseEvent _dispenseEventListener = new ListenerForDispenseEvent();
   private ListenerForPlayerDeathEvent _playerDeathEventListener = new ListenerForPlayerDeathEvent();
   private ListenerForBowEvents _bowEventsListener = new ListenerForBowEvents(this);
   private HashMap _customItemsBySlug = new HashMap();
   private HashMap _customItemsByPlugin = new HashMap();
   private HashMap _configsByPlugin = new HashMap();

   public static CustomItemManager getInstance(Plugin plugin) {
      if (plugin == null) {
         return null;
      } else if (_instance != null) {
         return !_isBondToCustomItems && !_allowedPlugins.contains(plugin.getName()) ? null : _instance;
      } else {
         _isBondToCustomItems = plugin.getName().equals("CustomItemsAPI");
         if (_isBondToCustomItems || _allowedPlugins.contains(plugin.getName())) {
            _instance = new CustomItemManager(plugin);
         }

         return _instance;
      }
   }

   private CustomItemManager(Plugin plugin) {
      if (!_isBondToCustomItems) {
         this._logger = new Logger((String)null, (String)null) {
            public void log(LogRecord logRecord) {
               logRecord.setMessage("[CustomItemsAPI] " + logRecord.getMessage());
               super.log(logRecord);
            }
         };
         this._logger.setLevel(Level.ALL);
         this._logger.setParent(Bukkit.getLogger());
         this._usePermission = new Permission("customitemsapi.use.*");
         this._usePermission.addParent("customitemsapi.*", true);
         Bukkit.getPluginManager().addPermission(this._usePermission);
         this._worldOverridePermission = new Permission("customitemsapi.world-override.*");
         this._worldOverridePermission.addParent("customitemsapi.*", true);
         Bukkit.getPluginManager().addPermission(this._worldOverridePermission);
      } else {
         this._logger = plugin.getLogger();
      }

      this.reboundListeners(plugin);
   }

   private void reboundListeners(Plugin plugin) {
      this._plugin = plugin;
      if (this._mainListener == null) {
         this._mainListener = new Listener() {
            @EventHandler
            private void inventoryClick(InventoryClickEvent event) {
               if (event.getInventory().getType() == InventoryType.ANVIL && CustomItemManager.this.getCustomItem(event.getCurrentItem()) != null) {
                  event.setCancelled(true);
               }

            }

            @EventHandler
            private void pluginDisable(PluginDisableEvent event) {
               CustomItemManager.this.remove(event.getPlugin());
               if (event.getPlugin() == CustomItemManager.this._plugin) {
                  if (CustomItemManager._isBondToCustomItems) {
                     CustomItemManager._isBondToCustomItems = false;
                  }

                  boolean rebounded = false;

                  for(Plugin plugin : CustomItemManager.this._customItemsByPlugin.keySet()) {
                     if (CustomItemManager._allowedPlugins.contains(plugin.getName())) {
                        if (!rebounded) {
                           CustomItemManager.this.reboundListeners(plugin);
                        }
                     } else {
                        CustomItemManager.this.remove(plugin);
                     }
                  }

                  if (!rebounded) {
                     CustomItemManager.this.destroy(true);
                     CustomItemManager.this._logger.info("CustomItemManager disposed!");
                  }
               }

            }
         };
      }

      this.destroy(false);
      PluginManager pluginManager = Bukkit.getPluginManager();
      pluginManager.registerEvents(this._interactionEventsListener, this._plugin);
      pluginManager.registerEvents(this._itemEventsListener, this._plugin);
      pluginManager.registerEvents(this._dispenseEventListener, this._plugin);
      pluginManager.registerEvents(this._playerDeathEventListener, this._plugin);
      pluginManager.registerEvents(this._bowEventsListener, this._plugin);
      this._logger.info("CustomItemManager bound to " + this._plugin.getName() + ".");
   }

   private void destroy(boolean hard) {
      HandlerList.unregisterAll(this._interactionEventsListener);
      HandlerList.unregisterAll(this._itemEventsListener);
      HandlerList.unregisterAll(this._dispenseEventListener);
      HandlerList.unregisterAll(this._playerDeathEventListener);
      HandlerList.unregisterAll(this._bowEventsListener);
      if (hard) {
         this._mainListener = null;
         this._generalContainer.clear();
         this._interactionEventsListener.clear();
         this._itemEventsListener.clear();
         this._dispenseEventListener.clear();
         this._playerDeathEventListener.clear();
         this._bowEventsListener.clear();
         this._customItemsBySlug.clear();
         this._customItemsByPlugin.clear();
         this._configsByPlugin.clear();
         _instance = null;
      }

   }

   private void remove(Plugin plugin) {
      if (this._customItemsByPlugin.remove(plugin) != null) {
         this._generalContainer.remove(plugin);
         this._interactionEventsListener.remove(plugin);
         this._itemEventsListener.remove(plugin);
         this._dispenseEventListener.remove(plugin);
         this._playerDeathEventListener.remove(plugin);
         this._bowEventsListener.remove(plugin);
         CustomItemContainer.removeFromHashMap(this._customItemsBySlug, plugin);
         this._customItemsByPlugin.remove(plugin);
         this._configsByPlugin.remove(plugin);
      }

   }

   File getDataFolder() {
      return new File(BetterPlugin.getGmbConfigFolder(), "CustomItemsAPI");
   }

   Logger getLogger() {
      return this._logger;
   }

   public boolean registerNew(CustomItem customItem, Plugin plugin) {
      if (customItem._owner == null && !this._customItemsBySlug.containsKey(customItem.getSlug())) {
         CustomItemConfig config = (CustomItemConfig)this._configsByPlugin.get(plugin);
         if (config == null) {
            config = new CustomItemConfig(this, plugin);
         }

         config.configureItem(customItem);
         boolean yep = false;
         yep |= this._interactionEventsListener.put(customItem);
         yep |= this._itemEventsListener.put(customItem);
         yep |= this._dispenseEventListener.put(customItem);
         yep |= this._playerDeathEventListener.put(customItem);
         if (customItem instanceof CustomBow) {
            yep |= this._bowEventsListener.put((CustomBow)customItem);
         }

         if (!yep) {
            this._logger.warning(customItem.getSlug() + " does not override any event methods!");
            config.removeItem(customItem);
            return false;
         } else {
            config.saveToFile();
            this._configsByPlugin.put(plugin, config);
            customItem._owner = plugin;
            (new Permission("customitemsapi.use." + customItem.getSlug())).addParent("customitemsapi.use.*", true);
            (new Permission("customitemsapi.world-override." + customItem.getSlug())).addParent("customitemsapi.world-override.*", true);
            this._generalContainer.put(customItem);
            this._customItemsBySlug.put(customItem.getSlug(), customItem);
            ArrayList<CustomItem> set = (ArrayList)this._customItemsByPlugin.get(plugin);
            if (set == null) {
               set = new ArrayList();
               this._customItemsByPlugin.put(plugin, set);
            }

            set.add(customItem);
            return true;
         }
      } else {
         this._logger.warning(plugin.getName() + " tried to register an already registed CustomItem, " + customItem.getSlug() + "!");
         return false;
      }
   }

   public Plugin[] getOwningPlugins() {
      return (Plugin[])this._customItemsByPlugin.keySet().toArray(new Plugin[0]);
   }

   public CustomItem[] getCustomItems(Plugin plugin) {
      ArrayList<CustomItem> set = (ArrayList)this._customItemsByPlugin.get(plugin);
      return set == null ? new CustomItem[0] : (CustomItem[])set.toArray(new CustomItem[0]);
   }

   public CustomItem getCustomItem(String slug) {
      return (CustomItem)this._customItemsBySlug.get(slug);
   }

   public CustomItem getCustomItem(ItemStack item) {
      return this._generalContainer.get(item);
   }
}
