package com.sk89q.worldedit;

import com.sk89q.jchronic.Chronic;
import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.cui.CUIEvent;
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionShapeEvent;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.snapshots.Snapshot;
import com.sk89q.worldedit.tools.BlockTool;
import com.sk89q.worldedit.tools.BrushTool;
import com.sk89q.worldedit.tools.SinglePickaxe;
import com.sk89q.worldedit.tools.Tool;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;

public class LocalSession {
   public static int MAX_HISTORY_SIZE = 15;
   public static int EXPIRATION_GRACE = 600000;
   private LocalConfiguration config;
   private long expirationTime;
   private RegionSelector selector;
   private boolean placeAtPos1;
   private LinkedList history;
   private int historyPointer;
   private CuboidClipboard clipboard;
   private boolean toolControl;
   private boolean superPickaxe;
   private BlockTool pickaxeMode;
   private Map tools;
   private int maxBlocksChanged;
   private boolean useInventory;
   private Snapshot snapshot;
   private String lastScript;
   private boolean beenToldVersion;
   private boolean hasCUISupport;
   private int cuiVersion;
   private boolean fastMode;
   private Mask mask;
   private TimeZone timezone;

   public LocalSession(LocalConfiguration config) {
      this.expirationTime = System.currentTimeMillis() + (long)EXPIRATION_GRACE;
      this.selector = new CuboidRegionSelector();
      this.placeAtPos1 = false;
      this.history = new LinkedList();
      this.historyPointer = 0;
      this.toolControl = true;
      this.superPickaxe = false;
      this.pickaxeMode = new SinglePickaxe();
      this.tools = new HashMap();
      this.maxBlocksChanged = -1;
      this.beenToldVersion = false;
      this.hasCUISupport = false;
      this.cuiVersion = -1;
      this.fastMode = false;
      this.timezone = TimeZone.getDefault();
      this.config = config;
   }

   public TimeZone getTimeZone() {
      return this.timezone;
   }

   public void setTimezone(TimeZone timezone) {
      this.timezone = timezone;
   }

   public void clearHistory() {
      this.history.clear();
      this.historyPointer = 0;
   }

   public void remember(EditSession editSession) {
      if (editSession.size() != 0) {
         while(this.historyPointer < this.history.size()) {
            this.history.remove(this.historyPointer);
         }

         this.history.add(editSession);

         while(this.history.size() > MAX_HISTORY_SIZE) {
            this.history.remove(0);
         }

         this.historyPointer = this.history.size();
      }
   }

   public EditSession undo(BlockBag newBlockBag, LocalPlayer player) {
      --this.historyPointer;
      if (this.historyPointer >= 0) {
         EditSession editSession = (EditSession)this.history.get(this.historyPointer);
         EditSession newEditSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(editSession.getWorld(), -1, newBlockBag, player);
         newEditSession.enableQueue();
         newEditSession.setFastMode(this.fastMode);
         editSession.undo(newEditSession);
         return editSession;
      } else {
         this.historyPointer = 0;
         return null;
      }
   }

   public EditSession redo(BlockBag newBlockBag, LocalPlayer player) {
      if (this.historyPointer < this.history.size()) {
         EditSession editSession = (EditSession)this.history.get(this.historyPointer);
         EditSession newEditSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(editSession.getWorld(), -1, newBlockBag, player);
         newEditSession.enableQueue();
         newEditSession.setFastMode(this.fastMode);
         editSession.redo(newEditSession);
         ++this.historyPointer;
         return editSession;
      } else {
         return null;
      }
   }

   public RegionSelector getRegionSelector(LocalWorld world) {
      if (this.selector.getIncompleteRegion().getWorld() == null) {
         this.selector = new CuboidRegionSelector(world);
      } else if (!this.selector.getIncompleteRegion().getWorld().equals(world)) {
         this.selector.getIncompleteRegion().setWorld(world);
         this.selector.clear();
      }

      return this.selector;
   }

   /** @deprecated */
   @Deprecated
   public RegionSelector getRegionSelector() {
      return this.selector;
   }

   public void setRegionSelector(LocalWorld world, RegionSelector selector) {
      selector.getIncompleteRegion().setWorld(world);
      this.selector = selector;
   }

   /** @deprecated */
   @Deprecated
   public boolean isRegionDefined() {
      return this.selector.isDefined();
   }

   public boolean isSelectionDefined(LocalWorld world) {
      return this.selector.getIncompleteRegion().getWorld() != null && this.selector.getIncompleteRegion().getWorld().equals(world) ? this.selector.isDefined() : false;
   }

   /** @deprecated */
   @Deprecated
   public Region getRegion() throws IncompleteRegionException {
      return this.selector.getRegion();
   }

   public Region getSelection(LocalWorld world) throws IncompleteRegionException {
      if (this.selector.getIncompleteRegion().getWorld() != null && this.selector.getIncompleteRegion().getWorld().equals(world)) {
         return this.selector.getRegion();
      } else {
         throw new IncompleteRegionException();
      }
   }

   public LocalWorld getSelectionWorld() {
      return this.selector.getIncompleteRegion().getWorld();
   }

   public CuboidClipboard getClipboard() throws EmptyClipboardException {
      if (this.clipboard == null) {
         throw new EmptyClipboardException();
      } else {
         return this.clipboard;
      }
   }

   public void setClipboard(CuboidClipboard clipboard) {
      this.clipboard = clipboard;
   }

   public boolean isToolControlEnabled() {
      return this.toolControl;
   }

   public void setToolControl(boolean toolControl) {
      this.toolControl = toolControl;
   }

   public int getBlockChangeLimit() {
      return this.maxBlocksChanged;
   }

   public void setBlockChangeLimit(int maxBlocksChanged) {
      this.maxBlocksChanged = maxBlocksChanged;
   }

   public boolean hasSuperPickAxe() {
      return this.superPickaxe;
   }

   public void enableSuperPickAxe() {
      this.superPickaxe = true;
   }

   public void disableSuperPickAxe() {
      this.superPickaxe = false;
   }

   public boolean toggleSuperPickAxe() {
      this.superPickaxe = !this.superPickaxe;
      return this.superPickaxe;
   }

   public Vector getPlacementPosition(LocalPlayer player) throws IncompleteRegionException {
      return (Vector)(!this.placeAtPos1 ? player.getBlockIn() : this.selector.getPrimaryPosition());
   }

   public boolean togglePlacementPosition() {
      this.placeAtPos1 = !this.placeAtPos1;
      return this.placeAtPos1;
   }

   public BlockBag getBlockBag(LocalPlayer player) {
      return !this.useInventory ? null : player.getInventoryBlockBag();
   }

   public Snapshot getSnapshot() {
      return this.snapshot;
   }

   public void setSnapshot(Snapshot snapshot) {
      this.snapshot = snapshot;
   }

   public BlockTool getSuperPickaxe() {
      return this.pickaxeMode;
   }

   public void setSuperPickaxe(BlockTool tool) {
      this.pickaxeMode = tool;
   }

   public Tool getTool(int item) {
      return (Tool)this.tools.get(item);
   }

   public BrushTool getBrushTool(int item) throws InvalidToolBindException {
      Tool tool = this.getTool(item);
      if (tool == null || !(tool instanceof BrushTool)) {
         tool = new BrushTool("worldedit.brush.sphere");
         this.setTool(item, tool);
      }

      return (BrushTool)tool;
   }

   public void setTool(int item, Tool tool) throws InvalidToolBindException {
      if (item > 0 && item < 255) {
         throw new InvalidToolBindException(item, "Blocks can't be used");
      } else if (item == this.config.wandItem) {
         throw new InvalidToolBindException(item, "Already used for the wand");
      } else if (item == this.config.navigationWand) {
         throw new InvalidToolBindException(item, "Already used for the navigation wand");
      } else {
         this.tools.put(item, tool);
      }
   }

   public boolean isUsingInventory() {
      return this.useInventory;
   }

   public void setUseInventory(boolean useInventory) {
      this.useInventory = useInventory;
   }

   public String getLastScript() {
      return this.lastScript;
   }

   public void setLastScript(String lastScript) {
      this.lastScript = lastScript;
   }

   public void tellVersion(LocalPlayer player) {
      if (this.config.showFirstUseVersion && !this.beenToldVersion) {
         player.printRaw("§8WorldEdit ver. " + WorldEdit.getVersion() + " (http://sk89q.com/projects/worldedit/)");
         this.beenToldVersion = true;
      }

   }

   public void dispatchCUIEvent(LocalPlayer player, CUIEvent event) {
      if (this.hasCUISupport) {
         player.dispatchCUIEvent(event);
      }

   }

   public void dispatchCUISetup(LocalPlayer player) {
      if (this.selector != null) {
         this.dispatchCUISelection(player);
      }

   }

   public void dispatchCUISelection(LocalPlayer player) {
      if (this.hasCUISupport) {
         if (this.selector instanceof CUIRegion) {
            CUIRegion tempSel = (CUIRegion)this.selector;
            if (tempSel.getProtocolVersion() > this.cuiVersion) {
               player.dispatchCUIEvent(new SelectionShapeEvent(tempSel.getLegacyTypeID()));
               tempSel.describeLegacyCUI(this, player);
            } else {
               player.dispatchCUIEvent(new SelectionShapeEvent(tempSel.getTypeID()));
               tempSel.describeCUI(this, player);
            }
         }

      }
   }

   public void describeCUI(LocalPlayer player) {
      if (this.hasCUISupport) {
         if (this.selector instanceof CUIRegion) {
            CUIRegion tempSel = (CUIRegion)this.selector;
            if (tempSel.getProtocolVersion() > this.cuiVersion) {
               tempSel.describeLegacyCUI(this, player);
            } else {
               tempSel.describeCUI(this, player);
            }
         }

      }
   }

   public void handleCUIInitializationMessage(String text) {
      if (!this.hasCUISupport()) {
         String[] split = text.split("\\|");
         if (split.length > 1 && split[0].equalsIgnoreCase("v")) {
            this.setCUISupport(true);

            try {
               this.setCUIVersion(Integer.parseInt(split[1]));
            } catch (NumberFormatException e) {
               WorldEdit.logger.warning("Error while reading CUI init message: " + e.getMessage());
            }
         }

      }
   }

   public boolean hasCUISupport() {
      return this.hasCUISupport;
   }

   public void setCUISupport(boolean support) {
      this.hasCUISupport = support;
   }

   public int getCUIVersion() {
      return this.cuiVersion;
   }

   public void setCUIVersion(int CUIVersion) {
      this.cuiVersion = CUIVersion;
   }

   public Calendar detectDate(String input) {
      Time.setTimeZone(this.getTimeZone());
      Options opt = new Options();
      opt.setNow(Calendar.getInstance(this.getTimeZone()));
      Span date = Chronic.parse(input, opt);
      return date == null ? null : date.getBeginCalendar();
   }

   public void update() {
      this.expirationTime = System.currentTimeMillis();
   }

   public boolean hasExpired() {
      return System.currentTimeMillis() - this.expirationTime > (long)EXPIRATION_GRACE;
   }

   public EditSession createEditSession(LocalPlayer player) {
      BlockBag blockBag = this.getBlockBag(player);
      EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(player.isPlayer() ? player.getWorld() : null, this.getBlockChangeLimit(), blockBag, player);
      editSession.setFastMode(this.fastMode);
      if (this.mask != null) {
         this.mask.prepare(this, player, (Vector)null);
      }

      editSession.setMask(this.mask);
      return editSession;
   }

   public boolean hasFastMode() {
      return this.fastMode;
   }

   public void setFastMode(boolean fastMode) {
      this.fastMode = fastMode;
   }

   public Mask getMask() {
      return this.mask;
   }

   public void setMask(Mask mask) {
      this.mask = mask;
   }
}
