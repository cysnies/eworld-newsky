package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.PlayerNeededException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitCommandSender extends LocalPlayer {
   private CommandSender sender;
   private WorldEditPlugin plugin;

   public BukkitCommandSender(WorldEditPlugin plugin, ServerInterface server, CommandSender sender) {
      super(server);
      this.plugin = plugin;
      this.sender = sender;
   }

   public String getName() {
      return this.sender.getName();
   }

   public void printRaw(String msg) {
      for(String part : msg.split("\n")) {
         this.sender.sendMessage(part);
      }

   }

   public void print(String msg) {
      for(String part : msg.split("\n")) {
         this.sender.sendMessage("§d" + part);
      }

   }

   public void printDebug(String msg) {
      for(String part : msg.split("\n")) {
         this.sender.sendMessage("§7" + part);
      }

   }

   public void printError(String msg) {
      for(String part : msg.split("\n")) {
         this.sender.sendMessage("§c" + part);
      }

   }

   public String[] getGroups() {
      return new String[0];
   }

   public boolean hasPermission(String perm) {
      return !this.plugin.getLocalConfiguration().noOpPermissions && this.sender.isOp() ? true : this.plugin.getPermissionsResolver().hasPermission((String)null, (String)this.sender.getName(), perm);
   }

   public boolean isPlayer() {
      return this.sender instanceof Player;
   }

   public int getItemInHand() {
      throw new PlayerNeededException();
   }

   public WorldVector getPosition() {
      throw new PlayerNeededException();
   }

   public LocalWorld getWorld() {
      throw new PlayerNeededException();
   }

   public double getPitch() {
      throw new PlayerNeededException();
   }

   public double getYaw() {
      throw new PlayerNeededException();
   }

   public void giveItem(int type, int amt) {
      throw new PlayerNeededException();
   }

   public void setPosition(Vector pos, float pitch, float yaw) {
      throw new PlayerNeededException();
   }

   public BlockBag getInventoryBlockBag() {
      throw new PlayerNeededException();
   }
}
