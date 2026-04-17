package org.hibernate.tuple.component;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.Component;

public class ComponentTuplizerFactory implements Serializable {
   private static final Class[] COMPONENT_TUP_CTOR_SIG = new Class[]{Component.class};
   private Map defaultImplClassByMode = buildBaseMapping();

   public void registerDefaultTuplizerClass(EntityMode entityMode, Class tuplizerClass) {
      assert this.isComponentTuplizerImplementor(tuplizerClass) : "Specified tuplizer class [" + tuplizerClass.getName() + "] does not implement " + ComponentTuplizer.class.getName();

      assert this.hasProperConstructor(tuplizerClass) : "Specified tuplizer class [" + tuplizerClass.getName() + "] is not properly instantiatable";

      this.defaultImplClassByMode.put(entityMode, tuplizerClass);
   }

   public ComponentTuplizer constructTuplizer(String tuplizerClassName, Component metadata) {
      try {
         Class<? extends ComponentTuplizer> tuplizerClass = ReflectHelper.classForName(tuplizerClassName);
         return this.constructTuplizer(tuplizerClass, metadata);
      } catch (ClassNotFoundException var4) {
         throw new HibernateException("Could not locate specified tuplizer class [" + tuplizerClassName + "]");
      }
   }

   public ComponentTuplizer constructTuplizer(Class tuplizerClass, Component metadata) {
      Constructor<? extends ComponentTuplizer> constructor = this.getProperConstructor(tuplizerClass);

      assert constructor != null : "Unable to locate proper constructor for tuplizer [" + tuplizerClass.getName() + "]";

      try {
         return (ComponentTuplizer)constructor.newInstance(metadata);
      } catch (Throwable t) {
         throw new HibernateException("Unable to instantiate default tuplizer [" + tuplizerClass.getName() + "]", t);
      }
   }

   public ComponentTuplizer constructDefaultTuplizer(EntityMode entityMode, Component metadata) {
      Class<? extends ComponentTuplizer> tuplizerClass = (Class)this.defaultImplClassByMode.get(entityMode);
      if (tuplizerClass == null) {
         throw new HibernateException("could not determine default tuplizer class to use [" + entityMode + "]");
      } else {
         return this.constructTuplizer(tuplizerClass, metadata);
      }
   }

   private boolean isComponentTuplizerImplementor(Class tuplizerClass) {
      return ReflectHelper.implementsInterface(tuplizerClass, ComponentTuplizer.class);
   }

   private boolean hasProperConstructor(Class tuplizerClass) {
      return this.getProperConstructor(tuplizerClass) != null;
   }

   private Constructor getProperConstructor(Class clazz) {
      Constructor<? extends ComponentTuplizer> constructor = null;

      try {
         constructor = clazz.getDeclaredConstructor(COMPONENT_TUP_CTOR_SIG);
         if (!ReflectHelper.isPublic(constructor)) {
            try {
               constructor.setAccessible(true);
            } catch (SecurityException var4) {
               constructor = null;
            }
         }
      } catch (NoSuchMethodException var5) {
      }

      return constructor;
   }

   private static Map buildBaseMapping() {
      Map<EntityMode, Class<? extends ComponentTuplizer>> map = new ConcurrentHashMap();
      map.put(EntityMode.POJO, PojoComponentTuplizer.class);
      map.put(EntityMode.MAP, DynamicMapComponentTuplizer.class);
      return map;
   }
}
