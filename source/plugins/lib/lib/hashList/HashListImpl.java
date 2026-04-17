package lib.hashList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HashListImpl implements HashList {
   private static final long serialVersionUID = 1L;
   protected HashMap hash = new HashMap();
   protected List list = new ArrayList();

   public boolean add(Object o) {
      if (this.hash.containsKey(o)) {
         return false;
      } else {
         try {
            this.hash.put(o, 0);
            this.list.add(o);
         } catch (Exception var3) {
            this.remove(o);
         }

         return true;
      }
   }

   public boolean add(Object o, int index) {
      if (this.hash.containsKey(o)) {
         return false;
      } else {
         try {
            this.list.add(index, o);
            this.hash.put(o, 0);
         } catch (Exception var4) {
            this.remove(o);
         }

         return true;
      }
   }

   public Object get(int index) {
      return this.list.get(index);
   }

   public boolean remove(Object o) {
      if (!this.hash.containsKey(o)) {
         return false;
      } else {
         this.hash.remove(o);
         this.list.remove(this.list.indexOf(o));
         return true;
      }
   }

   public Object remove(int index) {
      T o = (T)this.list.get(index);
      this.hash.remove(o);
      this.list.remove(index);
      return o;
   }

   public void clear() {
      this.hash.clear();
      this.list.clear();
   }

   public int indexOf(Object o) {
      return this.list.indexOf(o);
   }

   public boolean has(Object o) {
      return this.hash.containsKey(o);
   }

   public int size() {
      return this.list.size();
   }

   public boolean isEmpty() {
      return this.list.size() == 0;
   }

   public List getPage(int page, int pageSize) {
      List<T> result = new ArrayList();
      int maxPage = this.getMaxPage(pageSize);
      if (page >= 1 && page <= maxPage) {
         int begin = (page - 1) * pageSize;
         int end = page == maxPage ? this.list.size() : page * pageSize;

         for(int i = begin; i < end; ++i) {
            result.add(this.list.get(i));
         }
      }

      return result;
   }

   public int getMaxPage(int pageSize) {
      if (pageSize < 0) {
         throw new IllegalArgumentException();
      } else {
         return this.list.size() % pageSize == 0 ? this.list.size() / pageSize : this.list.size() / pageSize + 1;
      }
   }

   public Iterator iterator() {
      return this.list.iterator();
   }

   public HashList clone() {
      HashListImpl<T> hash = new HashListImpl();
      hash.hash = this.hash;
      hash.list = this.list;
      return hash;
   }

   public String toString() {
      String result = "";

      for(int i = 0; i < this.list.size(); ++i) {
         if (i != 0) {
            result = result + ",";
         }

         result = result + this.list.get(i);
      }

      return result;
   }
}
