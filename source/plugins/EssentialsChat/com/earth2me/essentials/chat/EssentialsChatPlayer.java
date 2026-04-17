package com.earth2me.essentials.chat;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public abstract class EssentialsChatPlayer implements Listener {
   protected transient IEssentials ess;
   protected static final Logger logger = Logger.getLogger("Minecraft");
   protected final transient Map listeners;
   protected final transient Server server;
   protected final transient Map chatStorage;

   public EssentialsChatPlayer(Server server, IEssentials ess, Map listeners, Map chatStorage) {
      this.ess = ess;
      this.listeners = listeners;
      this.server = server;
      this.chatStorage = chatStorage;
   }

   public void onPlayerChat(AsyncPlayerChatEvent event) {
   }

   public boolean isAborted(AsyncPlayerChatEvent event) {
      if (event.isCancelled()) {
         return true;
      } else {
         synchronized(this.listeners) {
            Iterator i$ = this.listeners.entrySet().iterator();

            while(true) {
               if (i$.hasNext()) {
                  Map.Entry<String, IEssentialsChatListener> listener = (Map.Entry)i$.next();

                  boolean var10000;
                  try {
                     if (!((IEssentialsChatListener)listener.getValue()).shouldHandleThisChat(event)) {
                        continue;
                     }

                     var10000 = true;
                  } catch (Throwable t) {
                     if (this.ess.getSettings().isDebug()) {
                        logger.log(Level.WARNING, "Error with EssentialsChat listener of " + (String)listener.getKey() + ": " + t.getMessage(), t);
                        continue;
                     }

                     logger.log(Level.WARNING, "Error with EssentialsChat listener of " + (String)listener.getKey() + ": " + t.getMessage());
                     continue;
                  }

                  return var10000;
               }

               return false;
            }
         }
      }
   }

   public String getChatType(String message) {
      switch (message.charAt(0)) {
         case '!':
            return "shout";
         case '?':
            return "question";
         default:
            return "";
      }
   }

   public ChatStore getChatStore(AsyncPlayerChatEvent event) {
      return (ChatStore)this.chatStorage.get(event);
   }

   public void setChatStore(AsyncPlayerChatEvent event, ChatStore chatStore) {
      this.chatStorage.put(event, chatStore);
   }

   public ChatStore delChatStore(AsyncPlayerChatEvent event) {
      return (ChatStore)this.chatStorage.remove(event);
   }

   protected void charge(User user, Trade charge) throws ChargeException {
      charge.charge(user);
   }

   protected boolean charge(AsyncPlayerChatEvent event, ChatStore chatStore) {
      try {
         this.charge(chatStore.getUser(), chatStore.getCharge());
         return true;
      } catch (ChargeException e) {
         this.ess.showError(chatStore.getUser(), e, chatStore.getLongType());
         event.setCancelled(true);
         return false;
      }
   }

   protected void sendLocalChat(AsyncPlayerChatEvent event, ChatStore chatStore) {
      event.setCancelled(true);
      User sender = chatStore.getUser();
      logger.info(I18n._("localFormat", new Object[]{sender.getName(), event.getMessage()}));
      Location loc = sender.getLocation();
      World world = loc.getWorld();
      if (this.charge(event, chatStore)) {
         Iterator i$ = event.getRecipients().iterator();

         while(true) {
            Player onlinePlayer;
            String type;
            User onlineUser;
            while(true) {
               if (!i$.hasNext()) {
                  return;
               }

               onlinePlayer = (Player)i$.next();
               type = I18n._("chatTypeLocal", new Object[0]);
               onlineUser = this.ess.getUser(onlinePlayer);
               if (onlineUser.equals(sender)) {
                  break;
               }

               boolean abort = false;
               Location playerLoc = onlineUser.getLocation();
               if (playerLoc.getWorld() != world) {
                  abort = true;
               } else {
                  double delta = playerLoc.distanceSquared(loc);
                  if (delta > (double)chatStore.getRadius()) {
                     abort = true;
                  }
               }

               if (!abort) {
                  break;
               }

               if (onlineUser.isAuthorized("essentials.chat.spy")) {
                  type = type.concat(I18n._("chatTypeSpy", new Object[0]));
                  break;
               }
            }

            String message = type.concat(String.format(event.getFormat(), sender.getDisplayName(), event.getMessage()));
            synchronized(this.listeners) {
               for(Map.Entry listener : this.listeners.entrySet()) {
                  try {
                     message = ((IEssentialsChatListener)listener.getValue()).modifyMessage(event, onlinePlayer, message);
                  } catch (Throwable t) {
                     if (this.ess.getSettings().isDebug()) {
                        logger.log(Level.WARNING, "Error with EssentialsChat listener of " + (String)listener.getKey() + ": " + t.getMessage(), t);
                     } else {
                        logger.log(Level.WARNING, "Error with EssentialsChat listener of " + (String)listener.getKey() + ": " + t.getMessage());
                     }
                  }
               }
            }

            onlineUser.sendMessage(message);
         }
      }
   }
}
