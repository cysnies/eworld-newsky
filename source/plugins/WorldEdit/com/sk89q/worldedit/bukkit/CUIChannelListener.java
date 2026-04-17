package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.LocalSession;
import java.nio.charset.Charset;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class CUIChannelListener implements PluginMessageListener {
   public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
   private final WorldEditPlugin plugin;

   public CUIChannelListener(WorldEditPlugin plugin) {
      this.plugin = plugin;
   }

   public void onPluginMessageReceived(String channel, Player player, byte[] message) {
      LocalSession session = this.plugin.getSession(player);
      if (!session.hasCUISupport()) {
         String text = new String(message, UTF_8_CHARSET);
         session.handleCUIInitializationMessage(text);
      }
   }
}
