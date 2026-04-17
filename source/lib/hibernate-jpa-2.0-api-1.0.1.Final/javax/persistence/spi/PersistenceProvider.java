package javax.persistence.spi;

import java.util.Map;
import javax.persistence.EntityManagerFactory;

public interface PersistenceProvider {
   EntityManagerFactory createEntityManagerFactory(String var1, Map var2);

   EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo var1, Map var2);

   ProviderUtil getProviderUtil();
}
