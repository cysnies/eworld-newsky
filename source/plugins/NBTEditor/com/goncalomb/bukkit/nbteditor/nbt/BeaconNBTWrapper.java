package com.goncalomb.bukkit.nbteditor.nbt;

import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffectType;

public final class BeaconNBTWrapper extends TileNBTWrapper {
   public BeaconNBTWrapper(Block block) {
      super(block);
   }

   public void setPrimary(PotionEffectType effect) {
      if (effect == null) {
         this._data.setInt("Primary", 0);
      } else {
         this._data.setInt("Primary", effect.getId());
      }

   }

   public void setSecondary(PotionEffectType effect) {
      if (effect == null) {
         this._data.setInt("Secondary", 0);
      } else {
         this._data.setInt("Secondary", effect.getId());
      }

   }
}
