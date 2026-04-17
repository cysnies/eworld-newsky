package com.goncalomb.bukkit;

import java.util.HashMap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.potion.PotionEffectType;

public final class PotionEffectsMap {
   private static final HashMap _potionEffectNames = new HashMap();
   private static final String _potionEffectStringList;

   static {
      _potionEffectNames.put("blindness", PotionEffectType.BLINDNESS);
      _potionEffectNames.put("nausea", PotionEffectType.CONFUSION);
      _potionEffectNames.put("resistance", PotionEffectType.DAMAGE_RESISTANCE);
      _potionEffectNames.put("haste", PotionEffectType.FAST_DIGGING);
      _potionEffectNames.put("fire-resistance", PotionEffectType.FIRE_RESISTANCE);
      _potionEffectNames.put("instant-damage", PotionEffectType.HARM);
      _potionEffectNames.put("instant-health", PotionEffectType.HEAL);
      _potionEffectNames.put("hunger", PotionEffectType.HUNGER);
      _potionEffectNames.put("strength", PotionEffectType.INCREASE_DAMAGE);
      _potionEffectNames.put("invisibility", PotionEffectType.INVISIBILITY);
      _potionEffectNames.put("jump-boost", PotionEffectType.JUMP);
      _potionEffectNames.put("night-vision", PotionEffectType.NIGHT_VISION);
      _potionEffectNames.put("poison", PotionEffectType.POISON);
      _potionEffectNames.put("regeneration", PotionEffectType.REGENERATION);
      _potionEffectNames.put("slowness", PotionEffectType.SLOW);
      _potionEffectNames.put("mining-fatigue", PotionEffectType.SLOW_DIGGING);
      _potionEffectNames.put("speed", PotionEffectType.SPEED);
      _potionEffectNames.put("water-breathing", PotionEffectType.WATER_BREATHING);
      _potionEffectNames.put("weakness", PotionEffectType.WEAKNESS);
      _potionEffectNames.put("wither", PotionEffectType.WITHER);
      _potionEffectNames.put("health-boost", PotionEffectType.HEALTH_BOOST);
      _potionEffectNames.put("absorption", PotionEffectType.ABSORPTION);
      _potionEffectNames.put("saturation", PotionEffectType.SATURATION);
      _potionEffectStringList = StringUtils.join(_potionEffectNames.keySet(), ", ");
   }

   private PotionEffectsMap() {
   }

   public static PotionEffectType getByName(String name) {
      return (PotionEffectType)_potionEffectNames.get(name.toLowerCase());
   }

   public static String getStringList() {
      return _potionEffectStringList;
   }
}
