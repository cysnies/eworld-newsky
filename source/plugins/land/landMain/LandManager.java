package landMain;

import event.LandRemoveEvent;
import flag.BanBonemealHandler;
import flag.BanBreakHandler;
import flag.BanBucketHandler;
import flag.BanButtonHandler;
import flag.BanCmdHandler;
import flag.BanConsumeHandler;
import flag.BanContactHandler;
import flag.BanContainerHandler;
import flag.BanDoorHandler;
import flag.BanEntityChangeHandler;
import flag.BanEyeHandler;
import flag.BanFlowHandler;
import flag.BanFlyHandler;
import flag.BanHideHandler;
import flag.BanIgniteHandler;
import flag.BanInteractHandler;
import flag.BanMonsterHandler;
import flag.BanMoveHandler;
import flag.BanPhysicalHandler;
import flag.BanPlaceHandler;
import flag.BanPotionHurtHandler;
import flag.BanPvpHandler;
import flag.BanTpHandler;
import flag.BanTpInHandler;
import flag.BanTpOutHandler;
import flag.BanUseHandler;
import flag.BanUseOtherHandler;
import flag.FixBugHandler;
import flag.FlyHandler;
import flag.ForcePvpHandler;
import flag.HouseHandler;
import flag.MineHandler;
import flag.PoisonWaterHandler;
import flag.PotionHandler;
import flag.ProtectHandler;
import flag.ResHandler;
import flag.StormHandler;
import java.util.List;
import land.EnterTip;
import land.Land;
import land.LandCmd;
import land.LandSpawn;
import land.LandUser;
import land.LeaveTip;
import land.Pos;
import land.Range;
import landCheck.LandCheck;
import landHandler.AdminHandler;
import landHandler.BuyHandler;
import landHandler.CmdHandler;
import landHandler.CreateHandler;
import landHandler.EnterLeaveHandler;
import landHandler.FlagHandler;
import landHandler.InHandler;
import landHandler.InfoHandler;
import landHandler.PersHandler;
import landHandler.RemoveHandler;
import landHandler.SelectHandler;
import landHandler.SellHandler;
import landHandler.SetHandler;
import landHandler.ShowHandler;
import landHandler.SubZoneHandler;
import landHandler.TipHandler;
import landHandler.TpHandler;
import landHandler.ZoneHandler;
import lib.hashList.HashList;
import lib.util.UtilFormat;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

public class LandManager {
   private String pn;
   private LandMain landMain;
   private Server server;
   private Dao dao;
   private LandCheck landCheck;
   private LandUserHandler landUserHandler;
   private EnterLeaveHandler enterLeaveHandler;
   private InfoHandler infoHandler;
   private RemoveHandler removeHandler;
   private SelectHandler selectHandler;
   private ShowHandler showHandler;
   private SellHandler sellHandler;
   private BuyHandler buyHandler;
   private TipHandler tipHandler;
   private CreateHandler createHandler;
   private ZoneHandler zoneHandler;
   private SubZoneHandler subZoneHandler;
   private SetHandler setHandler;
   private FlagHandler flagHandler;
   private AdminHandler adminHandler;
   private PersHandler persHandler;
   private BanBonemealHandler banBonemealHandler;
   private BanBreakHandler banBreakHandler;
   private BanPlaceHandler banPlaceHandler;
   private BanContainerHandler banContainerHandler;
   private BanButtonHandler banButtonHandler;
   private BanDoorHandler banDoorHandler;
   private BanEntityChangeHandler banEntityChangeHandler;
   private BanUseOtherHandler banUseOtherHandler;
   private FixBugHandler fixBugHandler;
   private FlyHandler flyHandler;
   private ForcePvpHandler forcePvpHandler;
   private BanUseHandler useHandler;
   private BanIgniteHandler banIgniteHandler;
   private BanInteractHandler banInteractHandler;
   private BanMonsterHandler banMonsterHandler;
   private BanMoveHandler banMoveHandler;
   private BanBucketHandler bucketHandler;
   private MineHandler mineHandler;
   private BanPotionHurtHandler banPotionHurtHandler;
   private BanPvpHandler pvpHandler;
   private TpHandler tpHandler;
   private BanTpHandler banTpHandler;
   private CmdHandler cmdHandler;
   private BanTpInHandler banInHandler;
   private BanTpOutHandler banOutHandler;
   private BanEyeHandler banEyeHandler;
   private BanFlyHandler banFlyHandler;
   private BanHideHandler banHideHandler;
   private BanCmdHandler banCmdHandler;
   private BanConsumeHandler banConsumeHandler;
   private BanContactHandler banContactHandler;
   private BanPhysicalHandler banPhysicalHandler;
   private PoisonWaterHandler waterHandler;
   private PotionHandler potionHandler;
   private ProtectHandler protectHandler;
   private ResHandler resHandler;
   private StormHandler stormHandler;
   private BanFlowHandler banFlowHandler;
   private HouseHandler houseHandler;
   private InHandler inHandler;

   public LandManager(LandMain landMain) {
      this.pn = landMain.getPn();
      this.landMain = landMain;
      this.server = landMain.getServer();
      this.dao = landMain.getDao();
      this.landCheck = new LandCheck(this);
      this.landUserHandler = new LandUserHandler(this);
      this.enterLeaveHandler = new EnterLeaveHandler(this);
      this.infoHandler = new InfoHandler(this);
      this.removeHandler = new RemoveHandler(this);
      this.selectHandler = new SelectHandler(this);
      this.showHandler = new ShowHandler(this);
      this.buyHandler = new BuyHandler(this);
      this.tipHandler = new TipHandler(this);
      this.createHandler = new CreateHandler(this);
      this.zoneHandler = new ZoneHandler(this);
      this.subZoneHandler = new SubZoneHandler(this);
      this.setHandler = new SetHandler(this);
      this.flagHandler = new FlagHandler(this);
      this.adminHandler = new AdminHandler(this);
      this.persHandler = new PersHandler(this);
      this.sellHandler = new SellHandler(this);
      this.banBonemealHandler = new BanBonemealHandler(this);
      this.banBreakHandler = new BanBreakHandler(this);
      this.banPlaceHandler = new BanPlaceHandler(this);
      this.banContainerHandler = new BanContainerHandler(this);
      this.banButtonHandler = new BanButtonHandler(this);
      this.banDoorHandler = new BanDoorHandler(this);
      this.banEntityChangeHandler = new BanEntityChangeHandler(this);
      this.banUseOtherHandler = new BanUseOtherHandler(this);
      this.fixBugHandler = new FixBugHandler(this);
      this.flyHandler = new FlyHandler(this);
      this.forcePvpHandler = new ForcePvpHandler(this);
      this.useHandler = new BanUseHandler(this);
      this.banIgniteHandler = new BanIgniteHandler(this);
      this.banInteractHandler = new BanInteractHandler(this);
      this.banMonsterHandler = new BanMonsterHandler(this);
      this.banMoveHandler = new BanMoveHandler(this);
      this.bucketHandler = new BanBucketHandler(this);
      this.mineHandler = new MineHandler(this);
      this.banPotionHurtHandler = new BanPotionHurtHandler(this);
      this.pvpHandler = new BanPvpHandler(this);
      this.tpHandler = new TpHandler(this);
      this.banTpHandler = new BanTpHandler(this);
      this.cmdHandler = new CmdHandler(this);
      this.banInHandler = new BanTpInHandler(this);
      this.banOutHandler = new BanTpOutHandler(this);
      this.banEyeHandler = new BanEyeHandler(this);
      this.banFlyHandler = new BanFlyHandler(this);
      this.banHideHandler = new BanHideHandler(this);
      this.banCmdHandler = new BanCmdHandler(this);
      this.banConsumeHandler = new BanConsumeHandler(this);
      this.banContactHandler = new BanContactHandler(this);
      this.banPhysicalHandler = new BanPhysicalHandler(this);
      this.waterHandler = new PoisonWaterHandler(this);
      this.potionHandler = new PotionHandler(this);
      this.protectHandler = new ProtectHandler(this);
      this.resHandler = new ResHandler(this);
      this.stormHandler = new StormHandler(this);
      this.banFlowHandler = new BanFlowHandler(this);
      this.houseHandler = new HouseHandler(this);
      this.inHandler = new InHandler(this);
      this.flagHandler.init();
      Land.init(this);
      Pos.init(this);
   }

   public static Land createLand(int type, boolean overlap, String name, String owner, Range range, int level) {
      return Land.createLand(type, overlap, name, owner, range, level);
   }

   public static Land createLand(int type, boolean overlap, String owner, Range range, int level) {
      return Land.createLand(type, overlap, owner, range, level);
   }

   public Land getLand(CommandSender sender, String s) {
      Land land;
      try {
         long id = (long)Integer.parseInt(s);
         land = this.getLand(id);
         if (land == null) {
            if (sender != null) {
               sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(145)}));
            }

            return null;
         }
      } catch (NumberFormatException var6) {
         land = this.getLand(s);
         if (land == null) {
            if (sender != null) {
               sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(150)}));
            }

            return null;
         }
      }

      return land;
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public Server getServer() {
      return this.server;
   }

   public LandMain getLandMain() {
      return this.landMain;
   }

   public String getUnusedName() {
      return this.landCheck.getUnusedName();
   }

   public Land getLand(long landId) {
      return this.landCheck.getLand(landId);
   }

   public Land getLand(String name) {
      return this.landCheck.getLand(name);
   }

   public HashList getUserLands(String name) {
      return this.landCheck.getUserLands(name);
   }

   public HashList getSystemLands() {
      return this.landCheck.getSystemLands();
   }

   public HashList getAllLands() {
      return this.landCheck.getAllLands();
   }

   public void registerEvents(Listener listener) {
      this.server.getPluginManager().registerEvents(listener, this.landMain);
   }

   public void register(String flag, String tip, boolean use, boolean player, boolean value, String per) {
      this.flagHandler.register(flag, tip, use, player, value, per);
   }

   public HashList getLands(Location l) {
      return this.landCheck.getLands(Pos.getPos(l));
   }

   public Land getHighestPriorityLand(Location l) {
      return this.landCheck.getHighestPriorityLand(l);
   }

   public LandUserHandler getLandUserHandler() {
      return this.landUserHandler;
   }

   public CreateHandler getCreateHandler() {
      return this.createHandler;
   }

   public TipHandler getTipHandler() {
      return this.tipHandler;
   }

   public SelectHandler getSelectHandler() {
      return this.selectHandler;
   }

   public RemoveHandler getRemoveHandler() {
      return this.removeHandler;
   }

   public SetHandler getSetHandler() {
      return this.setHandler;
   }

   public EnterLeaveHandler getEnterLeaveHandler() {
      return this.enterLeaveHandler;
   }

   public InfoHandler getInfoHandler() {
      return this.infoHandler;
   }

   public BuyHandler getBuyHandler() {
      return this.buyHandler;
   }

   public ShowHandler getShowHandler() {
      return this.showHandler;
   }

   public void remove(Land land) {
      this.dao.remove(land);
      LandRemoveEvent landRemoveEvent = new LandRemoveEvent(land);
      this.server.getPluginManager().callEvent(landRemoveEvent);
   }

   public void addLand(Land land) {
      this.dao.addLand(land);
   }

   public void addLandUser(LandUser landUser) {
      this.dao.addLandUser(landUser);
   }

   public int getFixedLevel(int level, Range range, Land ignoreLand) {
      return this.landCheck.getFixedLevel(level, range, ignoreLand);
   }

   public List getAllLandUsers() {
      return this.dao.getAllLandUsers();
   }

   public List getAllLandsFromDB() {
      return this.dao.getAllLands();
   }

   public List getAllEnterTips() {
      return this.dao.getAllEnterTips();
   }

   public List getAllLeaveTips() {
      return this.dao.getAllLeaveTips();
   }

   public void addEnterLandTip(EnterTip enterTip) {
      this.dao.addEnterLandTip(enterTip);
   }

   public void removeEnterLandTip(EnterTip enterTip) {
      this.dao.removeEnterLandTip(enterTip);
   }

   public void addLeaveLandTip(LeaveTip leaveTip) {
      this.dao.addLeaveLandTip(leaveTip);
   }

   public void removeLeaveLandTip(LeaveTip leaveTip) {
      this.dao.removeLeaveLandTip(leaveTip);
   }

   public BanPlaceHandler getBuildHandler() {
      return this.banPlaceHandler;
   }

   public BanUseHandler getUseHandler() {
      return this.useHandler;
   }

   public MineHandler getMineHandler() {
      return this.mineHandler;
   }

   public BanPvpHandler getPvpHandler() {
      return this.pvpHandler;
   }

   public List getAllLandSpawns() {
      return this.dao.getAllLandSpawns();
   }

   public List getAllLandCmds() {
      return this.dao.getAllLandCmds();
   }

   public void addLandSpawn(LandSpawn landSpawn) {
      this.dao.addLandSpawn(landSpawn);
   }

   public void addLandCmd(LandCmd landCmd) {
      this.dao.addLandCmd(landCmd);
   }

   public void removeLandSpawn(LandSpawn landSpawn) {
      this.dao.removeLandSpawn(landSpawn);
   }

   public void removeLandCmd(LandCmd landCmd) {
      this.dao.removeLandCmd(landCmd);
   }

   public CmdHandler getCmdHandler() {
      return this.cmdHandler;
   }

   public FlagHandler getFlagHandler() {
      return this.flagHandler;
   }

   public AdminHandler getAdminHandler() {
      return this.adminHandler;
   }

   public SellHandler getSellHandler() {
      return this.sellHandler;
   }

   public BanCmdHandler getBanCmdHandler() {
      return this.banCmdHandler;
   }

   public BanBucketHandler getBucketHandler() {
      return this.bucketHandler;
   }

   public LandCheck getLandCheck() {
      return this.landCheck;
   }

   public BanPhysicalHandler getBanPhysicalHandler() {
      return this.banPhysicalHandler;
   }

   public BanTpInHandler getBanInHandler() {
      return this.banInHandler;
   }

   public BanEyeHandler getBanEyeHandler() {
      return this.banEyeHandler;
   }

   public BanFlyHandler getBanFlyHandler() {
      return this.banFlyHandler;
   }

   public PoisonWaterHandler getWaterHandler() {
      return this.waterHandler;
   }

   public ProtectHandler getProtectHandler() {
      return this.protectHandler;
   }

   public PersHandler getPersHandler() {
      return this.persHandler;
   }

   public ZoneHandler getZoneHandler() {
      return this.zoneHandler;
   }

   public SubZoneHandler getSubZoneHandler() {
      return this.subZoneHandler;
   }

   public TpHandler getTpHandler() {
      return this.tpHandler;
   }

   public BanTpHandler getBanTpHandler() {
      return this.banTpHandler;
   }

   public BanContainerHandler getBanContainerHandler() {
      return this.banContainerHandler;
   }

   public BanButtonHandler getBanButtonHandler() {
      return this.banButtonHandler;
   }

   public BanDoorHandler getBanDoorHandler() {
      return this.banDoorHandler;
   }

   public BanUseOtherHandler getBanUseOtherHandler() {
      return this.banUseOtherHandler;
   }

   public BanInteractHandler getBanInteractHandler() {
      return this.banInteractHandler;
   }

   public BanFlowHandler getBanFlowHandler() {
      return this.banFlowHandler;
   }

   public HouseHandler getHouseHandler() {
      return this.houseHandler;
   }

   public BanBreakHandler getBanBreakHandler() {
      return this.banBreakHandler;
   }

   public BanMonsterHandler getBanMonsterHandler() {
      return this.banMonsterHandler;
   }

   public BanIgniteHandler getBanIgniteHandler() {
      return this.banIgniteHandler;
   }

   public BanEntityChangeHandler getBanEntityChangeHandler() {
      return this.banEntityChangeHandler;
   }

   public BanContactHandler getBanContactHandler() {
      return this.banContactHandler;
   }

   public BanTpOutHandler getBanOutHandler() {
      return this.banOutHandler;
   }

   public BanPotionHurtHandler getBanPotionHurtHandler() {
      return this.banPotionHurtHandler;
   }

   public FlyHandler getFlyHandler() {
      return this.flyHandler;
   }

   public PotionHandler getPotionHandler() {
      return this.potionHandler;
   }

   public ForcePvpHandler getForcePvpHandler() {
      return this.forcePvpHandler;
   }

   public BanConsumeHandler getBanConsumeHandler() {
      return this.banConsumeHandler;
   }

   public BanMoveHandler getBanMoveHandler() {
      return this.banMoveHandler;
   }

   public InHandler getInHandler() {
      return this.inHandler;
   }

   public BanBonemealHandler getBanTntHandler() {
      return this.banBonemealHandler;
   }

   public BanHideHandler getBanHideHandler() {
      return this.banHideHandler;
   }

   public StormHandler getStormHandler() {
      return this.stormHandler;
   }

   public FixBugHandler getFixBugHandler() {
      return this.fixBugHandler;
   }

   public ResHandler getResHandler() {
      return this.resHandler;
   }
}
