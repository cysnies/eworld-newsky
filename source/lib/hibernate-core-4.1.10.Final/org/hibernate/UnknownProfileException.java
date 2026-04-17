package org.hibernate;

public class UnknownProfileException extends HibernateException {
   private final String name;

   public UnknownProfileException(String name) {
      super("Unknow fetch profile [" + name + "]");
      this.name = name;
   }

   public String getName() {
      return this.name;
   }
}
