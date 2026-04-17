package org.hibernate;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LockOptions implements Serializable {
   public static final LockOptions NONE;
   public static final LockOptions READ;
   public static final LockOptions UPGRADE;
   public static final int NO_WAIT = 0;
   public static final int WAIT_FOREVER = -1;
   private LockMode lockMode;
   private int timeout;
   private Map aliasSpecificLockModes;
   private boolean scope;

   public LockOptions() {
      this.lockMode = LockMode.NONE;
      this.timeout = -1;
      this.aliasSpecificLockModes = null;
      this.scope = false;
   }

   public LockOptions(LockMode lockMode) {
      this.lockMode = LockMode.NONE;
      this.timeout = -1;
      this.aliasSpecificLockModes = null;
      this.scope = false;
      this.lockMode = lockMode;
   }

   public LockMode getLockMode() {
      return this.lockMode;
   }

   public LockOptions setLockMode(LockMode lockMode) {
      this.lockMode = lockMode;
      return this;
   }

   public LockOptions setAliasSpecificLockMode(String alias, LockMode lockMode) {
      if (this.aliasSpecificLockModes == null) {
         this.aliasSpecificLockModes = new HashMap();
      }

      this.aliasSpecificLockModes.put(alias, lockMode);
      return this;
   }

   public LockMode getAliasSpecificLockMode(String alias) {
      return this.aliasSpecificLockModes == null ? null : (LockMode)this.aliasSpecificLockModes.get(alias);
   }

   public LockMode getEffectiveLockMode(String alias) {
      LockMode lockMode = this.getAliasSpecificLockMode(alias);
      if (lockMode == null) {
         lockMode = this.lockMode;
      }

      return lockMode == null ? LockMode.NONE : lockMode;
   }

   public boolean hasAliasSpecificLockModes() {
      return this.aliasSpecificLockModes != null && !this.aliasSpecificLockModes.isEmpty();
   }

   public int getAliasLockCount() {
      return this.aliasSpecificLockModes == null ? 0 : this.aliasSpecificLockModes.size();
   }

   public Iterator getAliasLockIterator() {
      return this.aliasSpecificLockModes == null ? Collections.emptyList().iterator() : this.aliasSpecificLockModes.entrySet().iterator();
   }

   public LockMode findGreatestLockMode() {
      LockMode lockModeToUse = this.getLockMode();
      if (lockModeToUse == null) {
         lockModeToUse = LockMode.NONE;
      }

      if (this.aliasSpecificLockModes == null) {
         return lockModeToUse;
      } else {
         for(LockMode lockMode : this.aliasSpecificLockModes.values()) {
            if (lockMode.greaterThan(lockModeToUse)) {
               lockModeToUse = lockMode;
            }
         }

         return lockModeToUse;
      }
   }

   public int getTimeOut() {
      return this.timeout;
   }

   public LockOptions setTimeOut(int timeout) {
      this.timeout = timeout;
      return this;
   }

   public boolean getScope() {
      return this.scope;
   }

   public LockOptions setScope(boolean scope) {
      this.scope = scope;
      return this;
   }

   public static LockOptions copy(LockOptions source, LockOptions destination) {
      destination.setLockMode(source.getLockMode());
      destination.setScope(source.getScope());
      destination.setTimeOut(source.getTimeOut());
      if (source.aliasSpecificLockModes != null) {
         destination.aliasSpecificLockModes = new HashMap(source.aliasSpecificLockModes);
      }

      return destination;
   }

   public LockOptions makeCopy() {
      LockOptions copy = new LockOptions();
      copy(this, copy);
      return copy;
   }

   static {
      NONE = new LockOptions(LockMode.NONE);
      READ = new LockOptions(LockMode.READ);
      UPGRADE = new LockOptions(LockMode.UPGRADE);
   }
}
