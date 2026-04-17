package com.earth2me.essentials.storage;

import com.earth2me.essentials.IConf;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.api.IReload;
import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public abstract class AsyncStorageObjectHolder implements IConf, IStorageObjectHolder, IReload {
   private transient StorageObject data;
   private final transient ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
   private final transient Class clazz;
   protected final transient IEssentials ess;

   public AsyncStorageObjectHolder(IEssentials ess, Class clazz) {
      this.ess = ess;
      this.clazz = clazz;

      try {
         this.data = (StorageObject)clazz.newInstance();
      } catch (Exception ex) {
         Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
      }

   }

   public StorageObject getData() {
      return this.data;
   }

   public void acquireReadLock() {
      this.rwl.readLock().lock();
   }

   public void acquireWriteLock() {
      while(this.rwl.getReadHoldCount() > 0) {
         this.rwl.readLock().unlock();
      }

      this.rwl.writeLock().lock();
      this.rwl.readLock().lock();
   }

   public void close() {
      this.unlock();
   }

   public void unlock() {
      if (this.rwl.isWriteLockedByCurrentThread()) {
         this.rwl.writeLock().unlock();
         new StorageObjectDataWriter();
      }

      while(this.rwl.getReadHoldCount() > 0) {
         this.rwl.readLock().unlock();
      }

   }

   public void reloadConfig() {
      new StorageObjectDataReader();
   }

   public void onReload() {
      new StorageObjectDataReader();
   }

   public abstract void finishRead();

   public abstract void finishWrite();

   public abstract File getStorageFile();

   private class StorageObjectDataWriter extends AbstractDelayedYamlFileWriter {
      StorageObjectDataWriter() {
         super(AsyncStorageObjectHolder.this.ess, AsyncStorageObjectHolder.this.getStorageFile());
      }

      public StorageObject getObject() {
         AsyncStorageObjectHolder.this.acquireReadLock();
         return AsyncStorageObjectHolder.this.getData();
      }

      public void onFinish() {
         AsyncStorageObjectHolder.this.unlock();
         AsyncStorageObjectHolder.this.finishWrite();
      }
   }

   private class StorageObjectDataReader extends AbstractDelayedYamlFileReader {
      StorageObjectDataReader() {
         super(AsyncStorageObjectHolder.this.ess, AsyncStorageObjectHolder.this.getStorageFile(), AsyncStorageObjectHolder.this.clazz);
      }

      public void onStart() {
         AsyncStorageObjectHolder.this.rwl.writeLock().lock();
      }

      public void onSuccess(StorageObject object) {
         if (object != null) {
            AsyncStorageObjectHolder.this.data = object;
         }

         AsyncStorageObjectHolder.this.rwl.writeLock().unlock();
         AsyncStorageObjectHolder.this.finishRead();
      }

      public void onException() {
         if (AsyncStorageObjectHolder.this.data == null) {
            try {
               AsyncStorageObjectHolder.this.data = (StorageObject)AsyncStorageObjectHolder.this.clazz.newInstance();
            } catch (Exception ex) {
               Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
         }

         AsyncStorageObjectHolder.this.rwl.writeLock().unlock();
      }
   }
}
