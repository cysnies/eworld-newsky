package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

public interface ICaptcha {
   void checkCaptcha(Player var1, String var2, ChatConfig var3, ChatData var4, boolean var5);

   void sendCaptcha(Player var1, ChatConfig var2, ChatData var3);

   void sendNewCaptcha(Player var1, ChatConfig var2, ChatData var3);

   boolean shouldCheckCaptcha(ChatConfig var1, ChatData var2);

   boolean shouldStartCaptcha(ChatConfig var1, ChatData var2);

   void resetCaptcha(ChatConfig var1, ChatData var2);

   void resetCaptcha(Player var1);

   void generateCaptcha(ChatConfig var1, ChatData var2, boolean var3);
}
