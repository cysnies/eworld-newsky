package org.maxgamer.QuickShop.Listeners;

import com.dthielke.herochat.ChannelChatEvent;
import com.dthielke.herochat.Chatter.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.QuickShop.QuickShop;

public class HeroChatListener implements Listener {
   QuickShop plugin;

   public HeroChatListener(QuickShop plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onHeroChat(ChannelChatEvent e) {
      if (this.plugin.getShopManager().getActions().containsKey(e.getSender().getName())) {
         this.plugin.getShopManager().handleChat(e.getSender().getPlayer(), e.getMessage());
         e.setResult(Result.FAIL);
      }
   }
}
