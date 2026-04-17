package lib.hashList;

import java.util.Random;
import java.util.Set;

public class ChanceHashListImpl extends HashListImpl implements ChanceHashList {
   private static final long serialVersionUID = 1L;
   private static final Random RANDOM = new Random();
   private int totalChance;

   public boolean remove(Object o) {
      if (o == null) {
         throw new NullPointerException();
      } else if (!this.hash.containsKey(o)) {
         return false;
      } else {
         this.totalChance -= (Integer)this.hash.get(o);
         this.hash.remove(o);
         this.list.remove(o);
         return true;
      }
   }

   public Object remove(int index) {
      T o = (T)this.list.get(index);
      this.totalChance -= (Integer)this.hash.get(o);
      this.hash.remove(o);
      this.list.remove(o);
      return o;
   }

   public void clear() {
      super.clear();
      this.totalChance = 0;
   }

   public ChanceHashList clone() {
      ChanceHashListImpl<T> hash = new ChanceHashListImpl();
      hash.hash = this.hash;
      hash.list = this.list;
      hash.totalChance = this.totalChance;
      return hash;
   }

   public boolean add(Object o) {
      return this.addChance(o, 1);
   }

   public boolean add(Object o, int index) {
      return this.addChance(o, index, 1);
   }

   public boolean addChance(Object o, int chance) {
      if (super.add(o)) {
         this.hash.put(o, chance);
         this.totalChance += chance;
         return true;
      } else {
         return false;
      }
   }

   public boolean addChance(Object o, int index, int chance) {
      if (super.add(o, index)) {
         this.hash.put(o, chance);
         this.totalChance += chance;
         return true;
      } else {
         return false;
      }
   }

   public void setChance(int index, int chance) {
      T o = (T)this.list.get(index);
      this.totalChance += chance - (Integer)this.hash.get(o);
      this.hash.put(o, chance);
   }

   public boolean setChance(Object o, int chance) {
      if (this.hash.containsKey(o)) {
         this.totalChance += chance - (Integer)this.hash.get(o);
         this.hash.put(o, chance);
         return true;
      } else {
         return false;
      }
   }

   public int getChance(int index) {
      return (Integer)this.hash.get(this.list.get(index));
   }

   public int getChance(Object o) {
      return this.hash.containsKey(o) ? (Integer)this.hash.get(o) : -1;
   }

   public int getTotalChance() {
      return this.totalChance;
   }

   public void updateTotalChance(int totalChance) {
      double multi = (double)(totalChance / this.totalChance);
      this.totalChance = totalChance;
      int count = 0;

      for(Object t : this.hash.keySet()) {
         int chance = (int)((double)(Integer)this.hash.get(t) * multi);
         count += chance;
         this.hash.put(t, chance);
      }

      int left = totalChance - count;
      if (left > 0) {
         T t = (T)this.hash.keySet().iterator().next();
         this.hash.put(t, (Integer)this.hash.get(t) + left);
      }

   }

   public Object getRandom() {
      int select = RANDOM.nextInt(this.totalChance);

      for(int i = 0; i < this.list.size(); ++i) {
         select -= (Integer)this.hash.get(this.list.get(i));
         if (select < 0) {
            return this.list.get(i);
         }
      }

      return this.list.get(this.list.size() - 1);
   }

   public void convert(HashList hashList, boolean clear) {
      if (clear) {
         this.clear();
      }

      if (hashList instanceof ChanceHashListImpl) {
         ChanceHashList<T> chanceHashList = (ChanceHashList)hashList;

         for(Object t : chanceHashList) {
            this.addChance(t, chanceHashList.getChance(t));
         }
      } else {
         for(Object t : hashList) {
            this.add(t);
         }
      }

   }

   public void convert(Set set, boolean clear) {
      if (clear) {
         this.clear();
      }

      for(Object t : set) {
         this.add(t);
      }

   }
}
