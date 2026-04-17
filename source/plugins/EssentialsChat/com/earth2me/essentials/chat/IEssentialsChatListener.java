package com.earth2me.essentials.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public interface IEssentialsChatListener {
   boolean shouldHandleThisChat(AsyncPlayerChatEvent var1);

   String modifyMessage(AsyncPlayerChatEvent var1, Player var2, String var3);
}
