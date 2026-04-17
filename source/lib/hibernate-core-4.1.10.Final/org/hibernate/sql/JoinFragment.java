package org.hibernate.sql;

import org.hibernate.internal.util.StringHelper;

public abstract class JoinFragment {
   /** @deprecated */
   @Deprecated
   public static final int INNER_JOIN = 0;
   /** @deprecated */
   @Deprecated
   public static final int FULL_JOIN = 4;
   /** @deprecated */
   @Deprecated
   public static final int LEFT_OUTER_JOIN = 1;
   /** @deprecated */
   @Deprecated
   public static final int RIGHT_OUTER_JOIN = 2;
   private boolean hasFilterCondition = false;
   private boolean hasThetaJoins = false;

   public abstract void addJoin(String var1, String var2, String[] var3, String[] var4, JoinType var5);

   public abstract void addJoin(String var1, String var2, String[] var3, String[] var4, JoinType var5, String var6);

   public abstract void addCrossJoin(String var1, String var2);

   public abstract void addJoins(String var1, String var2);

   public abstract String toFromFragmentString();

   public abstract String toWhereFragmentString();

   public abstract void addCondition(String var1, String[] var2, String[] var3);

   public abstract boolean addCondition(String var1);

   public abstract JoinFragment copy();

   public void addFragment(JoinFragment ojf) {
      if (ojf.hasThetaJoins()) {
         this.hasThetaJoins = true;
      }

      this.addJoins(ojf.toFromFragmentString(), ojf.toWhereFragmentString());
   }

   protected boolean addCondition(StringBuilder buffer, String on) {
      if (StringHelper.isNotEmpty(on)) {
         if (!on.startsWith(" and")) {
            buffer.append(" and ");
         }

         buffer.append(on);
         return true;
      } else {
         return false;
      }
   }

   public boolean hasFilterCondition() {
      return this.hasFilterCondition;
   }

   public void setHasFilterCondition(boolean b) {
      this.hasFilterCondition = b;
   }

   public boolean hasThetaJoins() {
      return this.hasThetaJoins;
   }

   public void setHasThetaJoins(boolean hasThetaJoins) {
      this.hasThetaJoins = hasThetaJoins;
   }
}
