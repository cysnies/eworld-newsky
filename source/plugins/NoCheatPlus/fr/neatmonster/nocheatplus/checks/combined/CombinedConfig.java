package fr.neatmonster.nocheatplus.checks.combined;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class CombinedConfig extends ACheckConfig {
   public static final CheckConfigFactory factory = new CheckConfigFactory() {
      public final ICheckConfig getConfig(Player player) {
         return CombinedConfig.getConfig(player);
      }
   };
   private static final Map worldsMap = new HashMap();
   public final boolean bedLeaveCheck;
   public final ActionList bedLeaveActions;
   public final boolean enderPearlCheck;
   public final boolean enderPearlPreventClickBlock;
   public final boolean improbableCheck;
   public final float improbableLevel;
   public final ActionList improbableActions;
   public final boolean invulnerableCheck;
   public final int invulnerableInitialTicksJoin;
   public final Set invulnerableIgnore = new HashSet();
   public final Map invulnerableModifiers = new HashMap();
   public final int invulnerableModifierDefault;
   public final boolean invulnerableTriggerAlways;
   public final boolean invulnerableTriggerFallDistance;
   public final boolean munchHausenCheck;
   public final ActionList munchHausenActions;
   public final float yawRate;
   public final boolean yawRateImprobable;
   public final float yawRatePenaltyFactor;
   public final int yawRatePenaltyMin;
   public final int yawRatePenaltyMax;

   public static CombinedConfig getConfig(Player player) {
      String worldName = player.getWorld().getName();
      CombinedConfig cc = (CombinedConfig)worldsMap.get(worldName);
      if (cc == null) {
         cc = new CombinedConfig(ConfigManager.getConfigFile(worldName));
         worldsMap.put(worldName, cc);
      }

      return cc;
   }

   public CombinedConfig(ConfigFile config) {
      super(config, "checks.combined.");
      this.bedLeaveCheck = config.getBoolean("checks.combined.bedleave.active");
      this.bedLeaveActions = (ActionList)config.getOptimizedActionList("checks.combined.bedleave.actions", "nocheatplus.checks.combined.bedleave");
      this.enderPearlCheck = config.getBoolean("checks.combined.enderpearl.active");
      this.enderPearlPreventClickBlock = config.getBoolean("checks.combined.enderpearl.preventclickblock");
      this.improbableCheck = config.getBoolean("checks.combined.improbable.active");
      this.improbableLevel = (float)config.getDouble("checks.combined.improbable.level");
      this.improbableActions = (ActionList)config.getOptimizedActionList("checks.combined.improbable.actions", "nocheatplus.checks.combined.improbable");
      this.invulnerableCheck = config.getBoolean("checks.combined.invulnerable.active");
      this.invulnerableInitialTicksJoin = config.getInt("checks.combined.invulnerable.initialticks.join");
      boolean error = false;

      for(String input : config.getStringList("checks.combined.invulnerable.ignore")) {
         String normInput = input.trim().toUpperCase();

         try {
            this.invulnerableIgnore.add(DamageCause.valueOf(normInput.replace(' ', '_').replace('-', '_')));
         } catch (Exception var11) {
            error = true;
            LogUtil.logWarning("[NoCheatPlus] Bad damage cause (combined.invulnerable.ignore): " + input);
         }
      }

      Integer defaultMod = 0;
      ConfigurationSection sec = config.getConfigurationSection("checks.combined.invulnerable.modifiers");

      for(String input : sec.getKeys(false)) {
         int modifier = sec.getInt(input, 0);
         String normInput = input.trim().toUpperCase();
         if (normInput.equals("ALL")) {
            defaultMod = modifier;
         } else {
            try {
               this.invulnerableModifiers.put(DamageCause.valueOf(normInput.replace(' ', '_').replace('-', '_')), modifier);
            } catch (Exception var10) {
               error = true;
               LogUtil.logWarning("[NoCheatPlus] Bad damage cause (combined.invulnerable.modifiers): " + input);
            }
         }
      }

      this.invulnerableModifierDefault = defaultMod;
      if (error) {
         LogUtil.logInfo("[NoCheatPlus] Damage causes can be: " + StringUtil.join(Arrays.asList(DamageCause.values()), ", "));
      }

      this.invulnerableTriggerAlways = config.getBoolean("checks.combined.invulnerable.triggers.always");
      this.invulnerableTriggerFallDistance = config.getBoolean("checks.combined.invulnerable.triggers.falldistance");
      this.munchHausenCheck = config.getBoolean("checks.combined.munchhausen.active");
      this.munchHausenActions = (ActionList)config.getOptimizedActionList("checks.combined.munchhausen.actions", "nocheatplus.checks.combined.munchhausen");
      this.yawRate = (float)config.getInt("checks.combined.yawrate.rate");
      this.yawRateImprobable = config.getBoolean("checks.combined.yawrate.improbable");
      this.yawRatePenaltyFactor = (float)config.getDouble("checks.combined.yawrate.penalty.factor");
      this.yawRatePenaltyMin = config.getInt("checks.combined.yawrate.penalty.minimum");
      this.yawRatePenaltyMax = config.getInt("checks.combined.yawrate.penalty.maximum");
   }

   public boolean isEnabled(CheckType checkType) {
      switch (checkType) {
         case COMBINED_IMPROBABLE:
            return this.improbableCheck;
         case COMBINED_BEDLEAVE:
            return this.bedLeaveCheck;
         case COMBINED_MUNCHHAUSEN:
            return this.munchHausenCheck;
         default:
            return false;
      }
   }

   public static void clear() {
      worldsMap.clear();
   }
}
