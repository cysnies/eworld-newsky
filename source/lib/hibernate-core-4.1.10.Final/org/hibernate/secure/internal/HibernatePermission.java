package org.hibernate.secure.internal;

import java.security.Permission;

public class HibernatePermission extends Permission {
   public static final String INSERT = "insert";
   public static final String UPDATE = "update";
   public static final String DELETE = "delete";
   public static final String READ = "read";
   public static final String ANY = "*";
   private final String actions;

   public HibernatePermission(String entityName, String actions) {
      super(entityName);
      this.actions = actions;
   }

   public boolean implies(Permission permission) {
      return ("*".equals(this.getName()) || this.getName().equals(permission.getName())) && ("*".equals(this.actions) || this.actions.indexOf(permission.getActions()) >= 0);
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof HibernatePermission)) {
         return false;
      } else {
         HibernatePermission permission = (HibernatePermission)obj;
         return permission.getName().equals(this.getName()) && permission.getActions().equals(this.actions);
      }
   }

   public int hashCode() {
      return this.getName().hashCode() * 37 + this.actions.hashCode();
   }

   public String getActions() {
      return this.actions;
   }

   public String toString() {
      return "HibernatePermission(" + this.getName() + ':' + this.actions + ')';
   }
}
