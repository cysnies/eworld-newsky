package com.sk89q.worldedit.bukkit;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.cui.CUIEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BukkitPlayer extends LocalPlayer {
   private Player player;
   private WorldEditPlugin plugin;

   public BukkitPlayer(WorldEditPlugin plugin, ServerInterface server, Player player) {
      super(server);
      this.plugin = plugin;
      this.player = player;
   }

   public int getItemInHand() {
      ItemStack itemStack = this.player.getItemInHand();
      return itemStack != null ? itemStack.getTypeId() : 0;
   }

   public String getName() {
      return this.player.getName();
   }

   public WorldVector getPosition() {
      Location loc = this.player.getLocation();
      return new WorldVector(BukkitUtil.getLocalWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
   }

   public double getPitch() {
      return (double)this.player.getLocation().getPitch();
   }

   public double getYaw() {
      return (double)this.player.getLocation().getYaw();
   }

   public void giveItem(int type, int amt) {
      this.player.getInventory().addItem(new ItemStack[]{new ItemStack(type, amt)});
   }

   public void printRaw(String msg) {
      for(String part : msg.split("\n")) {
         this.player.sendMessage(part);
      }

   }

   public void print(String msg) {
      for(String part : msg.split("\n")) {
         this.player.sendMessage("§d" + part);
      }

   }

   public void printDebug(String msg) {
      for(String part : msg.split("\n")) {
         this.player.sendMessage("§7" + part);
      }

   }

   public void printError(String msg) {
      for(String part : msg.split("\n")) {
         this.player.sendMessage("§c" + part);
      }

   }

   public void setPosition(Vector pos, float pitch, float yaw) {
      this.player.teleport(new Location(this.player.getWorld(), pos.getX(), pos.getY(), pos.getZ(), yaw, pitch));
   }

   public String[] getGroups() {
      return this.plugin.getPermissionsResolver().getGroups((OfflinePlayer)this.player);
   }

   public BlockBag getInventoryBlockBag() {
      return new BukkitPlayerBlockBag(this.player);
   }

   public boolean hasPermission(String perm) {
      return !this.plugin.getLocalConfiguration().noOpPermissions && this.player.isOp() || this.plugin.getPermissionsResolver().hasPermission(this.player.getWorld().getName(), (OfflinePlayer)this.player, perm);
   }

   public LocalWorld getWorld() {
      return BukkitUtil.getLocalWorld(this.player.getWorld());
   }

   public void dispatchCUIEvent(CUIEvent event) {
      String[] params = event.getParameters();
      String send = event.getTypeId();
      if (params.length > 0) {
         send = send + "|" + StringUtil.joinString(params, "|");
      }

      this.player.sendPluginMessage(this.plugin, "WECUI", send.getBytes(CUIChannelListener.UTF_8_CHARSET));
   }

   public Player getPlayer() {
      return this.player;
   }

   public boolean hasCreativeMode() {
      return this.player.getGameMode() == GameMode.CREATIVE;
   }
}
