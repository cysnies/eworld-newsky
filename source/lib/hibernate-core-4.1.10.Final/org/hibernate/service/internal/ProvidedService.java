package org.hibernate.service.internal;

public class ProvidedService {
   private final Class serviceRole;
   private final Object service;

   public ProvidedService(Class serviceRole, Object service) {
      this.serviceRole = serviceRole;
      this.service = service;
   }

   public Class getServiceRole() {
      return this.serviceRole;
   }

   public Object getService() {
      return this.service;
   }
}
