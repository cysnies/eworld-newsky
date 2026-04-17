package org.hibernate.tool.hbm2ddl;

public enum Target {
   EXPORT,
   SCRIPT,
   NONE,
   BOTH;

   public boolean doExport() {
      return this == BOTH || this == EXPORT;
   }

   public boolean doScript() {
      return this == BOTH || this == SCRIPT;
   }

   public static Target interpret(boolean script, boolean export) {
      if (script) {
         return export ? BOTH : SCRIPT;
      } else {
         return export ? EXPORT : NONE;
      }
   }
}
