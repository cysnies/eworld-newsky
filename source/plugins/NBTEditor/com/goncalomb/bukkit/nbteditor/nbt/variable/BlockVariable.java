package com.goncalomb.bukkit.nbteditor.nbt.variable;

import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import org.bukkit.Material;

public final class BlockVariable extends NBTGenericVariable2X {
   private boolean _asShort;
   private boolean _dataAsInt;

   public BlockVariable(String blockNbtKey, String dataNbtKey) {
      this(blockNbtKey, dataNbtKey, false);
   }

   public BlockVariable(String blockNbtKey, String dataNbtKey, boolean asShort) {
      this(blockNbtKey, dataNbtKey, asShort, false);
   }

   public BlockVariable(String blockNbtKey, String dataNbtKey, boolean asShort, boolean dataAsInt) {
      super(blockNbtKey, dataNbtKey);
      this._asShort = asShort;
      this._dataAsInt = dataAsInt;
   }

   boolean set(NBTTagCompoundWrapper data, String value) {
      String[] pieces = value.split(":", 2);
      Material material = Material.getMaterial(pieces[0]);
      if (material == null) {
         try {
            int blockId = Integer.parseInt(pieces[0]);
            if (blockId < 0 || blockId > 255) {
               return false;
            }

            material = Material.getMaterial(blockId);
         } catch (NumberFormatException var8) {
            return false;
         }
      }

      if (material != null && material.isBlock()) {
         int blockData = 0;
         if (pieces.length == 2) {
            try {
               blockData = Integer.parseInt(pieces[1]);
               if (blockData < 0 || blockData > 255) {
                  return false;
               }
            } catch (NumberFormatException var7) {
               return false;
            }
         }

         if (this._asShort) {
            if (material.getId() > 127) {
               return false;
            }

            data.setShort(this._nbtKey, (short)(material.getId() & 255));
            data.setShort(this._nbtKey2, (short)(blockData & 255));
         } else {
            data.setInt(this._nbtKey, material.getId());
            if (this._dataAsInt) {
               data.setInt(this._nbtKey2, (byte)(blockData & 255));
            } else {
               data.setByte(this._nbtKey2, (byte)(blockData & 255));
            }
         }

         return true;
      } else {
         return false;
      }
   }

   String get(NBTTagCompoundWrapper data) {
      if (data.hasKey(this._nbtKey) && data.hasKey(this._nbtKey2)) {
         int materialId;
         int materialData;
         if (this._asShort) {
            materialId = data.getShort(this._nbtKey) & 255;
            materialData = data.getShort(this._nbtKey2) & 255;
         } else {
            materialId = data.getInt(this._nbtKey);
            if (this._dataAsInt) {
               materialData = data.getInt(this._nbtKey2) & 255;
            } else {
               materialData = data.getByte(this._nbtKey2) & 255;
            }
         }

         return Material.getMaterial(materialId).name() + ":" + materialData;
      } else {
         return null;
      }
   }

   String getFormat() {
      return Lang._("nbt.variable.formats.block");
   }
}
