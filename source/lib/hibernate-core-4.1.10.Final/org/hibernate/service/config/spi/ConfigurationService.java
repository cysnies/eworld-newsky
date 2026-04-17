package org.hibernate.service.config.spi;

import java.util.Map;
import org.hibernate.service.Service;

public interface ConfigurationService extends Service {
   Map getSettings();

   Object getSetting(String var1, Converter var2);

   Object getSetting(String var1, Converter var2, Object var3);

   Object getSetting(String var1, Class var2, Object var3);

   Object cast(Class var1, Object var2);

   public interface Converter {
      Object convert(Object var1);
   }
}
