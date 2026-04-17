package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.checks.AsyncCheck;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import java.util.Random;
import org.bukkit.entity.Player;

public class Captcha extends AsyncCheck implements ICaptcha {
   private final Random random = new Random();

   public Captcha() {
      super(CheckType.CHAT_CAPTCHA);
   }

   public void checkCaptcha(Player player, String message, ChatConfig cc, ChatData data, boolean isMainThread) {
      if (message.equals(data.captchaGenerated)) {
         data.reset();
         data.captchaStarted = false;
         player.sendMessage(ColorUtil.replaceColors(cc.captchaSuccess));
      } else {
         ++data.captchTries;
         ++data.captchaVL;
         if (data.captchTries > cc.captchaTries) {
            this.executeActions(player, data.captchaVL, (double)1.0F, cc.captchaActions, isMainThread);
         }

         if (player.isOnline()) {
            this.sendCaptcha(player, cc, data);
         }
      }

   }

   public void sendNewCaptcha(Player player, ChatConfig cc, ChatData data) {
      this.generateCaptcha(cc, data, true);
      this.sendCaptcha(player, cc, data);
      data.captchaStarted = true;
   }

   public void generateCaptcha(ChatConfig cc, ChatData data, boolean reset) {
      if (reset) {
         data.captchTries = 0;
      }

      char[] chars = new char[cc.captchaLength];

      for(int i = 0; i < cc.captchaLength; ++i) {
         chars[i] = cc.captchaCharacters.charAt(this.random.nextInt(cc.captchaCharacters.length()));
      }

      data.captchaGenerated = new String(chars);
   }

   public void resetCaptcha(Player player) {
      ChatData data = ChatData.getData(player);
      synchronized(data) {
         this.resetCaptcha(ChatConfig.getConfig(player), data);
      }
   }

   public void resetCaptcha(ChatConfig cc, ChatData data) {
      data.captchTries = 0;
      if (this.shouldCheckCaptcha(cc, data) || this.shouldStartCaptcha(cc, data)) {
         this.generateCaptcha(cc, data, true);
      }

   }

   public void sendCaptcha(Player player, ChatConfig cc, ChatData data) {
      player.sendMessage(ColorUtil.replaceColors(cc.captchaQuestion.replace("[captcha]", data.captchaGenerated)));
   }

   public boolean shouldStartCaptcha(ChatConfig cc, ChatData data) {
      return cc.captchaCheck && !data.captchaStarted && !data.hasCachedPermission("nocheatplus.checks.chat.captcha");
   }

   public boolean shouldCheckCaptcha(ChatConfig cc, ChatData data) {
      return cc.captchaCheck && data.captchaStarted && !data.hasCachedPermission("nocheatplus.checks.chat.captcha");
   }
}
