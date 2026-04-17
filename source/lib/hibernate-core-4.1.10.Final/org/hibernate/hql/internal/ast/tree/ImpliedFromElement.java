package org.hibernate.hql.internal.ast.tree;

public class ImpliedFromElement extends FromElement {
   private boolean impliedInFromClause = false;
   private boolean inProjectionList = false;

   public boolean isImplied() {
      return true;
   }

   public void setImpliedInFromClause(boolean flag) {
      this.impliedInFromClause = flag;
   }

   public boolean isImpliedInFromClause() {
      return this.impliedInFromClause;
   }

   public void setInProjectionList(boolean inProjectionList) {
      this.inProjectionList = inProjectionList;
   }

   public boolean inProjectionList() {
      return this.inProjectionList && this.isFromOrJoinFragment();
   }

   public boolean isIncludeSubclasses() {
      return false;
   }

   public String getDisplayText() {
      StringBuilder buf = new StringBuilder();
      buf.append("ImpliedFromElement{");
      this.appendDisplayText(buf);
      buf.append("}");
      return buf.toString();
   }
}
