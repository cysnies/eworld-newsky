package com.goncalomb.bukkit.nbteditor.commands;

import com.goncalomb.bukkit.PotionEffectsMap;
import com.goncalomb.bukkit.Utils;
import com.goncalomb.bukkit.UtilsMc;
import com.goncalomb.bukkit.betterplugin.BetterCommand;
import com.goncalomb.bukkit.betterplugin.BetterCommandException;
import com.goncalomb.bukkit.betterplugin.BetterCommandType;
import com.goncalomb.bukkit.betterplugin.Lang;
import com.goncalomb.bukkit.betterplugin.SubCommand;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandNBTPotion extends BetterCommand {
   public CommandNBTPotion() {
      super("nbtpotion", "nbteditor.potion");
      this.setAlises(new String[]{"nbtp"});
      this.setDescription(Lang._("nbt.cmds.nbtp.description"));
   }

   @SubCommand.Command(
      args = "",
      type = BetterCommandType.PLAYER_ONLY,
      maxargs = 3,
      usage = "<effect> [level] [duration]"
   )
   public boolean potionCommand(CommandSender sender, String[] args) throws BetterCommandException {
      if (args.length > 0) {
         HandItemWrapper.Potion item = new HandItemWrapper.Potion((Player)sender);
         PotionEffectType effect = PotionEffectsMap.getByName(args[0]);
         if (effect != null) {
            int level = 1;
            if (args.length >= 2) {
               level = Utils.parseInt(args[1], 32767, 0, -1);
               if (level == -1) {
                  sender.sendMessage(Lang._("nbt.invalid-level"));
                  return true;
               }
            }

            int duration = effect != PotionEffectType.HARM && effect != PotionEffectType.HEAL ? 600 : 0;
            if (args.length == 3) {
               duration = UtilsMc.parseTickDuration(args[2]);
               if (duration == -1) {
                  sender.sendMessage(Lang._("common.invalid-duration"));
                  return true;
               }
            }

            if (level == 0) {
               List<PotionEffect> effects = ((PotionMeta)item.meta).getCustomEffects();
               ((PotionMeta)item.meta).clearCustomEffects();

               for(PotionEffect eff : effects) {
                  if (!eff.getType().equals(effect)) {
                     ((PotionMeta)item.meta).addCustomEffect(eff, true);
                  }
               }

               sender.sendMessage(Lang._("nbt.cmds.nbtp.removed"));
            } else {
               ((PotionMeta)item.meta).addCustomEffect(new PotionEffect(effect, duration, level - 1), true);
               sender.sendMessage(Lang._("nbt.cmds.nbtp.added"));
            }

            item.save();
            return true;
         }

         sender.sendMessage(Lang._("nbt.cmds.nbtp.invalid-effect"));
      }

      sender.sendMessage(Lang._("nbt.effects-prefix") + PotionEffectsMap.getStringList());
      sender.sendMessage(Lang._("nbt.cmds.nbtp.info"));
      return false;
   }
}
