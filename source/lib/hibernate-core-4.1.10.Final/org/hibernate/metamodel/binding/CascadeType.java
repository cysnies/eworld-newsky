package org.hibernate.metamodel.binding;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.CascadeStyle;

public enum CascadeType {
   ALL,
   ALL_DELETE_ORPHAN,
   UPDATE,
   PERSIST,
   MERGE,
   LOCK,
   REFRESH,
   REPLICATE,
   EVICT,
   DELETE,
   DELETE_ORPHAN,
   NONE;

   private static final Map hbmOptionToCascadeType = new HashMap();
   private static final Map jpaCascadeTypeToHibernateCascadeType;
   private static final Map cascadeTypeToCascadeStyle;

   public static CascadeType getCascadeType(String hbmOptionName) {
      return (CascadeType)hbmOptionToCascadeType.get(hbmOptionName);
   }

   public static CascadeType getCascadeType(javax.persistence.CascadeType jpaCascade) {
      return (CascadeType)jpaCascadeTypeToHibernateCascadeType.get(jpaCascade);
   }

   public CascadeStyle toCascadeStyle() {
      CascadeStyle cascadeStyle = (CascadeStyle)cascadeTypeToCascadeStyle.get(this);
      if (cascadeStyle == null) {
         throw new MappingException("No CascadeStyle that corresponds with CascadeType=" + this.name());
      } else {
         return cascadeStyle;
      }
   }

   static {
      hbmOptionToCascadeType.put("all", ALL);
      hbmOptionToCascadeType.put("all-delete-orphan", ALL_DELETE_ORPHAN);
      hbmOptionToCascadeType.put("save-update", UPDATE);
      hbmOptionToCascadeType.put("persist", PERSIST);
      hbmOptionToCascadeType.put("merge", MERGE);
      hbmOptionToCascadeType.put("lock", LOCK);
      hbmOptionToCascadeType.put("refresh", REFRESH);
      hbmOptionToCascadeType.put("replicate", REPLICATE);
      hbmOptionToCascadeType.put("evict", EVICT);
      hbmOptionToCascadeType.put("delete", DELETE);
      hbmOptionToCascadeType.put("remove", DELETE);
      hbmOptionToCascadeType.put("delete-orphan", DELETE_ORPHAN);
      hbmOptionToCascadeType.put("none", NONE);
      jpaCascadeTypeToHibernateCascadeType = new HashMap();
      jpaCascadeTypeToHibernateCascadeType.put(javax.persistence.CascadeType.ALL, ALL);
      jpaCascadeTypeToHibernateCascadeType.put(javax.persistence.CascadeType.PERSIST, PERSIST);
      jpaCascadeTypeToHibernateCascadeType.put(javax.persistence.CascadeType.MERGE, MERGE);
      jpaCascadeTypeToHibernateCascadeType.put(javax.persistence.CascadeType.REFRESH, REFRESH);
      jpaCascadeTypeToHibernateCascadeType.put(javax.persistence.CascadeType.DETACH, EVICT);
      cascadeTypeToCascadeStyle = new HashMap();
      cascadeTypeToCascadeStyle.put(ALL, CascadeStyle.ALL);
      cascadeTypeToCascadeStyle.put(ALL_DELETE_ORPHAN, CascadeStyle.ALL_DELETE_ORPHAN);
      cascadeTypeToCascadeStyle.put(UPDATE, CascadeStyle.UPDATE);
      cascadeTypeToCascadeStyle.put(PERSIST, CascadeStyle.PERSIST);
      cascadeTypeToCascadeStyle.put(MERGE, CascadeStyle.MERGE);
      cascadeTypeToCascadeStyle.put(LOCK, CascadeStyle.LOCK);
      cascadeTypeToCascadeStyle.put(REFRESH, CascadeStyle.REFRESH);
      cascadeTypeToCascadeStyle.put(REPLICATE, CascadeStyle.REPLICATE);
      cascadeTypeToCascadeStyle.put(EVICT, CascadeStyle.EVICT);
      cascadeTypeToCascadeStyle.put(DELETE, CascadeStyle.DELETE);
      cascadeTypeToCascadeStyle.put(DELETE_ORPHAN, CascadeStyle.DELETE_ORPHAN);
      cascadeTypeToCascadeStyle.put(NONE, CascadeStyle.NONE);
   }
}
