package com.earth2me.essentials.settings;

import com.earth2me.essentials.storage.MapValueType;
import com.earth2me.essentials.storage.StorageObject;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;

public class Spawns implements StorageObject {
   @MapValueType(Location.class)
   private Map spawns = new HashMap();

   public Map getSpawns() {
      return this.spawns;
   }

   public void setSpawns(Map spawns) {
      this.spawns = spawns;
   }

   public String toString() {
      return "Spawns(spawns=" + this.getSpawns() + ")";
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Spawns)) {
         return false;
      } else {
         Spawns other = (Spawns)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            if (this.getSpawns() == null) {
               if (other.getSpawns() != null) {
                  return false;
               }
            } else if (!this.getSpawns().equals(other.getSpawns())) {
               return false;
            }

            return true;
         }
      }
   }

   public boolean canEqual(Object other) {
      return other instanceof Spawns;
   }

   public int hashCode() {
      int PRIME = 31;
      int result = 1;
      result = result * 31 + (this.getSpawns() == null ? 0 : this.getSpawns().hashCode());
      return result;
   }
}
