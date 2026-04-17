package javax.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.spi.LoadState;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;

public class Persistence {
   /** @deprecated */
   @Deprecated
   public static final String PERSISTENCE_PROVIDER = "javax.persistence.spi.PeristenceProvider";
   /** @deprecated */
   @Deprecated
   protected static final Set providers = new HashSet();
   private static PersistenceUtil util = new PersistenceUtil() {
      public boolean isLoaded(Object entity, String attributeName) {
         List<PersistenceProvider> providers = Persistence.getProviders();

         for(PersistenceProvider provider : providers) {
            LoadState state = provider.getProviderUtil().isLoadedWithoutReference(entity, attributeName);
            if (state != LoadState.UNKNOWN) {
               return state == LoadState.LOADED;
            }
         }

         for(PersistenceProvider provider : providers) {
            LoadState state = provider.getProviderUtil().isLoadedWithReference(entity, attributeName);
            if (state != LoadState.UNKNOWN) {
               return state == LoadState.LOADED;
            }
         }

         return true;
      }

      public boolean isLoaded(Object object) {
         for(PersistenceProvider provider : Persistence.getProviders()) {
            LoadState state = provider.getProviderUtil().isLoaded(object);
            if (state != LoadState.UNKNOWN) {
               return state == LoadState.LOADED;
            }
         }

         return true;
      }
   };

   public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName) {
      return createEntityManagerFactory(persistenceUnitName, (Map)null);
   }

   public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
      EntityManagerFactory emf = null;

      for(PersistenceProvider provider : getProviders()) {
         emf = provider.createEntityManagerFactory(persistenceUnitName, properties);
         if (emf != null) {
            break;
         }
      }

      if (emf == null) {
         throw new PersistenceException("No Persistence provider for EntityManager named " + persistenceUnitName);
      } else {
         return emf;
      }
   }

   private static List getProviders() {
      return PersistenceProviderResolverHolder.getPersistenceProviderResolver().getPersistenceProviders();
   }

   public static PersistenceUtil getPersistenceUtil() {
      return util;
   }
}
