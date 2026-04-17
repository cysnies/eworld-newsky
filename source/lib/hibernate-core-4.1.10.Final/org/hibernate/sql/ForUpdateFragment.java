package org.hibernate.sql;

import java.util.Iterator;
import java.util.Map;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.QueryException;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;

public class ForUpdateFragment {
   private final StringBuilder aliases;
   private boolean isNowaitEnabled;
   private final Dialect dialect;
   private LockMode lockMode;
   private LockOptions lockOptions;

   public ForUpdateFragment(Dialect dialect) {
      this.aliases = new StringBuilder();
      this.dialect = dialect;
   }

   public ForUpdateFragment(Dialect dialect, LockOptions lockOptions, Map keyColumnNames) throws QueryException {
      this(dialect);
      LockMode upgradeType = null;
      Iterator iter = lockOptions.getAliasLockIterator();
      this.lockOptions = lockOptions;
      if (!iter.hasNext()) {
         LockMode lockMode = lockOptions.getLockMode();
         if (LockMode.READ.lessThan(lockMode)) {
            upgradeType = lockMode;
            this.lockMode = lockMode;
         }
      }

      while(iter.hasNext()) {
         Map.Entry me = (Map.Entry)iter.next();
         LockMode lockMode = (LockMode)me.getValue();
         if (LockMode.READ.lessThan(lockMode)) {
            String tableAlias = (String)me.getKey();
            if (dialect.forUpdateOfColumns()) {
               String[] keyColumns = (String[])keyColumnNames.get(tableAlias);
               if (keyColumns == null) {
                  throw new IllegalArgumentException("alias not found: " + tableAlias);
               }

               keyColumns = StringHelper.qualify(tableAlias, keyColumns);

               for(int i = 0; i < keyColumns.length; ++i) {
                  this.addTableAlias(keyColumns[i]);
               }
            } else {
               this.addTableAlias(tableAlias);
            }

            if (upgradeType != null && lockMode != upgradeType) {
               throw new QueryException("mixed LockModes");
            }

            upgradeType = lockMode;
         }
      }

      if (upgradeType == LockMode.UPGRADE_NOWAIT) {
         this.setNowaitEnabled(true);
      }

   }

   public ForUpdateFragment addTableAlias(String alias) {
      if (this.aliases.length() > 0) {
         this.aliases.append(", ");
      }

      this.aliases.append(alias);
      return this;
   }

   public String toFragmentString() {
      if (this.lockOptions != null) {
         return this.dialect.getForUpdateString(this.aliases.toString(), this.lockOptions);
      } else if (this.aliases.length() == 0) {
         return this.lockMode != null ? this.dialect.getForUpdateString(this.lockMode) : "";
      } else {
         return this.isNowaitEnabled ? this.dialect.getForUpdateNowaitString(this.aliases.toString()) : this.dialect.getForUpdateString(this.aliases.toString());
      }
   }

   public ForUpdateFragment setNowaitEnabled(boolean nowait) {
      this.isNowaitEnabled = nowait;
      return this;
   }
}
