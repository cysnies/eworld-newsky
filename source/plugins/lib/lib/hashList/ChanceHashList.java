package lib.hashList;

import java.io.Serializable;
import java.util.Set;

public interface ChanceHashList extends HashList, Iterable, Cloneable, Serializable {
   boolean add(Object var1);

   boolean add(Object var1, int var2);

   ChanceHashList clone();

   boolean addChance(Object var1, int var2);

   boolean addChance(Object var1, int var2, int var3);

   void setChance(int var1, int var2);

   boolean setChance(Object var1, int var2);

   int getChance(int var1);

   int getChance(Object var1);

   int getTotalChance();

   void updateTotalChance(int var1);

   Object getRandom();

   void convert(HashList var1, boolean var2);

   void convert(Set var1, boolean var2);
}
