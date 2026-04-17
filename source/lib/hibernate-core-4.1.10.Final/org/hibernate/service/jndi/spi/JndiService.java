package org.hibernate.service.jndi.spi;

import javax.naming.event.NamespaceChangeListener;
import org.hibernate.service.Service;

public interface JndiService extends Service {
   Object locate(String var1);

   void bind(String var1, Object var2);

   void unbind(String var1);

   void addListener(String var1, NamespaceChangeListener var2);
}
