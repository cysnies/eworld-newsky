package com.wimbli.WorldBorder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

public class DynMapFeatures {
   private static DynmapAPI api;
   private static MarkerAPI markApi;
   private static MarkerSet markSet;
   private static int lineWeight = 3;
   private static double lineOpacity = (double)1.0F;
   private static int lineColor = 16711680;
   private static Map roundBorders = new HashMap();
   private static Map squareBorders = new HashMap();

   public static boolean renderEnabled() {
      return api != null;
   }

   public static boolean borderEnabled() {
      return markApi != null;
   }

   public static void setup() {
      Plugin test = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
      if (test != null && test.isEnabled()) {
         api = (DynmapAPI)test;

         try {
            Class.forName("org.dynmap.markers.CircleMarker");
            if (api.getDynmapVersion().startsWith("0.35-")) {
               throw new ClassNotFoundException();
            }
         } catch (ClassNotFoundException var2) {
            Config.LogConfig("DynMap is available, but border display is currently disabled: you need DynMap v0.36 or newer.");
            return;
         } catch (NullPointerException var3) {
            Config.LogConfig("DynMap is present, but an NPE (type 1) was encountered while trying to integrate. Border display disabled.");
            return;
         }

         try {
            markApi = api.getMarkerAPI();
            if (markApi == null) {
               return;
            }
         } catch (NullPointerException var4) {
            Config.LogConfig("DynMap is present, but an NPE (type 2) was encountered while trying to integrate. Border display disabled.");
            return;
         }

         showAllBorders();
         Config.LogConfig("Successfully hooked into DynMap for the ability to display borders.");
      }
   }

   public static void renderRegion(String worldName, CoordXZ coord) {
      if (renderEnabled()) {
         World world = Bukkit.getWorld(worldName);
         int y = world != null ? world.getMaxHeight() : 255;
         int x = CoordXZ.regionToBlock(coord.x);
         int z = CoordXZ.regionToBlock(coord.z);
         api.triggerRenderOfVolume(worldName, x, 0, z, x + 511, y, z + 511);
      }
   }

   public static void renderChunks(String worldName, List coords) {
      if (renderEnabled()) {
         World world = Bukkit.getWorld(worldName);
         int y = world != null ? world.getMaxHeight() : 255;

         for(CoordXZ coord : coords) {
            renderChunk(worldName, coord, y);
         }

      }
   }

   public static void renderChunk(String worldName, CoordXZ coord, int maxY) {
      if (renderEnabled()) {
         int x = CoordXZ.chunkToBlock(coord.x);
         int z = CoordXZ.chunkToBlock(coord.z);
         api.triggerRenderOfVolume(worldName, x, 0, z, x + 15, maxY, z + 15);
      }
   }

   public static void showAllBorders() {
      if (borderEnabled()) {
         removeAllBorders();
         if (!Config.DynmapBorderEnabled()) {
            if (markSet != null) {
               markSet.deleteMarkerSet();
            }

            markSet = null;
         } else {
            markSet = markApi.getMarkerSet("worldborder.markerset");
            if (markSet == null) {
               markSet = markApi.createMarkerSet("worldborder.markerset", "WorldBorder", (Set)null, false);
            } else {
               markSet.setMarkerSetLabel("WorldBorder");
            }

            Map<String, BorderData> borders = Config.getBorders();

            for(Map.Entry wdata : borders.entrySet()) {
               String worldName = (String)wdata.getKey();
               BorderData border = (BorderData)wdata.getValue();
               showBorder(worldName, border);
            }

         }
      }
   }

   public static void showBorder(String worldName, BorderData border) {
      if (borderEnabled()) {
         if (Config.DynmapBorderEnabled()) {
            label34: {
               if (border.getShape() == null) {
                  if (Config.ShapeRound()) {
                     break label34;
                  }
               } else if (border.getShape()) {
                  break label34;
               }

               showSquareBorder(worldName, border);
               return;
            }

            showRoundBorder(worldName, border);
         }
      }
   }

   private static void showRoundBorder(String worldName, BorderData border) {
      if (squareBorders.containsKey(worldName)) {
         removeBorder(worldName);
      }

      CircleMarker marker = (CircleMarker)roundBorders.get(worldName);
      if (marker == null) {
         marker = markSet.createCircleMarker("worldborder_" + worldName, Config.DynmapMessage(), false, worldName, border.getX(), (double)64.0F, border.getZ(), (double)border.getRadiusX(), (double)border.getRadiusZ(), true);
         marker.setLineStyle(lineWeight, lineOpacity, lineColor);
         marker.setFillStyle((double)0.0F, 0);
         roundBorders.put(worldName, marker);
      } else {
         marker.setCenter(worldName, border.getX(), (double)64.0F, border.getZ());
         marker.setRadius((double)border.getRadiusX(), (double)border.getRadiusZ());
      }

   }

   private static void showSquareBorder(String worldName, BorderData border) {
      if (roundBorders.containsKey(worldName)) {
         removeBorder(worldName);
      }

      double[] xVals = new double[]{border.getX() - (double)border.getRadiusX(), border.getX() + (double)border.getRadiusX()};
      double[] zVals = new double[]{border.getZ() - (double)border.getRadiusZ(), border.getZ() + (double)border.getRadiusZ()};
      AreaMarker marker = (AreaMarker)squareBorders.get(worldName);
      if (marker == null) {
         marker = markSet.createAreaMarker("worldborder_" + worldName, Config.DynmapMessage(), false, worldName, xVals, zVals, true);
         marker.setLineStyle(3, (double)1.0F, 16711680);
         marker.setFillStyle((double)0.0F, 0);
         squareBorders.put(worldName, marker);
      } else {
         marker.setCornerLocations(xVals, zVals);
      }

   }

   public static void removeAllBorders() {
      if (borderEnabled()) {
         for(CircleMarker marker : roundBorders.values()) {
            marker.deleteMarker();
         }

         roundBorders.clear();

         for(AreaMarker marker : squareBorders.values()) {
            marker.deleteMarker();
         }

         squareBorders.clear();
      }
   }

   public static void removeBorder(String worldName) {
      if (borderEnabled()) {
         CircleMarker marker = (CircleMarker)roundBorders.remove(worldName);
         if (marker != null) {
            marker.deleteMarker();
         }

         AreaMarker marker2 = (AreaMarker)squareBorders.remove(worldName);
         if (marker2 != null) {
            marker2.deleteMarker();
         }

      }
   }
}
