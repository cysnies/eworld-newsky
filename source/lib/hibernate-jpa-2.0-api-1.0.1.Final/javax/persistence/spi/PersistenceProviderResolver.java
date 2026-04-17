package javax.persistence.spi;

import java.util.List;

public interface PersistenceProviderResolver {
   List getPersistenceProviders();

   void clearCachedProviders();
}
