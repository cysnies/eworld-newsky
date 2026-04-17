package lib.hashList;

import java.io.Serializable;
import java.util.List;

public interface HashList extends Iterable, Cloneable, Serializable {
   boolean add(Object var1);

   boolean add(Object var1, int var2);

   Object get(int var1);

   boolean remove(Object var1);

   Object remove(int var1);

   void clear();

   int indexOf(Object var1);

   boolean has(Object var1);

   int size();

   boolean isEmpty();

   List getPage(int var1, int var2);

   int getMaxPage(int var1);

   HashList clone();
}
