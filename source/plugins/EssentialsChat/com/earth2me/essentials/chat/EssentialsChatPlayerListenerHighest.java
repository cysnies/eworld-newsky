package com.earth2me.essentials.chat;

import com.earth2me.essentials.IEssentials;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EssentialsChatPlayerListenerHighest extends EssentialsChatPlayer {
   public EssentialsChatPlayerListenerHighest(Server server, IEssentials ess, Map listeners, Map chatStorage) {
      super(server, ess, listeners, chatStorage);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      ChatStore chatStore = this.delChatStore(event);
      if (!this.isAborted(event) && chatStore != null) {
         this.charge(event, chatStore);
      }
   }
}
