package lib.tab;

import java.util.HashMap;
import org.bukkit.plugin.Plugin;

public class TabObject {
   HashMap tabs = new HashMap();

   public void setPriority(Plugin p, int pri) {
      for(int a = -1; a < 4; ++a) {
         if (this.tabs.get(a) != null && ((TabHolder)this.tabs.get(a)).p == p) {
            this.tabs.put(a, (Object)null);
         }
      }

      if (pri > -2) {
         TabHolder t = new TabHolder(p);
         this.tabs.put(pri, t);
      }

   }

   public TabHolder getTab() {
      int a;
      for(a = 3; this.tabs.get(a) == null && a > -3; --a) {
      }

      return a == -2 ? new TabHolder((Plugin)null) : (TabHolder)this.tabs.get(a);
   }

   public void setTab(Plugin plugin, int x, int y, String msg, int ping) {
      int a;
      for(a = -1; (this.tabs.get(a) == null || ((TabHolder)this.tabs.get(a)).p != plugin) && a < 3; ++a) {
      }

      if (a == 3 && (this.tabs.get(a) == null || ((TabHolder)this.tabs.get(a)).p != plugin)) {
         this.setPriority(plugin, 0);
         a = 0;
      }

      TabHolder t = (TabHolder)this.tabs.get(a);
      t.tabs[y][x] = msg;
      t.tabPings[y][x] = ping;
      t.maxh = 3;
      t.maxv = Math.max(x + 1, t.maxv);
   }
}
