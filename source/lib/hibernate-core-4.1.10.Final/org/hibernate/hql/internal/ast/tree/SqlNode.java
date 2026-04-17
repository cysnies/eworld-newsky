package org.hibernate.hql.internal.ast.tree;

import org.hibernate.type.Type;

public class SqlNode extends Node {
   private String originalText;
   private Type dataType;

   public void setText(String s) {
      super.setText(s);
      if (s != null && s.length() > 0 && this.originalText == null) {
         this.originalText = s;
      }

   }

   public String getOriginalText() {
      return this.originalText;
   }

   public Type getDataType() {
      return this.dataType;
   }

   public void setDataType(Type dataType) {
      this.dataType = dataType;
   }
}
