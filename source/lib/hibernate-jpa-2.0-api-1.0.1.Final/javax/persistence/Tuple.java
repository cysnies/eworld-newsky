package javax.persistence;

import java.util.List;

public interface Tuple {
   Object get(TupleElement var1);

   Object get(String var1, Class var2);

   Object get(String var1);

   Object get(int var1, Class var2);

   Object get(int var1);

   Object[] toArray();

   List getElements();
}
