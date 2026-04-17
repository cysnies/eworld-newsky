package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class InventoryData extends ACheckData {
   public static final CheckDataFactory factory = new CheckDataFactory() {
      public final ICheckData getData(Player player) {
         return InventoryData.getData(player);
      }

      public ICheckData removeData(String playerName) {
         return InventoryData.removeData(playerName);
      }

      public void removeAllData() {
         InventoryData.clear();
      }
   };
   private static final Map playersMap = new HashMap();
   public double dropVL;
   public double fastClickVL;
   public double instantBowVL;
   public double instantEatVL;
   public long lastClickTime = 0L;
   public int dropCount;
   public long dropLastTime;
   public final ActionFrequency fastClickFreq = new ActionFrequency(5, 200L);
   public Material fastClickLastCursor = null;
   public Material fastClickLastClicked = null;
   public int fastClickLastCursorAmount = 0;
   public long instantBowInteract;
   public long instantBowShoot;
   public Material instantEatFood;
   public long instantEatInteract;

   public static InventoryData getData(Player player) {
      if (!playersMap.containsKey(player.getName())) {
         playersMap.put(player.getName(), new InventoryData());
      }

      return (InventoryData)playersMap.get(player.getName());
   }

   public static ICheckData removeData(String playerName) {
      return (ICheckData)playersMap.remove(playerName);
   }

   public static void clear() {
      playersMap.clear();
   }
}
