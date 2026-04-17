package org.yi.acru.bukkit;

import com.gmail.nossr50.mcMMO;
import com.griefcraft.lwc.LWCPlugin;
import com.massivecraft.factions.P;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.palmergames.bukkit.towny.Towny;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.bukkit.plugin.Plugin;

public class PluginCoreLink {
   private boolean linked = false;
   private boolean enabled = false;
   private LinkType type;
   private Plugin plugin;
   private Object data;

   PluginCoreLink(Plugin target, LinkType handler) {
      this.plugin = target;
      this.type = handler;
      this.data = null;
   }

   public boolean isLinked() {
      return this.linked;
   }

   protected void setLinked(boolean value) {
      this.linked = value;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   protected void setEnabled(boolean value) {
      this.enabled = value;
   }

   public LinkType getType() {
      return this.type;
   }

   public String getPluginName() {
      return this.plugin == null ? null : this.plugin.getDescription().getName();
   }

   public String getPluginVersion() {
      return this.plugin == null ? null : this.plugin.getDescription().getVersion();
   }

   protected GroupManager getGroupManager() {
      return (GroupManager)this.plugin;
   }

   protected Permissions getPermissions() {
      return (Permissions)this.plugin;
   }

   protected PermissionsPlugin getPermsBukkit() {
      return (PermissionsPlugin)this.plugin;
   }

   protected Towny getTowny() {
      return (Towny)this.plugin;
   }

   protected SimpleClans getSimpleClans() {
      return (SimpleClans)this.plugin;
   }

   protected mcMMO getMcmmo() {
      return (mcMMO)this.plugin;
   }

   protected P getFactions() {
      return (P)this.plugin;
   }

   protected LWCPlugin getLWCPlugin() {
      return (LWCPlugin)this.plugin;
   }

   protected void setData(Object value) {
      this.data = value;
   }

   protected WorldsHolder getWorldsHolder() {
      return (WorldsHolder)this.data;
   }

   protected PermissionHandler getPermissionHandler() {
      return (PermissionHandler)this.data;
   }

   public static enum LinkType {
      NONE,
      GROUPS,
      PERMISSIONS,
      ZONES,
      ECONOMY,
      GROUPS_PERMISSIONS,
      GROUPS_ZONES,
      PERMISSIONS_ZONES,
      GROUPS_PERMISSIONS_ZONES,
      GroupManager,
      Permissions;
   }
}
