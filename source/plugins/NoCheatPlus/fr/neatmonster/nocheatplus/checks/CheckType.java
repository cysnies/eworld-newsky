package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakConfig;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakData;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractConfig;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractData;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceConfig;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceData;
import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.fight.FightConfig;
import fr.neatmonster.nocheatplus.checks.fight.FightData;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryConfig;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.players.DataManager;
import org.bukkit.entity.Player;

public enum CheckType {
   ALL("nocheatplus.checks"),
   BLOCKBREAK(BlockBreakConfig.factory, BlockBreakData.factory, "nocheatplus.checks.blockbreak"),
   BLOCKBREAK_BREAK(BLOCKBREAK, "nocheatplus.checks.blockbreak.break"),
   BLOCKBREAK_DIRECTION(BLOCKBREAK, "nocheatplus.checks.blockbreak.direction"),
   BLOCKBREAK_FASTBREAK(BLOCKBREAK, "nocheatplus.checks.blockbreak.fastbreak"),
   BLOCKBREAK_FREQUENCY(BLOCKBREAK, "nocheatplus.checks.blockbreak.frequency"),
   BLOCKBREAK_NOSWING(BLOCKBREAK, "nocheatplus.checks.blockbreak.noswing"),
   BLOCKBREAK_REACH(BLOCKBREAK, "nocheatplus.checks.blockbreak.reach"),
   BLOCKBREAK_WRONGBLOCK(BLOCKBREAK, "nocheatplus.checks.blockbreak.wrongblock"),
   BLOCKINTERACT(BlockInteractConfig.factory, BlockInteractData.factory, "nocheatplus.checks.blockinteract"),
   BLOCKINTERACT_DIRECTION(BLOCKINTERACT, "nocheatplus.checks.blockinteract.direction"),
   BLOCKINTERACT_REACH(BLOCKINTERACT, "nocheatplus.checks.blockinteract.reach"),
   BLOCKINTERACT_SPEED(BLOCKINTERACT, "nocheatplus.checks.blockinteract.speed"),
   BLOCKINTERACT_VISIBLE(BLOCKINTERACT, "nocheatplus.checks.blockinteract.visible"),
   BLOCKPLACE(BlockPlaceConfig.factory, BlockPlaceData.factory, "nocheatplus.checks.blockplace"),
   BLOCKPLACE_AGAINST(BLOCKPLACE, "nocheatplus.checks.blockplace.against"),
   BLOCKPLACE_AUTOSIGN(BLOCKPLACE, "nocheatplus.checks.blockplace.autosign"),
   BLOCKPLACE_DIRECTION(BLOCKPLACE, "nocheatplus.checks.blockplace.direction"),
   BLOCKPLACE_FASTPLACE(BLOCKPLACE, "nocheatplus.checks.blockplace.fastplace"),
   BLOCKPLACE_NOSWING(BLOCKPLACE, "nocheatplus.checks.blockplace.noswing"),
   BLOCKPLACE_REACH(BLOCKPLACE, "nocheatplus.checks.blockbreak.reach"),
   BLOCKPLACE_SPEED(BLOCKPLACE, "nocheatplus.checks.blockplace.speed"),
   CHAT(ChatConfig.factory, ChatData.factory, "nocheatplus.checks.chat"),
   CHAT_CAPTCHA(CHAT, "nocheatplus.checks.chat.captcha"),
   CHAT_COLOR(CHAT, "nocheatplus.checks.chat.color"),
   CHAT_COMMANDS(CHAT, "nocheatplus.checks.chat.commands"),
   CHAT_TEXT(CHAT, "nocheatplus.checks.chat.text"),
   CHAT_LOGINS(CHAT, "nocheatplus.checks.chat.logins"),
   CHAT_RELOG(CHAT, "nocheatplus.checks.chat.relog"),
   COMBINED(CombinedConfig.factory, CombinedData.factory, "nocheatplus.checks.combined"),
   COMBINED_BEDLEAVE(COMBINED, "nocheatplus.checks.combined.bedleave"),
   COMBINED_IMPROBABLE(COMBINED, "nocheatplus.checks.combined.improbable"),
   COMBINED_MUNCHHAUSEN(COMBINED, "nocheatplus.checks.combined.munchhausen"),
   FIGHT(FightConfig.factory, FightData.factory, "nocheatplus.checks.fight"),
   FIGHT_ANGLE(FIGHT, "nocheatplus.checks.fight.angle"),
   FIGHT_CRITICAL(FIGHT, "nocheatplus.checks.fight.critical"),
   FIGHT_DIRECTION(FIGHT, "nocheatplus.checks.fight.direction"),
   FIGHT_FASTHEAL(FIGHT, "nocheatplus.checks.fight.fastheal"),
   FIGHT_GODMODE(FIGHT, "nocheatplus.checks.fight.godmode"),
   FIGHT_KNOCKBACK(FIGHT, "nocheatplus.checks.fight.knockback"),
   FIGHT_NOSWING(FIGHT, "nocheatplus.checks.fight.noswing"),
   FIGHT_REACH(FIGHT, "nocheatplus.checks.fight.reach"),
   FIGHT_SELFHIT(FIGHT, "nocheatplus.checks.fight.selfhit"),
   FIGHT_SPEED(FIGHT, "nocheatplus.checks.fight.speed"),
   INVENTORY(InventoryConfig.factory, InventoryData.factory, "nocheatplus.checks.inventory"),
   INVENTORY_DROP(INVENTORY, "nocheatplus.checks.inventory.drop"),
   INVENTORY_FASTCLICK(INVENTORY, "nocheatplus.checks.inventory.fastclick"),
   INVENTORY_FASTCONSUME(INVENTORY, "nocheatplus.checks.inventory.fastconsume"),
   INVENTORY_INSTANTBOW(INVENTORY, "nocheatplus.checks.inventory.instantbow"),
   INVENTORY_INSTANTEAT(INVENTORY, "nocheatplus.checks.inventory.instanteat"),
   INVENTORY_ITEMS(INVENTORY, "nocheatplus.checks.inventory.items"),
   MOVING(MovingConfig.factory, MovingData.factory, "nocheatplus.checks.moving"),
   MOVING_CREATIVEFLY(MOVING, "nocheatplus.checks.moving.creativefly"),
   MOVING_MOREPACKETS(MOVING, "nocheatplus.checks.moving.morepackets"),
   MOVING_MOREPACKETSVEHICLE(MOVING, "nocheatplus.checks.moving.morepacketsvehicle"),
   MOVING_NOFALL(MOVING, "nocheatplus.checks.moving.nofall"),
   MOVING_PASSABLE(MOVING, "nocheatplus.checks.moving.passable"),
   MOVING_SURVIVALFLY(MOVING, "nocheatplus.checks.moving.survivalfly"),
   UNKNOWN;

   private final CheckType parent;
   private final CheckConfigFactory configFactory;
   private final CheckDataFactory dataFactory;
   private final String permission;

   private CheckType() {
      this((CheckConfigFactory)null, (CheckDataFactory)null, (String)null);
   }

   private CheckType(String permission) {
      this((CheckConfigFactory)null, (CheckDataFactory)null, permission);
   }

   private CheckType(CheckConfigFactory configFactory, CheckDataFactory dataFactory, String permission) {
      this((CheckType)null, permission, configFactory, dataFactory);
   }

   private CheckType(CheckType parent, String permission) {
      this(parent, permission, parent.getConfigFactory(), parent.getDataFactory());
   }

   private CheckType(CheckType parent, String permission, CheckConfigFactory configFactory, CheckDataFactory dataFactory) {
      this.parent = parent;
      this.permission = permission;
      this.configFactory = configFactory;
      this.dataFactory = dataFactory;
   }

   public CheckConfigFactory getConfigFactory() {
      return this.configFactory;
   }

   public CheckDataFactory getDataFactory() {
      return this.dataFactory;
   }

   public String getName() {
      return this.toString().toLowerCase().replace("_", ".");
   }

   public CheckType getParent() {
      return this.parent;
   }

   public String getPermission() {
      return this.permission;
   }

   public boolean hasCachedPermission(Player player) {
      return this.hasCachedPermission(player, this.getPermission());
   }

   public boolean hasCachedPermission(Player player, String permission) {
      return this.dataFactory.getData(player).hasCachedPermission(permission);
   }

   public final boolean isEnabled(Player player) {
      return this.configFactory.getConfig(player).isEnabled(this);
   }

   /** @deprecated */
   public static boolean removeData(String playerName, CheckType checkType) {
      return DataManager.removeData(playerName, checkType);
   }
}
