package com.earth2me.essentials.settings;

import com.earth2me.essentials.storage.MapValueType;
import com.earth2me.essentials.storage.StorageObject;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;

public class Jails implements StorageObject {
   @MapValueType(Location.class)
   private Map jails = new HashMap();

   public Map getJails() {
      return this.jails;
   }

   public void setJails(Map jails) {
      this.jails = jails;
   }

   public String toString() {
      return "Jails(jails=" + this.getJails() + ")";
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Jails)) {
         return false;
      } else {
         Jails other = (Jails)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            if (this.getJails() == null) {
               if (other.getJails() != null) {
                  return false;
               }
            } else if (!this.getJails().equals(other.getJails())) {
               return false;
            }

            return true;
         }
      }
   }

   public boolean canEqual(Object other) {
      return other instanceof Jails;
   }

   public int hashCode() {
      int PRIME = 31;
      int result = 1;
      result = result * 31 + (this.getJails() == null ? 0 : this.getJails().hashCode());
      return result;
   }
}
