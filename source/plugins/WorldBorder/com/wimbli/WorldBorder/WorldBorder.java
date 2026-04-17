package com.wimbli.WorldBorder;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldBorder extends JavaPlugin {
   public static WorldBorder plugin;

   public WorldBorder() {
      plugin = this;
   }

   public void onEnable() {
      Config.load(this, false);
      this.getCommand("wborder").setExecutor(new WBCommand(this));
      this.getServer().getPluginManager().registerEvents(new WBListener(), this);
      DynMapFeatures.setup();
      Location spawn = ((World)this.getServer().getWorlds().get(0)).getSpawnLocation();
      System.out.println("For reference, the main world's spawn location is at X: " + Config.coord.format(spawn.getX()) + " Y: " + Config.coord.format(spawn.getY()) + " Z: " + Config.coord.format(spawn.getZ()));
   }

   public void onDisable() {
      DynMapFeatures.removeAllBorders();
      Config.StopBorderTimer();
      Config.StoreFillTask();
      Config.StopFillTask();
   }

   public BorderData GetWorldBorder(String worldName) {
      return Config.Border(worldName);
   }
}
