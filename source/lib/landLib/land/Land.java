package land;

import event.LandCreateEvent;
import java.io.Serializable;
import java.util.HashMap;
import landHandler.AdminHandler;
import landMain.Dao;
import landMain.LandManager;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Land implements Serializable {
   private static final long serialVersionUID = 1L;
   private static final int NAME_MAX_LENGTH = 30;
   private static final String SYSTEM = "@系统@";
   private static final String ALL = "@所有玩家@";
   private static LandManager LandManager;
   private static Dao Dao;
   private long id;
   private boolean fix;
   private int type;
   private boolean overlap;
   private int price;
   private String name;
   private String owner;
   private Range range;
   private int level;
   private HashMap flags;
   private HashMap pers;

   public Land() {
   }

   private Land(int type, boolean overlap, String name, String owner, Range range, int level) {
      this.type = type;
      this.overlap = overlap;
      this.name = name;
      this.owner = owner;
      this.range = range;
      this.level = level;
      this.flags = new HashMap();
      this.pers = new HashMap();
      this.price = -1;
   }

   public static void init(LandManager landManager) {
      LandManager = landManager;
      Dao = landManager.getLandMain().getDao();
   }

   public static Land createLand(int type, boolean overlap, String name, String owner, Range range, int level) {
      name = name.trim();
      if (owner == null) {
         owner = "@系统@";
      }

      owner = owner.trim();
      level = LandManager.getFixedLevel(level, range, (Land)null);
      Land land = new Land(type, overlap, name, owner, range, level);
      LandManager.addLand(land);
      LandCreateEvent createEvent = new LandCreateEvent(land);
      LandManager.getServer().getPluginManager().callEvent(createEvent);
      return land;
   }

   public static Land createLand(int type, boolean overlap, String owner, Range range, int level) {
      String name = LandManager.getUnusedName();
      return createLand(type, overlap, name, owner, range, level);
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public boolean isOverlap() {
      return this.overlap;
   }

   public void setOverlap(boolean overlap) {
      this.overlap = overlap;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getOwner() {
      return this.owner;
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   public Range getRange() {
      return this.range.clone();
   }

   public void setRange(Range range) {
      this.range = range;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public HashMap getFlags() {
      return this.flags;
   }

   public void setFlags(HashMap flags) {
      this.flags = flags;
   }

   public boolean addFlag(String flag, int value) {
      if (this.flags.containsKey(flag)) {
         if ((Integer)this.flags.get(flag) != value) {
            this.flags.put(flag, value);
            Dao.addLand(this);
         }

         return false;
      } else {
         this.flags.put(flag, value);
         Dao.addLand(this);
         return true;
      }
   }

   public boolean setFlag(String flag, int value) {
      if (!this.flags.containsKey(flag)) {
         return false;
      } else {
         if ((Integer)this.flags.get(flag) != value) {
            this.flags.put(flag, value);
            Dao.addLand(this);
         }

         return true;
      }
   }

   public boolean hasFlag(String flag) {
      return this.flags.containsKey(flag);
   }

   public int getFlag(String flag) {
      return (Integer)this.flags.get(flag);
   }

   public boolean removeFlag(String flag) {
      this.pers.remove(flag);
      if (!this.flags.containsKey(flag)) {
         return false;
      } else {
         this.flags.remove(flag);
         Dao.addLand(this);
         return true;
      }
   }

   public boolean hasPer(String flag, String name) {
      String adminFlag = AdminHandler.getFlagAdmin();
      if (!flag.equals(adminFlag) && this.hasPer(adminFlag, name)) {
         return true;
      } else if (this.owner.equals(name)) {
         return true;
      } else if (!this.pers.containsKey(flag) || !((HashList)this.pers.get(flag)).has(name) && !((HashList)this.pers.get(flag)).has("@所有玩家@")) {
         Player tarP = Bukkit.getServer().getPlayerExact(name);
         return tarP != null && tarP.isOp();
      } else {
         return true;
      }
   }

   public boolean addPer(String flag, String name) {
      if (!this.pers.containsKey(flag)) {
         this.pers.put(flag, new HashListImpl());
      }

      return ((HashList)this.pers.get(flag)).add(name);
   }

   public boolean removePer(String flag, String name) {
      try {
         return ((HashList)this.pers.get(flag)).remove(name);
      } catch (Exception var4) {
         return false;
      }
   }

   public boolean removeAllPer(String flag) {
      if (this.pers.containsKey(flag)) {
         this.pers.remove(flag);
         return true;
      } else {
         return false;
      }
   }

   public long getSize() {
      return this.range.getSize();
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public HashMap getPers() {
      return this.pers;
   }

   public void setPers(HashMap pers) {
      this.pers = pers;
   }

   public boolean isFix() {
      return this.fix;
   }

   public void setFix(boolean fix) {
      this.fix = fix;
   }

   public int getPrice() {
      return this.price;
   }

   public void setPrice(int price) {
      this.price = price;
   }

   public int hashCode() {
      return (int)this.id;
   }

   public boolean equals(Object obj) {
      Land land = (Land)obj;
      return land.getId() == this.id;
   }

   public static String getSystem() {
      return "@系统@";
   }

   public static int getNameMaxLength() {
      return 30;
   }
}
