package com.earth2me.essentials.chat;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EssentialsChatPlayerListenerLowest extends EssentialsChatPlayer {
   public EssentialsChatPlayerListenerLowest(Server server, IEssentials ess, Map listeners, Map chatStorage) {
      super(server, ess, listeners, chatStorage);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      if (!this.isAborted(event)) {
         User user = this.ess.getUser(event.getPlayer());
         if (user == null) {
            event.setCancelled(true);
         } else {
            ChatStore chatStore = new ChatStore(this.ess, user, this.getChatType(event.getMessage()));
            this.setChatStore(event, chatStore);
            event.setMessage(Util.formatMessage(user, "essentials.chat", event.getMessage()));
            String group = user.getGroup();
            String world = user.getWorld().getName();
            MessageFormat format = this.ess.getSettings().getChatFormat(group);
            synchronized(format) {
               event.setFormat(format.format(new Object[]{group, world, world.substring(0, 1).toUpperCase(Locale.ENGLISH)}));
            }
         }
      }
   }
}
