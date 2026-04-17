package com.earth2me.essentials.chat;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EssentialsChatPlayerListenerNormal extends EssentialsChatPlayer {
   public EssentialsChatPlayerListenerNormal(Server server, IEssentials ess, Map listeners, Map chatStorage) {
      super(server, ess, listeners, chatStorage);
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      if (!this.isAborted(event)) {
         long radius = (long)this.ess.getSettings().getChatRadius();
         if (radius >= 1L) {
            radius *= radius;
            ChatStore chatStore = this.getChatStore(event);
            User user = chatStore.getUser();
            chatStore.setRadius(radius);
            if (event.getMessage().length() > 1 && chatStore.getType().length() > 0) {
               StringBuilder permission = new StringBuilder();
               permission.append("essentials.chat.").append(chatStore.getType());
               if (user.isAuthorized(permission.toString())) {
                  StringBuilder format = new StringBuilder();
                  format.append(chatStore.getType()).append("Format");
                  event.setMessage(event.getMessage().substring(1));
                  event.setFormat(I18n._(format.toString(), new Object[]{event.getFormat()}));
               } else {
                  StringBuilder errorMsg = new StringBuilder();
                  errorMsg.append("notAllowedTo").append(chatStore.getType().substring(0, 1).toUpperCase(Locale.ENGLISH)).append(chatStore.getType().substring(1));
                  user.sendMessage(I18n._(errorMsg.toString(), new Object[0]));
                  event.setCancelled(true);
               }
            } else {
               this.sendLocalChat(event, chatStore);
            }
         }
      }
   }
}
