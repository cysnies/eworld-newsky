package org.hibernate.service.classloading.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;
import org.jboss.logging.Logger;

public class ClassLoaderServiceImpl implements ClassLoaderService {
   private static final Logger log = Logger.getLogger(ClassLoaderServiceImpl.class);
   private final ClassLoader aggregatedClassLoader;

   public ClassLoaderServiceImpl() {
      this(ClassLoaderServiceImpl.class.getClassLoader());
   }

   public ClassLoaderServiceImpl(ClassLoader classLoader) {
      this(Collections.singletonList(classLoader));
   }

   public ClassLoaderServiceImpl(List providedClassLoaders) {
      LinkedHashSet<ClassLoader> orderedClassLoaderSet = new LinkedHashSet();
      if (providedClassLoaders != null) {
         for(ClassLoader classLoader : providedClassLoaders) {
            if (classLoader != null) {
               orderedClassLoaderSet.add(classLoader);
            }
         }
      }

      orderedClassLoaderSet.add(ClassLoaderServiceImpl.class.getClassLoader());
      ClassLoader tccl = locateTCCL();
      if (tccl != null) {
         orderedClassLoaderSet.add(tccl);
      }

      ClassLoader sysClassLoader = locateSystemClassLoader();
      if (sysClassLoader != null) {
         orderedClassLoaderSet.add(sysClassLoader);
      }

      this.aggregatedClassLoader = new AggregatedClassLoader(orderedClassLoaderSet);
   }

   /** @deprecated */
   @Deprecated
   public static ClassLoaderServiceImpl fromConfigSettings(Map configVales) {
      List<ClassLoader> providedClassLoaders = new ArrayList();
      Collection<ClassLoader> classLoaders = (Collection)configVales.get("hibernate.classLoaders");
      if (classLoaders != null) {
         for(ClassLoader classLoader : classLoaders) {
            providedClassLoaders.add(classLoader);
         }
      }

      addIfSet(providedClassLoaders, "hibernate.classLoader.application", configVales);
      addIfSet(providedClassLoaders, "hibernate.classLoader.resources", configVales);
      addIfSet(providedClassLoaders, "hibernate.classLoader.hibernate", configVales);
      addIfSet(providedClassLoaders, "hibernate.classLoader.environment", configVales);
      return new ClassLoaderServiceImpl(providedClassLoaders);
   }

   private static void addIfSet(List providedClassLoaders, String name, Map configVales) {
      ClassLoader providedClassLoader = (ClassLoader)configVales.get(name);
      if (providedClassLoader != null) {
         providedClassLoaders.add(providedClassLoader);
      }

   }

   private static ClassLoader locateSystemClassLoader() {
      try {
         return ClassLoader.getSystemClassLoader();
      } catch (Exception var1) {
         return null;
      }
   }

   private static ClassLoader locateTCCL() {
      try {
         return Thread.currentThread().getContextClassLoader();
      } catch (Exception var1) {
         return null;
      }
   }

   public Class classForName(String className) {
      try {
         return Class.forName(className, true, this.aggregatedClassLoader);
      } catch (Exception e) {
         throw new ClassLoadingException("Unable to load class [" + className + "]", e);
      }
   }

   public URL locateResource(String name) {
      try {
         return new URL(name);
      } catch (Exception var4) {
         try {
            return this.aggregatedClassLoader.getResource(name);
         } catch (Exception var3) {
            return null;
         }
      }
   }

   public InputStream locateResourceStream(String name) {
      try {
         log.tracef("trying via [new URL(\"%s\")]", name);
         return (new URL(name)).openStream();
      } catch (Exception var7) {
         try {
            log.tracef("trying via [ClassLoader.getResourceAsStream(\"%s\")]", name);
            InputStream stream = this.aggregatedClassLoader.getResourceAsStream(name);
            if (stream != null) {
               return stream;
            }
         } catch (Exception var5) {
         }

         String stripped = name.startsWith("/") ? name.substring(1) : null;
         if (stripped != null) {
            try {
               log.tracef("trying via [new URL(\"%s\")]", stripped);
               return (new URL(stripped)).openStream();
            } catch (Exception var6) {
               try {
                  log.tracef("trying via [ClassLoader.getResourceAsStream(\"%s\")]", stripped);
                  InputStream stream = this.aggregatedClassLoader.getResourceAsStream(stripped);
                  if (stream != null) {
                     return stream;
                  }
               } catch (Exception var4) {
               }
            }
         }

         return null;
      }
   }

   public List locateResources(String name) {
      ArrayList<URL> urls = new ArrayList();

      try {
         Enumeration<URL> urlEnumeration = this.aggregatedClassLoader.getResources(name);
         if (urlEnumeration != null && urlEnumeration.hasMoreElements()) {
            while(urlEnumeration.hasMoreElements()) {
               urls.add(urlEnumeration.nextElement());
            }
         }
      } catch (Exception var4) {
      }

      return urls;
   }

   public LinkedHashSet loadJavaServices(Class serviceContract) {
      ServiceLoader<S> loader = ServiceLoader.load(serviceContract, this.aggregatedClassLoader);
      LinkedHashSet<S> services = new LinkedHashSet();

      for(Object service : loader) {
         services.add(service);
      }

      return services;
   }

   private static class AggregatedClassLoader extends ClassLoader {
      private final ClassLoader[] individualClassLoaders;

      private AggregatedClassLoader(LinkedHashSet orderedClassLoaderSet) {
         super((ClassLoader)null);
         this.individualClassLoaders = (ClassLoader[])orderedClassLoaderSet.toArray(new ClassLoader[orderedClassLoaderSet.size()]);
      }

      public Enumeration getResources(String name) throws IOException {
         final HashSet<URL> resourceUrls = new HashSet();

         for(ClassLoader classLoader : this.individualClassLoaders) {
            Enumeration<URL> urls = classLoader.getResources(name);

            while(urls.hasMoreElements()) {
               resourceUrls.add(urls.nextElement());
            }
         }

         return new Enumeration() {
            final Iterator resourceUrlIterator = resourceUrls.iterator();

            public boolean hasMoreElements() {
               return this.resourceUrlIterator.hasNext();
            }

            public URL nextElement() {
               return (URL)this.resourceUrlIterator.next();
            }
         };
      }

      protected URL findResource(String name) {
         for(ClassLoader classLoader : this.individualClassLoaders) {
            URL resource = classLoader.getResource(name);
            if (resource != null) {
               return resource;
            }
         }

         return super.findResource(name);
      }

      protected Class findClass(String name) throws ClassNotFoundException {
         for(ClassLoader classLoader : this.individualClassLoaders) {
            try {
               return classLoader.loadClass(name);
            } catch (Exception var7) {
            }
         }

         throw new ClassNotFoundException("Could not load requested class : " + name);
      }
   }
}
