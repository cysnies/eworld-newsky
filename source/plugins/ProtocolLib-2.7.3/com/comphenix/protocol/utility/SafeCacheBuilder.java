package com.comphenix.protocol.utility;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class SafeCacheBuilder {
   private CacheBuilder builder = CacheBuilder.newBuilder();
   private static Method BUILD_METHOD;
   private static Method AS_MAP_METHOD;

   private SafeCacheBuilder() {
   }

   public static SafeCacheBuilder newBuilder() {
      return new SafeCacheBuilder();
   }

   public SafeCacheBuilder concurrencyLevel(int concurrencyLevel) {
      this.builder.concurrencyLevel(concurrencyLevel);
      return this;
   }

   public SafeCacheBuilder expireAfterAccess(long duration, TimeUnit unit) {
      this.builder.expireAfterAccess(duration, unit);
      return this;
   }

   public SafeCacheBuilder expireAfterWrite(long duration, TimeUnit unit) {
      this.builder.expireAfterWrite(duration, unit);
      return this;
   }

   public SafeCacheBuilder initialCapacity(int initialCapacity) {
      this.builder.initialCapacity(initialCapacity);
      return this;
   }

   public SafeCacheBuilder maximumSize(int size) {
      this.builder.maximumSize(size);
      return this;
   }

   public SafeCacheBuilder removalListener(RemovalListener listener) {
      this.builder.removalListener(listener);
      return this;
   }

   public SafeCacheBuilder ticker(Ticker ticker) {
      this.builder.ticker(ticker);
      return this;
   }

   public SafeCacheBuilder softValues() {
      this.builder.softValues();
      return this;
   }

   public SafeCacheBuilder weakKeys() {
      this.builder.weakKeys();
      return this;
   }

   public SafeCacheBuilder weakValues() {
      this.builder.weakValues();
      return this;
   }

   public ConcurrentMap build(CacheLoader loader) {
      Object cache = null;
      if (BUILD_METHOD == null) {
         try {
            BUILD_METHOD = this.builder.getClass().getDeclaredMethod("build", CacheLoader.class);
            BUILD_METHOD.setAccessible(true);
         } catch (Exception e) {
            throw new FieldAccessException("Unable to find CacheBuilder.build(CacheLoader)", e);
         }
      }

      try {
         cache = BUILD_METHOD.invoke(this.builder, loader);
      } catch (Exception e) {
         throw new FieldAccessException("Unable to invoke " + BUILD_METHOD + " on " + this.builder, e);
      }

      if (AS_MAP_METHOD == null) {
         try {
            AS_MAP_METHOD = cache.getClass().getMethod("asMap");
            AS_MAP_METHOD.setAccessible(true);
         } catch (Exception e) {
            throw new FieldAccessException("Unable to find Cache.asMap() in " + cache, e);
         }
      }

      try {
         return (ConcurrentMap)AS_MAP_METHOD.invoke(cache);
      } catch (Exception e) {
         throw new FieldAccessException("Unable to invoke " + AS_MAP_METHOD + " on " + cache, e);
      }
   }
}
