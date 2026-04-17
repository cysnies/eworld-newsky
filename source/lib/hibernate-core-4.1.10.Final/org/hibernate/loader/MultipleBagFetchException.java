package org.hibernate.loader;

import java.util.List;
import org.hibernate.HibernateException;

public class MultipleBagFetchException extends HibernateException {
   private final List bagRoles;

   public MultipleBagFetchException(List bagRoles) {
      super("cannot simultaneously fetch multiple bags");
      this.bagRoles = bagRoles;
   }

   public List getBagRoles() {
      return this.bagRoles;
   }
}
