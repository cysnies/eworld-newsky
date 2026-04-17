package javax.persistence.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.PersistenceException;

public class PersistenceProviderResolverHolder {
   private static final PersistenceProviderResolver DEFAULT_RESOLVER = new PersistenceProviderResolverPerClassLoader();
   private static volatile PersistenceProviderResolver RESOLVER;

   public static PersistenceProviderResolver getPersistenceProviderResolver() {
      return RESOLVER == null ? DEFAULT_RESOLVER : RESOLVER;
   }

   public static void setPersistenceProviderResolver(PersistenceProviderResolver resolver) {
      RESOLVER = resolver;
   }

   private static class PersistenceProviderResolverPerClassLoader implements PersistenceProviderResolver {
      private final WeakHashMap resolvers;
      private volatile short barrier;

      private PersistenceProviderResolverPerClassLoader() {
         this.resolvers = new WeakHashMap();
         this.barrier = 1;
      }

      public List getPersistenceProviders() {
         ClassLoader cl = getContextualClassLoader();
         if (this.barrier == 1) {
         }

         PersistenceProviderResolver currentResolver = (PersistenceProviderResolver)this.resolvers.get(cl);
         if (currentResolver == null) {
            currentResolver = new CachingPersistenceProviderResolver(cl);
            this.resolvers.put(cl, currentResolver);
            this.barrier = 1;
         }

         return currentResolver.getPersistenceProviders();
      }

      public void clearCachedProviders() {
         ClassLoader cl = getContextualClassLoader();
         if (this.barrier == 1) {
         }

         PersistenceProviderResolver currentResolver = (PersistenceProviderResolver)this.resolvers.get(cl);
         if (currentResolver != null) {
            currentResolver.clearCachedProviders();
         }

      }

      private static ClassLoader getContextualClassLoader() {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         if (cl == null) {
            cl = PersistenceProviderResolverPerClassLoader.class.getClassLoader();
         }

         return cl;
      }

      private static class CachingPersistenceProviderResolver implements PersistenceProviderResolver {
         private final List resolverClasses = new ArrayList();
         private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");

         public CachingPersistenceProviderResolver(ClassLoader cl) {
            this.loadResolverClasses(cl);
         }

         private void loadResolverClasses(ClassLoader cl) {
            synchronized(this.resolverClasses) {
               try {
                  Enumeration<URL> resources = cl.getResources("META-INF/services/" + PersistenceProvider.class.getName());
                  Set<String> names = new HashSet();

                  while(resources.hasMoreElements()) {
                     URL url = (URL)resources.nextElement();
                     InputStream is = url.openStream();

                     try {
                        names.addAll(providerNamesFromReader(new BufferedReader(new InputStreamReader(is))));
                     } finally {
                        is.close();
                     }
                  }

                  for(String s : names) {
                     Class<? extends PersistenceProvider> providerClass = cl.loadClass(s);
                     WeakReference<Class<? extends PersistenceProvider>> reference = new WeakReference(providerClass);
                     if (s.endsWith("HibernatePersistence") && this.resolverClasses.size() > 0) {
                        WeakReference<Class<? extends PersistenceProvider>> movedReference = (WeakReference)this.resolverClasses.get(0);
                        this.resolverClasses.add(0, reference);
                        this.resolverClasses.add(movedReference);
                     } else {
                        this.resolverClasses.add(reference);
                     }
                  }
               } catch (IOException e) {
                  throw new PersistenceException(e);
               } catch (ClassNotFoundException e) {
                  throw new PersistenceException(e);
               }

            }
         }

         public List getPersistenceProviders() {
            synchronized(this.resolverClasses) {
               List<PersistenceProvider> providers = new ArrayList(this.resolverClasses.size());

               try {
                  for(WeakReference providerClass : this.resolverClasses) {
                     providers.add(((Class)providerClass.get()).newInstance());
                  }
               } catch (InstantiationException e) {
                  throw new PersistenceException(e);
               } catch (IllegalAccessException e) {
                  throw new PersistenceException(e);
               }

               return providers;
            }
         }

         public synchronized void clearCachedProviders() {
            synchronized(this.resolverClasses) {
               this.resolverClasses.clear();
               this.loadResolverClasses(PersistenceProviderResolverHolder.PersistenceProviderResolverPerClassLoader.getContextualClassLoader());
            }
         }

         private static Set providerNamesFromReader(BufferedReader reader) throws IOException {
            Set<String> names = new HashSet();

            String line;
            while((line = reader.readLine()) != null) {
               line = line.trim();
               Matcher m = nonCommentPattern.matcher(line);
               if (m.find()) {
                  names.add(m.group().trim());
               }
            }

            return names;
         }
      }
   }
}
