package org.hibernate;

public enum FetchMode {
   DEFAULT,
   JOIN,
   SELECT;

   /** @deprecated */
   public static final FetchMode LAZY = SELECT;
   /** @deprecated */
   public static final FetchMode EAGER = JOIN;
}
