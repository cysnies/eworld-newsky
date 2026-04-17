package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.checks.inventory.FastConsume;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DefaultComponentFactory {
   public Collection getAvailableComponentsOnEnable() {
      List<Object> available = new LinkedList();

      try {
         FastConsume.testAvailability();
         available.add(new FastConsume());
      } catch (Throwable var3) {
         LogUtil.logInfo("[NoCheatPlus] Inventory checks: FastConsume is not available.");
      }

      return available;
   }
}
