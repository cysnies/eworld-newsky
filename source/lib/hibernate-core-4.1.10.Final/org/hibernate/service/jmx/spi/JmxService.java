package org.hibernate.service.jmx.spi;

import javax.management.ObjectName;
import org.hibernate.service.Service;
import org.hibernate.service.spi.Manageable;

public interface JmxService extends Service {
   void registerService(Manageable var1, Class var2);

   void registerMBean(ObjectName var1, Object var2);
}
