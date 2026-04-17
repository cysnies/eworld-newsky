package org.hibernate.service.spi;

public interface Manageable {
   String getManagementDomain();

   String getManagementServiceType();

   Object getManagementBean();
}
