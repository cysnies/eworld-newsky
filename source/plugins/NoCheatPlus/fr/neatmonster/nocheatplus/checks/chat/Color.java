package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.checks.AsyncCheck;
import fr.neatmonster.nocheatplus.checks.CheckType;
import org.bukkit.entity.Player;

public class Color extends AsyncCheck {
   public Color() {
      super(CheckType.CHAT_COLOR);
   }

   public String check(Player player, String message, boolean isMainThread) {
      ChatConfig cc = ChatConfig.getConfig(player);
      ChatData data = ChatData.getData(player);
      synchronized(data) {
         if (message.contains("§")) {
            ++data.colorVL;
            if (this.executeActions(player, data.colorVL, (double)1.0F, cc.colorActions, isMainThread)) {
               return message.replaceAll("Â§.", "").replaceAll("§.", "");
            }
         }

         return message;
      }
   }
}
