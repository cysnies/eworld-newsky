package javax.persistence;

public interface Cache {
   boolean contains(Class var1, Object var2);

   void evict(Class var1, Object var2);

   void evict(Class var1);

   void evictAll();
}
