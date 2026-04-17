package org.hibernate.service.classloading.spi;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import org.hibernate.service.Service;

public interface ClassLoaderService extends Service {
   Class classForName(String var1);

   URL locateResource(String var1);

   InputStream locateResourceStream(String var1);

   List locateResources(String var1);

   LinkedHashSet loadJavaServices(Class var1);
}
