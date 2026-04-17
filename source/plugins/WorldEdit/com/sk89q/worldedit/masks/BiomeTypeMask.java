package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import java.util.HashSet;
import java.util.Set;

public class BiomeTypeMask implements Mask {
   private Set biomes;

   public BiomeTypeMask() {
      this(new HashSet());
   }

   public BiomeTypeMask(Set biomes) {
      this.biomes = biomes;
   }

   public void prepare(LocalSession session, LocalPlayer player, Vector target) {
   }

   public boolean matches2D(EditSession editSession, Vector2D pos) {
      BiomeType biome = editSession.getWorld().getBiome(pos);
      return this.biomes.contains(biome);
   }

   public boolean matches(EditSession editSession, Vector pos) {
      return this.matches2D(editSession, pos.toVector2D());
   }
}
