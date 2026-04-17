package com.earth2me.essentials.storage;

public interface IStorageObjectHolder {
   StorageObject getData();

   void acquireReadLock();

   void acquireWriteLock();

   void close();

   void unlock();
}
