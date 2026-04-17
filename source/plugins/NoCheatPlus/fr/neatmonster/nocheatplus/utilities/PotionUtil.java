package fr.neatmonster.nocheatplus.utilities;

import java.util.Collection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionUtil {
   public static final double getPotionEffectAmplifier(Player player, PotionEffectType type) {
      if (!player.hasPotionEffect(type)) {
         return Double.NEGATIVE_INFINITY;
      } else {
         Collection<PotionEffect> effects = player.getActivePotionEffects();
         double max = Double.NEGATIVE_INFINITY;

         for(PotionEffect effect : effects) {
            if (effect.getType().equals(type)) {
               max = Math.max(max, (double)effect.getAmplifier());
            }
         }

         return max;
      }
   }
}
