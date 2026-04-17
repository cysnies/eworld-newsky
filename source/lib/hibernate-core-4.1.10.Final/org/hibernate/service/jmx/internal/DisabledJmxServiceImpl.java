package org.hibernate.service.jmx.internal;

import javax.management.ObjectName;
import org.hibernate.service.jmx.spi.JmxService;
import org.hibernate.service.spi.Manageable;

public class DisabledJmxServiceImpl implements JmxService {
   public static final DisabledJmxServiceImpl INSTANCE = new DisabledJmxServiceImpl();

   public void registerService(Manageable service, Class serviceRole) {
   }

   public void registerMBean(ObjectName objectName, Object mBean) {
   }
}
