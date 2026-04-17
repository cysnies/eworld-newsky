package org.hibernate.tuple.entity;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;

public class EntityTuplizerFactory implements Serializable {
   public static final Class[] ENTITY_TUP_CTOR_SIG = new Class[]{EntityMetamodel.class, PersistentClass.class};
   public static final Class[] ENTITY_TUP_CTOR_SIG_NEW = new Class[]{EntityMetamodel.class, EntityBinding.class};
   private Map defaultImplClassByMode = buildBaseMapping();

   public void registerDefaultTuplizerClass(EntityMode entityMode, Class tuplizerClass) {
      assert this.isEntityTuplizerImplementor(tuplizerClass) : "Specified tuplizer class [" + tuplizerClass.getName() + "] does not implement " + EntityTuplizer.class.getName();

      assert this.hasProperConstructor(tuplizerClass, ENTITY_TUP_CTOR_SIG) : "Specified tuplizer class [" + tuplizerClass.getName() + "] is not properly instantiatable";

      assert this.hasProperConstructor(tuplizerClass, ENTITY_TUP_CTOR_SIG_NEW) : "Specified tuplizer class [" + tuplizerClass.getName() + "] is not properly instantiatable";

      this.defaultImplClassByMode.put(entityMode, tuplizerClass);
   }

   public EntityTuplizer constructTuplizer(String tuplizerClassName, EntityMetamodel metamodel, PersistentClass persistentClass) {
      try {
         Class<? extends EntityTuplizer> tuplizerClass = ReflectHelper.classForName(tuplizerClassName);
         return this.constructTuplizer(tuplizerClass, metamodel, persistentClass);
      } catch (ClassNotFoundException var5) {
         throw new HibernateException("Could not locate specified tuplizer class [" + tuplizerClassName + "]");
      }
   }

   public EntityTuplizer constructTuplizer(String tuplizerClassName, EntityMetamodel metamodel, EntityBinding entityBinding) {
      try {
         Class<? extends EntityTuplizer> tuplizerClass = ReflectHelper.classForName(tuplizerClassName);
         return this.constructTuplizer(tuplizerClass, metamodel, entityBinding);
      } catch (ClassNotFoundException var5) {
         throw new HibernateException("Could not locate specified tuplizer class [" + tuplizerClassName + "]");
      }
   }

   public EntityTuplizer constructTuplizer(Class tuplizerClass, EntityMetamodel metamodel, PersistentClass persistentClass) {
      Constructor<? extends EntityTuplizer> constructor = this.getProperConstructor(tuplizerClass, ENTITY_TUP_CTOR_SIG);

      assert constructor != null : "Unable to locate proper constructor for tuplizer [" + tuplizerClass.getName() + "]";

      try {
         return (EntityTuplizer)constructor.newInstance(metamodel, persistentClass);
      } catch (Throwable t) {
         throw new HibernateException("Unable to instantiate default tuplizer [" + tuplizerClass.getName() + "]", t);
      }
   }

   public EntityTuplizer constructTuplizer(Class tuplizerClass, EntityMetamodel metamodel, EntityBinding entityBinding) {
      Constructor<? extends EntityTuplizer> constructor = this.getProperConstructor(tuplizerClass, ENTITY_TUP_CTOR_SIG_NEW);

      assert constructor != null : "Unable to locate proper constructor for tuplizer [" + tuplizerClass.getName() + "]";

      try {
         return (EntityTuplizer)constructor.newInstance(metamodel, entityBinding);
      } catch (Throwable t) {
         throw new HibernateException("Unable to instantiate default tuplizer [" + tuplizerClass.getName() + "]", t);
      }
   }

   public EntityTuplizer constructDefaultTuplizer(EntityMode entityMode, EntityMetamodel metamodel, PersistentClass persistentClass) {
      Class<? extends EntityTuplizer> tuplizerClass = (Class)this.defaultImplClassByMode.get(entityMode);
      if (tuplizerClass == null) {
         throw new HibernateException("could not determine default tuplizer class to use [" + entityMode + "]");
      } else {
         return this.constructTuplizer(tuplizerClass, metamodel, persistentClass);
      }
   }

   public EntityTuplizer constructDefaultTuplizer(EntityMode entityMode, EntityMetamodel metamodel, EntityBinding entityBinding) {
      Class<? extends EntityTuplizer> tuplizerClass = (Class)this.defaultImplClassByMode.get(entityMode);
      if (tuplizerClass == null) {
         throw new HibernateException("could not determine default tuplizer class to use [" + entityMode + "]");
      } else {
         return this.constructTuplizer(tuplizerClass, metamodel, entityBinding);
      }
   }

   private boolean isEntityTuplizerImplementor(Class tuplizerClass) {
      return ReflectHelper.implementsInterface(tuplizerClass, EntityTuplizer.class);
   }

   private boolean hasProperConstructor(Class tuplizerClass, Class[] constructorArgs) {
      return this.getProperConstructor(tuplizerClass, constructorArgs) != null && !ReflectHelper.isAbstractClass(tuplizerClass);
   }

   private Constructor getProperConstructor(Class clazz, Class[] constructorArgs) {
      Constructor<? extends EntityTuplizer> constructor = null;

      try {
         constructor = clazz.getDeclaredConstructor(constructorArgs);
         if (!ReflectHelper.isPublic(constructor)) {
            try {
               constructor.setAccessible(true);
            } catch (SecurityException var5) {
               constructor = null;
            }
         }
      } catch (NoSuchMethodException var6) {
      }

      return constructor;
   }

   private static Map buildBaseMapping() {
      Map<EntityMode, Class<? extends EntityTuplizer>> map = new ConcurrentHashMap();
      map.put(EntityMode.POJO, PojoEntityTuplizer.class);
      map.put(EntityMode.MAP, DynamicMapEntityTuplizer.class);
      return map;
   }
}
