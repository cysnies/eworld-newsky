package org.hibernate.sql;

public class DisjunctionFragment {
   private StringBuilder buffer = new StringBuilder();

   public DisjunctionFragment addCondition(ConditionFragment fragment) {
      if (this.buffer.length() > 0) {
         this.buffer.append(" or ");
      }

      this.buffer.append("(").append(fragment.toFragmentString()).append(")");
      return this;
   }

   public String toFragmentString() {
      return this.buffer.toString();
   }
}
