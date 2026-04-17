package fr.neatmonster.nocheatplus.players;

import fr.neatmonster.nocheatplus.components.IData;
import java.util.HashSet;
import java.util.Set;

public class PlayerData implements IData {
   public static final String TAG_NOTIFY_OFF = "notify_off";
   public final PlayerTask task;
   protected Set tags = null;
   final String lcName;

   public PlayerData(String playerName) {
      this.lcName = playerName.toLowerCase();
      this.task = new PlayerTask(this.lcName);
   }

   public boolean hasTag(String tag) {
      return this.tags != null && this.tags.contains(tag);
   }

   public void addTag(String tag) {
      if (this.tags == null) {
         this.tags = new HashSet();
      }

      this.tags.add(tag);
   }

   public void removeTag(String tag) {
      if (this.tags != null) {
         this.tags.remove(tag);
         if (this.tags.isEmpty()) {
            this.tags = null;
         }
      }

   }

   public void setTag(String tag, boolean add) {
      if (add) {
         this.addTag(tag);
      } else {
         this.removeTag(tag);
      }

   }

   public boolean getNotifyOff() {
      return this.hasTag("notify_off");
   }

   public void setNotifyOff(boolean notifyOff) {
      this.setTag("notify_off", notifyOff);
   }
}
