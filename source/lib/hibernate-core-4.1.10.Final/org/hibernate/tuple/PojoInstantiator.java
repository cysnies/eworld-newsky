package org.hibernate.tuple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import org.hibernate.InstantiationException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.bytecode.spi.ReflectionOptimizer;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;
import org.jboss.logging.Logger;

public class PojoInstantiator implements Instantiator, Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, PojoInstantiator.class.getName());
   private transient Constructor constructor;
   private final Class mappedClass;
   private final transient ReflectionOptimizer.InstantiationOptimizer optimizer;
   private final boolean embeddedIdentifier;
   private final Class proxyInterface;
   private final boolean isAbstract;

   public PojoInstantiator(Component component, ReflectionOptimizer.InstantiationOptimizer optimizer) {
      this.mappedClass = component.getComponentClass();
      this.isAbstract = ReflectHelper.isAbstractClass(this.mappedClass);
      this.optimizer = optimizer;
      this.proxyInterface = null;
      this.embeddedIdentifier = false;

      try {
         this.constructor = ReflectHelper.getDefaultConstructor(this.mappedClass);
      } catch (PropertyNotFoundException var4) {
         LOG.noDefaultConstructor(this.mappedClass.getName());
         this.constructor = null;
      }

   }

   public PojoInstantiator(PersistentClass persistentClass, ReflectionOptimizer.InstantiationOptimizer optimizer) {
      this.mappedClass = persistentClass.getMappedClass();
      this.isAbstract = ReflectHelper.isAbstractClass(this.mappedClass);
      this.proxyInterface = persistentClass.getProxyInterface();
      this.embeddedIdentifier = persistentClass.hasEmbeddedIdentifier();
      this.optimizer = optimizer;

      try {
         this.constructor = ReflectHelper.getDefaultConstructor(this.mappedClass);
      } catch (PropertyNotFoundException var4) {
         LOG.noDefaultConstructor(this.mappedClass.getName());
         this.constructor = null;
      }

   }

   public PojoInstantiator(EntityBinding entityBinding, ReflectionOptimizer.InstantiationOptimizer optimizer) {
      this.mappedClass = entityBinding.getEntity().getClassReference();
      this.isAbstract = ReflectHelper.isAbstractClass(this.mappedClass);
      this.proxyInterface = (Class)entityBinding.getProxyInterfaceType().getValue();
      this.embeddedIdentifier = entityBinding.getHierarchyDetails().getEntityIdentifier().isEmbedded();
      this.optimizer = optimizer;

      try {
         this.constructor = ReflectHelper.getDefaultConstructor(this.mappedClass);
      } catch (PropertyNotFoundException var4) {
         LOG.noDefaultConstructor(this.mappedClass.getName());
         this.constructor = null;
      }

   }

   private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
      stream.defaultReadObject();
      this.constructor = ReflectHelper.getDefaultConstructor(this.mappedClass);
   }

   public Object instantiate() {
      if (this.isAbstract) {
         throw new InstantiationException("Cannot instantiate abstract class or interface: ", this.mappedClass);
      } else if (this.optimizer != null) {
         return this.optimizer.newInstance();
      } else if (this.constructor == null) {
         throw new InstantiationException("No default constructor for entity: ", this.mappedClass);
      } else {
         try {
            return this.constructor.newInstance((Object[])null);
         } catch (Exception e) {
            throw new InstantiationException("Could not instantiate entity: ", this.mappedClass, e);
         }
      }
   }

   public Object instantiate(Serializable id) {
      boolean useEmbeddedIdentifierInstanceAsEntity = this.embeddedIdentifier && id != null && id.getClass().equals(this.mappedClass);
      return useEmbeddedIdentifierInstanceAsEntity ? id : this.instantiate();
   }

   public boolean isInstance(Object object) {
      return this.mappedClass.isInstance(object) || this.proxyInterface != null && this.proxyInterface.isInstance(object);
   }
}
