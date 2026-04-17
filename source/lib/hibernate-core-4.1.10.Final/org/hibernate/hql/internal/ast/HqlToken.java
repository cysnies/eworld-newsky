package org.hibernate.hql.internal.ast;

import antlr.CommonToken;

public class HqlToken extends CommonToken {
   private boolean possibleID = false;
   private int tokenType;

   public boolean isPossibleID() {
      return this.possibleID;
   }

   public void setType(int t) {
      this.tokenType = this.getType();
      super.setType(t);
   }

   private int getPreviousType() {
      return this.tokenType;
   }

   public void setPossibleID(boolean possibleID) {
      this.possibleID = possibleID;
   }

   public String toString() {
      return "[\"" + this.getText() + "\",<" + this.getType() + "> previously: <" + this.getPreviousType() + ">,line=" + this.line + ",col=" + this.col + ",possibleID=" + this.possibleID + "]";
   }
}
