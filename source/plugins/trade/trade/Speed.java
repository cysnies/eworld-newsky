package trade;

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Speed implements Listener {
   private HashMap speedHash = new HashMap();

   public void register(String plugin, String type) {
      if (!this.speedHash.containsKey(plugin)) {
         this.speedHash.put(plugin, new HashMap());
      }

      ((HashMap)this.speedHash.get(plugin)).put(type, new HashMap());
   }

   public String check(Player p, String plugin, String type, int limit) {
      try {
         String name = p.getName();
         long now = System.currentTimeMillis();
         Long pre = (Long)((HashMap)((HashMap)this.speedHash.get(plugin)).get(type)).get(name);
         if (pre != null && now - pre < (long)limit) {
            int wait = (int)((long)limit - (now - pre));
            int second = wait / 1000;
            int milli = wait % 1000;
            String msg = Util.convert("&c你的速度过快(&6请等候{0}秒{1}毫秒&c)".replace("{0}", "" + second).replace("{1}", "" + milli));
            return msg;
         } else {
            ((HashMap)((HashMap)this.speedHash.get(plugin)).get(type)).put(name, now);
            return null;
         }
      } catch (Exception e) {
         e.printStackTrace();
         return Util.convert("&c异常");
      }
   }
}
