package fr.neatmonster.nocheatplus.utilities.ds;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class CoordMap {
   private static final int p1 = 73856093;
   private static final int p2 = 19349663;
   private static final int p3 = 83492791;
   private final float loadFactor;
   private List[] entries;
   private int size;

   private static final int getHash(int x, int y, int z) {
      return 73856093 * x ^ 19349663 * y ^ 83492791 * z;
   }

   public CoordMap() {
      this(10, 0.75F);
   }

   public CoordMap(int initialCapacity) {
      this(initialCapacity, 0.75F);
   }

   public CoordMap(int initialCapacity, float loadFactor) {
      this.size = 0;
      this.loadFactor = loadFactor;
      this.entries = new List[initialCapacity];
   }

   public final boolean contains(int x, int y, int z) {
      return this.get(x, y, z) != null;
   }

   public final Object get(int x, int y, int z) {
      int hash = getHash(x, y, z);
      int slot = Math.abs(hash) % this.entries.length;
      List<Entry<V>> bucket = this.entries[slot];
      if (bucket == null) {
         return null;
      } else {
         for(Entry entry : bucket) {
            if (hash == entry.hash && x == entry.x && z == entry.z && y == entry.y) {
               return entry.value;
            }
         }

         return null;
      }
   }

   public final boolean put(int x, int y, int z, Object value) {
      int hash = getHash(x, y, z);
      int absHash = Math.abs(hash);
      int slot = absHash % this.entries.length;
      List<Entry<V>> bucket = this.entries[slot];
      if (bucket != null) {
         for(Entry entry : bucket) {
            if (hash == entry.hash && x == entry.x && z == entry.z && y == entry.y) {
               entry.value = value;
               return true;
            }
         }
      } else if ((float)(this.size + 1) > (float)this.entries.length * this.loadFactor) {
         this.resize(this.size + 1);
         slot = absHash % this.entries.length;
         bucket = this.entries[slot];
      }

      if (bucket == null) {
         bucket = new LinkedList();
         this.entries[slot] = bucket;
      }

      bucket.add(new Entry(x, y, z, value, hash));
      ++this.size;
      return false;
   }

   public final Object remove(int x, int y, int z) {
      int hash = getHash(x, y, z);
      int absHash = Math.abs(hash);
      int slot = absHash % this.entries.length;
      List<Entry<V>> bucket = this.entries[slot];
      if (bucket == null) {
         return null;
      } else {
         for(int i = 0; i < bucket.size(); ++i) {
            Entry<V> entry = (Entry)bucket.get(i);
            if (entry.hash == hash && x == entry.x && z == entry.z && y == entry.y) {
               bucket.remove(entry);
               if (bucket.isEmpty()) {
                  this.entries[slot] = null;
               }

               --this.size;
               return entry.value;
            }
         }

         return null;
      }
   }

   private final void resize(int size) {
      int newCapacity = Math.min(Math.max((int)((float)(size + 4) / this.loadFactor), this.entries.length + this.entries.length / 4), 4);
      List<Entry<V>>[] newEntries = new List[newCapacity];
      int used = -1;

      for(int oldSlot = 0; oldSlot < this.entries.length; ++oldSlot) {
         List<Entry<V>> oldBucket = this.entries[oldSlot];
         if (oldBucket != null) {
            for(Entry entry : oldBucket) {
               int newSlot = Math.abs(entry.hash) % newCapacity;
               List<Entry<V>> newBucket = newEntries[newSlot];
               if (newBucket == null) {
                  if (used < 0) {
                     newBucket = new LinkedList();
                  } else {
                     newBucket = this.entries[used];
                     this.entries[used] = null;
                     --used;
                  }

                  newEntries[newSlot] = newBucket;
               }

               newBucket.add(entry);
            }

            oldBucket.clear();
            this.entries[oldSlot] = null;
            ++used;
            this.entries[used] = oldBucket;
         }
      }

      this.entries = newEntries;
   }

   public final int size() {
      return this.size;
   }

   public void clear() {
      this.size = 0;
      Arrays.fill(this.entries, (Object)null);
   }

   public final Iterator iterator() {
      return new CoordMapIterator(this);
   }

   public static final class Entry {
      protected final int x;
      protected final int y;
      protected final int z;
      protected Object value;
      protected final int hash;

      public Entry(int x, int y, int z, Object value, int hash) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.value = value;
         this.hash = hash;
      }

      public final int getX() {
         return this.x;
      }

      public final int getY() {
         return this.y;
      }

      public final int getZ() {
         return this.z;
      }

      public final Object getValue() {
         return this.value;
      }
   }

   public static final class CoordMapIterator implements Iterator {
      private final CoordMap map;
      private final List[] entries;
      private int slot = 0;
      private int index = 0;
      private int slotLast = -1;
      private int indexLast = -1;

      protected CoordMapIterator(CoordMap map) {
         this.map = map;
         this.entries = map.entries;
      }

      public final boolean hasNext() {
         while(this.slot < this.entries.length) {
            List<Entry<V>> bucket = this.entries[this.slot];
            if (bucket == null) {
               ++this.slot;
               this.index = 0;
            } else {
               if (this.index < bucket.size()) {
                  return true;
               }

               ++this.slot;
               this.index = 0;
            }
         }

         return false;
      }

      public final Entry next() {
         while(this.slot < this.entries.length) {
            List<Entry<V>> bucket = this.entries[this.slot];
            if (bucket == null) {
               ++this.slot;
               this.index = 0;
            } else {
               int size = bucket.size();
               if (this.index < size) {
                  Entry<V> res = (Entry)bucket.get(this.index);
                  this.slotLast = this.slot;
                  this.indexLast = this.index++;
                  if (this.index == size) {
                     this.index = 0;
                     ++this.slot;
                  }

                  return res;
               }

               ++this.slot;
               this.index = 0;
            }
         }

         throw new NoSuchElementException();
      }

      public final void remove() {
         if (this.slotLast != -1) {
            List<Entry<V>> bucket = this.entries[this.slotLast];
            bucket.remove(this.indexLast);
            if (bucket.isEmpty()) {
               this.entries[this.slotLast] = null;
            } else if (this.slotLast == this.slot) {
               --this.index;
            }

            this.map.size--;
            this.slotLast = this.indexLast = -1;
         }
      }
   }
}
