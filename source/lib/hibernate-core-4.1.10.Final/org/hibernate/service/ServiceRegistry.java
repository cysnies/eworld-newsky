package org.hibernate.service;

public interface ServiceRegistry {
   ServiceRegistry getParentServiceRegistry();

   Service getService(Class var1);
}
