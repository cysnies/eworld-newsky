package landCheck;

import event.LandCreateEvent;
import event.LandRemoveEvent;
import event.NameChangeEvent;
import event.OwnerChangeEvent;
import event.RangeChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import land.Land;
import land.Pos;
import land.Range;
import landMain.LandManager;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilFormat;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class LandCheck implements Listener {
   private static final String LAND_NAME = "领地";
   static final Integer[] levels = new Integer[]{0, 50000, 10000, 9000, 8000, 7000, 6000, 5000, 4000, 3000, 2000, 1000, 800, 700, 600, 500, 400, 300, 200, 100, 70, 60, 30, 15};
   private List levelList = new ArrayList();
   private LandManager landManager;
   private Server server;
   private String pn;
   private HashMap landHash;
   private HashMap idHash;
   private HashMap nameHash;
   private HashMap ownerHash;
   private HashList allLandList;
   private static final int MAX_BUFFER_SIZE = 1000;
   private static final int DROP_PER = 100;
   private HashMap bufferHash;
   private int bufferSize;

   public LandCheck(LandManager landManager) {
      Integer[] var5;
      int var4 = (var5 = levels).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         int i = var5[var3];
         this.levelList.add(i);
      }

      this.bufferHash = new HashMap();
      this.landManager = landManager;
      this.server = landManager.getServer();
      this.pn = landManager.getLandMain().getPn();
      this.landHash = new HashMap();

      for(World w : this.server.getWorlds()) {
         this.landHash.put(w.getName(), new ArrayList());

         for(int level = 1; level <= this.levelList.size(); ++level) {
            ((List)this.landHash.get(w.getName())).add(new HashMap());
         }
      }

      landManager.registerEvents(this);
      this.loadData();
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onLandCreate(LandCreateEvent e) {
      this.resetBuffer();
      Land land = e.getLand();
      this.add(land.getRange(), land.getId());
      this.idHash.put(land.getId(), land);
      this.nameHash.put(land.getName(), land);
      if (!this.ownerHash.containsKey(land.getOwner())) {
         this.ownerHash.put(land.getOwner(), new HashListImpl());
      }

      ((HashList)this.ownerHash.get(land.getOwner())).add(land);
      this.allLandList.add(land);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onLandRemove(LandRemoveEvent e) {
      this.resetBuffer();
      Land land = e.getRemovedLand();
      this.remove(land.getRange(), land.getId());
      this.idHash.remove(land.getId());
      this.nameHash.remove(land.getName());
      this.allLandList.remove(land);
      if (this.ownerHash.get(land.getOwner()) != null) {
         ((HashList)this.ownerHash.get(land.getOwner())).remove(land);
         if (((HashList)this.ownerHash.get(land.getOwner())).size() == 0) {
            this.ownerHash.remove(land.getOwner());
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onNameChange(NameChangeEvent e) {
      String oldName = e.getOldName();
      this.nameHash.remove(oldName);
      this.nameHash.put(e.getLand().getName(), e.getLand());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onOwnerChange(OwnerChangeEvent e) {
      String oldOwner = e.getOldOwner();
      ((HashList)this.ownerHash.get(oldOwner)).remove(e.getLand());
      if (((HashList)this.ownerHash.get(oldOwner)).size() == 0) {
         this.ownerHash.remove(oldOwner);
      }

      if (!this.ownerHash.containsKey(e.getLand().getOwner())) {
         this.ownerHash.put(e.getLand().getOwner(), new HashListImpl());
      }

      ((HashList)this.ownerHash.get(e.getLand().getOwner())).add(e.getLand());
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = false
   )
   public void onRangeChange(RangeChangeEvent e) {
      this.resetBuffer();
      Land land = e.getLand();
      this.remove(e.getOldRange(), land.getId());
      this.add(land.getRange(), land.getId());
   }

   public String getFriendPer(boolean friendPer) {
      return friendPer ? this.get(1005) : this.get(1010);
   }

   public String getType(int type) {
      return this.get(1000 + type);
   }

   public String getUnusedName() {
      for(int i = 1; i < Integer.MAX_VALUE; ++i) {
         if (!this.nameHash.containsKey("领地" + i)) {
            return "领地" + i;
         }
      }

      return null;
   }

   public Land getLand(long landId) {
      return (Land)this.idHash.get(landId);
   }

   public Land getLand(String name) {
      return (Land)this.nameHash.get(name);
   }

   public HashList getLands(Location l) {
      return this.getLands(Pos.getPos(l));
   }

   public Land getHighestPriorityLand(Location l) {
      Land result = null;

      for(Land land : this.getLands(l)) {
         if (result == null) {
            result = land;
         } else if (land.getLevel() >= result.getLevel()) {
            result = land;
         }
      }

      return result;
   }

   public HashList getAllLands() {
      return this.allLandList;
   }

   public HashList getUserLands(String name) {
      return (HashList)this.ownerHash.get(name);
   }

   public HashList getSystemLands() {
      return (HashList)this.ownerHash.get(Land.getSystem());
   }

   public HashList getLands(Pos pos) {
      HashList<Land> result = (HashList)this.bufferHash.get(pos);
      String world = pos.getWorld();
      if (result != null) {
         return result;
      } else {
         result = new HashListImpl();
         int x = pos.getX();
         int y = pos.getY();
         int z = pos.getZ();

         for(int level = 0; level < ((List)this.landHash.get(world)).size(); ++level) {
            int size = (Integer)this.levelList.get(level);
            int xLevel;
            int yLevel;
            int zLevel;
            if (level != 0) {
               xLevel = this.d(x, size);
               yLevel = this.d(y, size);
               zLevel = this.d(z, size);
            } else {
               xLevel = 0;
               yLevel = 0;
               zLevel = 0;
            }

            HashMap<Integer, HashMap<Integer, List<Long>>> yHash;
            HashMap<Integer, List<Long>> zHash;
            List<Long> listTemp;
            if (this.landHash.get(world) != null && ((List)this.landHash.get(world)).get(level) != null && (yHash = (HashMap)((HashMap)((List)this.landHash.get(world)).get(level)).get(xLevel)) != null && (zHash = (HashMap)yHash.get(yLevel)) != null && (listTemp = (List)zHash.get(zLevel)) != null) {
               for(long id : listTemp) {
                  if (((Land)this.idHash.get(id)).getRange().checkPos(pos)) {
                     result.add((Land)this.idHash.get(id));
                  }
               }
            }
         }

         this.bufferHash.put(pos, result);
         ++this.bufferSize;
         if (this.bufferSize > 1000) {
            this.bufferSize -= 100;
            int count = 0;
            Iterator<Pos> it = this.bufferHash.keySet().iterator();

            while(it.hasNext()) {
               it.next();
               it.remove();
               ++count;
               if (count >= 100) {
                  break;
               }
            }
         }

         return result;
      }
   }

   public int getFixedLevel(int level, Range range, Land ignoreLand) {
      HashList<Land> landHash = this.getCollisionLands(range);

      boolean check;
      do {
         check = true;

         for(Land land : landHash) {
            if (land.getLevel() == level && (ignoreLand == null || !land.equals(ignoreLand))) {
               check = false;
               --level;
               break;
            }
         }
      } while(!check);

      return level;
   }

   public HashList getCollisionLands(Range range) {
      HashList<Land> result = new HashListImpl();

      for(Land land : this.nameHash.values()) {
         if (this.checkCollision(range, land.getRange())) {
            result.add(land);
         }
      }

      return result;
   }

   public boolean checkCollision(Range range1, Range range2) {
      if (!range1.getP1().getWorld().equals(range2.getP1().getWorld())) {
         return false;
      } else {
         return range1.getXLength() + range2.getXLength() > Math.abs(range1.getP2().getX() + range1.getP1().getX() - range2.getP2().getX() - range2.getP1().getX()) && range1.getYLength() + range2.getYLength() > Math.abs(range1.getP2().getY() + range1.getP1().getY() - range2.getP2().getY() - range2.getP1().getY()) && range1.getZLength() + range2.getZLength() > Math.abs(range1.getP2().getZ() + range1.getP1().getZ() - range2.getP2().getZ() - range2.getP1().getZ());
      }
   }

   private void loadData() {
      List<Land> landList = this.landManager.getAllLandsFromDB();
      this.idHash = new HashMap();
      this.nameHash = new HashMap();
      this.ownerHash = new HashMap();
      this.allLandList = new HashListImpl();
      Iterator<Land> it = landList.iterator();

      while(it.hasNext()) {
         Land land = (Land)it.next();
         if (this.server.getWorld(land.getRange().getP1().getWorld()) == null) {
            this.landManager.getLandMain().getDao().remove(land);
            it.remove();
         } else {
            this.idHash.put(land.getId(), land);
            this.nameHash.put(land.getName(), land);
            if (!this.ownerHash.containsKey(land.getOwner())) {
               this.ownerHash.put(land.getOwner(), new HashListImpl());
            }

            ((HashList)this.ownerHash.get(land.getOwner())).add(land);
            this.allLandList.add(land);
            this.addToHash(land);
         }
      }

   }

   private void addToHash(Land land) {
      int saveLevel = this.getSaveLevel(land.getRange());
      int size = (Integer)this.levelList.get(saveLevel);
      Pos pos = land.getRange().getP1();
      if (size == 0) {
         this.addToHash(pos.getWorld(), land.getId(), 0, 0, 0, 0);
      } else {
         int x = this.d(pos.getX(), size);
         int y = this.d(pos.getY(), size);
         int z = this.d(pos.getZ(), size);
         this.addToHash(pos.getWorld(), land.getId(), saveLevel, x, y, z);
      }

   }

   private void addToHash(String world, long id, int saveLevel, int x, int y, int z) {
      HashMap<Integer, HashMap<Integer, HashMap<Integer, List<Long>>>> xHash = (HashMap)((List)this.landHash.get(world)).get(saveLevel);
      HashMap<Integer, HashMap<Integer, List<Long>>> yHash = (HashMap)xHash.get(x);
      if (yHash == null) {
         yHash = new HashMap();
         xHash.put(x, yHash);
      }

      HashMap<Integer, List<Long>> zHash = (HashMap)yHash.get(y);
      if (zHash == null) {
         zHash = new HashMap();
         yHash.put(y, zHash);
      }

      List<Long> list2 = (List)zHash.get(z);
      if (list2 == null) {
         list2 = new ArrayList();
         zHash.put(z, list2);
      }

      list2.add(id);
   }

   private int getSaveLevel(Range range) {
      for(int level = this.levelList.size() - 1; level > 0; --level) {
         int size = (Integer)this.levelList.get(level);
         if (this.d(range.getP1().getX(), size) == this.d(range.getP2().getX(), size) && this.d(range.getP1().getY(), size) == this.d(range.getP2().getY(), size) && this.d(range.getP1().getZ(), size) == this.d(range.getP2().getZ(), size)) {
            return level;
         }
      }

      return 0;
   }

   private int d(int n, int size) {
      if (size == 0) {
         return 0;
      } else {
         return n >= 0 ? n / size : n / size - 1;
      }
   }

   private void add(Range range, long id) {
      String world = range.getP1().getWorld();
      int saveLevel = this.getSaveLevel(range);
      int size = (Integer)this.levelList.get(saveLevel);
      int x = this.d(range.getP1().getX(), size);
      int y = this.d(range.getP1().getY(), size);
      int z = this.d(range.getP1().getZ(), size);
      HashMap<Integer, HashMap<Integer, HashMap<Integer, List<Long>>>> xHash = (HashMap)((List)this.landHash.get(world)).get(saveLevel);
      HashMap<Integer, HashMap<Integer, List<Long>>> yHash;
      if ((yHash = (HashMap)xHash.get(x)) == null) {
         yHash = new HashMap();
         xHash.put(x, yHash);
      }

      HashMap<Integer, List<Long>> zHash;
      if ((zHash = (HashMap)yHash.get(y)) == null) {
         zHash = new HashMap();
         yHash.put(y, zHash);
      }

      List<Long> listTemp;
      if ((listTemp = (List)zHash.get(z)) == null) {
         listTemp = new LinkedList();
         zHash.put(z, listTemp);
      }

      listTemp.add(id);
   }

   private void remove(Range range, long id) {
      String world = range.getP1().getWorld();
      int saveLevel = this.getSaveLevel(range);
      int size = (Integer)this.levelList.get(saveLevel);
      int x = this.d(range.getP1().getX(), size);
      int y = this.d(range.getP1().getY(), size);
      int z = this.d(range.getP1().getZ(), size);
      List<Long> listTemp = (List)((HashMap)((HashMap)((HashMap)((List)this.landHash.get(world)).get(saveLevel)).get(x)).get(y)).get(z);
      listTemp.remove(listTemp.indexOf(id));
      if (listTemp.isEmpty()) {
         ((HashMap)((HashMap)((HashMap)((List)this.landHash.get(world)).get(saveLevel)).get(x)).get(y)).remove(z);
      }

      if (((HashMap)((HashMap)((HashMap)((List)this.landHash.get(world)).get(saveLevel)).get(x)).get(y)).isEmpty()) {
         ((HashMap)((HashMap)((List)this.landHash.get(world)).get(saveLevel)).get(x)).remove(y);
      }

      if (((HashMap)((HashMap)((List)this.landHash.get(world)).get(saveLevel)).get(x)).isEmpty()) {
         ((HashMap)((List)this.landHash.get(world)).get(saveLevel)).remove(x);
      }

   }

   private void resetBuffer() {
      this.bufferSize = 0;
      this.bufferHash.clear();
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
