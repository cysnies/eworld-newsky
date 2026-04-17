package javax.persistence.spi;

public interface ProviderUtil {
   LoadState isLoadedWithoutReference(Object var1, String var2);

   LoadState isLoadedWithReference(Object var1, String var2);

   LoadState isLoaded(Object var1);
}
