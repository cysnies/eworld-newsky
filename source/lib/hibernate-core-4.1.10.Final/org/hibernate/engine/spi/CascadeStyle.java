package org.hibernate.engine.spi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.internal.util.collections.ArrayHelper;

public abstract class CascadeStyle implements Serializable {
   public static final CascadeStyle ALL_DELETE_ORPHAN = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return true;
      }

      public boolean hasOrphanDelete() {
         return true;
      }

      public String toString() {
         return "STYLE_ALL_DELETE_ORPHAN";
      }
   };
   public static final CascadeStyle ALL = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return true;
      }

      public String toString() {
         return "STYLE_ALL";
      }
   };
   public static final CascadeStyle UPDATE = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return action == CascadingAction.SAVE_UPDATE;
      }

      public String toString() {
         return "STYLE_SAVE_UPDATE";
      }
   };
   public static final CascadeStyle LOCK = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return action == CascadingAction.LOCK;
      }

      public String toString() {
         return "STYLE_LOCK";
      }
   };
   public static final CascadeStyle REFRESH = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return action == CascadingAction.REFRESH;
      }

      public String toString() {
         return "STYLE_REFRESH";
      }
   };
   public static final CascadeStyle EVICT = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return action == CascadingAction.EVICT;
      }

      public String toString() {
         return "STYLE_EVICT";
      }
   };
   public static final CascadeStyle REPLICATE = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return action == CascadingAction.REPLICATE;
      }

      public String toString() {
         return "STYLE_REPLICATE";
      }
   };
   public static final CascadeStyle MERGE = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return action == CascadingAction.MERGE;
      }

      public String toString() {
         return "STYLE_MERGE";
      }
   };
   public static final CascadeStyle PERSIST = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return action == CascadingAction.PERSIST || action == CascadingAction.PERSIST_ON_FLUSH;
      }

      public String toString() {
         return "STYLE_PERSIST";
      }
   };
   public static final CascadeStyle DELETE = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return action == CascadingAction.DELETE;
      }

      public String toString() {
         return "STYLE_DELETE";
      }
   };
   public static final CascadeStyle DELETE_ORPHAN = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return action == CascadingAction.DELETE || action == CascadingAction.SAVE_UPDATE;
      }

      public boolean reallyDoCascade(CascadingAction action) {
         return action == CascadingAction.DELETE;
      }

      public boolean hasOrphanDelete() {
         return true;
      }

      public String toString() {
         return "STYLE_DELETE_ORPHAN";
      }
   };
   public static final CascadeStyle NONE = new CascadeStyle() {
      public boolean doCascade(CascadingAction action) {
         return false;
      }

      public String toString() {
         return "STYLE_NONE";
      }
   };
   static final Map STYLES = new HashMap();

   public abstract boolean doCascade(CascadingAction var1);

   public boolean reallyDoCascade(CascadingAction action) {
      return this.doCascade(action);
   }

   public boolean hasOrphanDelete() {
      return false;
   }

   public static CascadeStyle getCascadeStyle(String cascade) {
      CascadeStyle style = (CascadeStyle)STYLES.get(cascade);
      if (style == null) {
         throw new MappingException("Unsupported cascade style: " + cascade);
      } else {
         return style;
      }
   }

   static {
      STYLES.put("all", ALL);
      STYLES.put("all-delete-orphan", ALL_DELETE_ORPHAN);
      STYLES.put("save-update", UPDATE);
      STYLES.put("persist", PERSIST);
      STYLES.put("merge", MERGE);
      STYLES.put("lock", LOCK);
      STYLES.put("refresh", REFRESH);
      STYLES.put("replicate", REPLICATE);
      STYLES.put("evict", EVICT);
      STYLES.put("delete", DELETE);
      STYLES.put("remove", DELETE);
      STYLES.put("delete-orphan", DELETE_ORPHAN);
      STYLES.put("none", NONE);
   }

   public static final class MultipleCascadeStyle extends CascadeStyle {
      private final CascadeStyle[] styles;

      public MultipleCascadeStyle(CascadeStyle[] styles) {
         this.styles = styles;
      }

      public boolean doCascade(CascadingAction action) {
         for(CascadeStyle style : this.styles) {
            if (style.doCascade(action)) {
               return true;
            }
         }

         return false;
      }

      public boolean reallyDoCascade(CascadingAction action) {
         for(CascadeStyle style : this.styles) {
            if (style.reallyDoCascade(action)) {
               return true;
            }
         }

         return false;
      }

      public boolean hasOrphanDelete() {
         for(CascadeStyle style : this.styles) {
            if (style.hasOrphanDelete()) {
               return true;
            }
         }

         return false;
      }

      public String toString() {
         return ArrayHelper.toString(this.styles);
      }
   }
}
