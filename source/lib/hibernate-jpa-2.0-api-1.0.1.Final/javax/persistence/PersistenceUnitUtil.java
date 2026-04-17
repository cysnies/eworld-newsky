package javax.persistence;

public interface PersistenceUnitUtil extends PersistenceUtil {
   boolean isLoaded(Object var1, String var2);

   boolean isLoaded(Object var1);

   Object getIdentifier(Object var1);
}
