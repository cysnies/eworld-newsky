package org.hibernate.service;

import org.hibernate.HibernateException;

public class UnknownServiceException extends HibernateException {
   public final Class serviceRole;

   public UnknownServiceException(Class serviceRole) {
      super("Unknown service requested [" + serviceRole.getName() + "]");
      this.serviceRole = serviceRole;
   }

   public Class getServiceRole() {
      return this.serviceRole;
   }
}
