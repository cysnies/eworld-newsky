package com.earth2me.essentials.storage;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlStorageReader implements IStorageReader {
   private static final transient Map PREPARED_YAMLS = Collections.synchronizedMap(new HashMap());
   private static final transient Map LOCKS = new HashMap();
   private final transient Reader reader;
   private final transient Plugin plugin;

   public YamlStorageReader(Reader reader, Plugin plugin) {
      this.reader = reader;
      this.plugin = plugin;
   }

   public StorageObject load(Class clazz) throws ObjectLoadException {
      Yaml yaml = (Yaml)PREPARED_YAMLS.get(clazz);
      if (yaml == null) {
         yaml = new Yaml(this.prepareConstructor(clazz));
         PREPARED_YAMLS.put(clazz, yaml);
      }

      ReentrantLock lock;
      synchronized(LOCKS) {
         lock = (ReentrantLock)LOCKS.get(clazz);
         if (lock == null) {
            lock = new ReentrantLock();
         }
      }

      lock.lock();

      StorageObject var5;
      try {
         T object = (T)((StorageObject)yaml.load(this.reader));
         if (object == null) {
            object = (T)((StorageObject)clazz.newInstance());
         }

         var5 = object;
      } catch (Exception ex) {
         throw new ObjectLoadException(ex);
      } finally {
         lock.unlock();
      }

      return var5;
   }

   private Constructor prepareConstructor(Class clazz) {
      Constructor constructor = new BukkitConstructor(clazz, this.plugin);
      Set<Class> classes = new HashSet();
      this.prepareConstructor(constructor, classes, clazz);
      return constructor;
   }

   private void prepareConstructor(Constructor constructor, Set classes, Class clazz) {
      classes.add(clazz);
      TypeDescription description = new TypeDescription(clazz);

      for(Field field : clazz.getDeclaredFields()) {
         this.prepareList(field, description, classes, constructor);
         this.prepareMap(field, description, classes, constructor);
         if (StorageObject.class.isAssignableFrom(field.getType()) && !classes.contains(field.getType())) {
            this.prepareConstructor(constructor, classes, field.getType());
         }
      }

      constructor.addTypeDescription(description);
   }

   private void prepareList(Field field, TypeDescription description, Set classes, Constructor constructor) {
      ListType listType = (ListType)field.getAnnotation(ListType.class);
      if (listType != null) {
         description.putListPropertyType(field.getName(), listType.value());
         if (StorageObject.class.isAssignableFrom(listType.value()) && !classes.contains(listType.value())) {
            this.prepareConstructor(constructor, classes, listType.value());
         }
      }

   }

   private void prepareMap(Field field, TypeDescription description, Set classes, Constructor constructor) {
      MapValueType mapType = (MapValueType)field.getAnnotation(MapValueType.class);
      if (mapType != null) {
         MapKeyType mapKeyType = (MapKeyType)field.getAnnotation(MapKeyType.class);
         description.putMapPropertyType(field.getName(), mapKeyType == null ? String.class : mapKeyType.value(), mapType.value());
         if (StorageObject.class.isAssignableFrom(mapType.value()) && !classes.contains(mapType.value())) {
            this.prepareConstructor(constructor, classes, mapType.value());
         }
      }

   }
}
