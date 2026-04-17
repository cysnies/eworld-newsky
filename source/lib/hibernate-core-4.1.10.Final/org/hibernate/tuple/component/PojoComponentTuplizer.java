package org.hibernate.tuple.component;

import java.io.Serializable;
import java.lang.reflect.Method;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.bytecode.spi.BasicProxyFactory;
import org.hibernate.bytecode.spi.ReflectionOptimizer;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;
import org.hibernate.property.BackrefPropertyAccessor;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.PojoInstantiator;

public class PojoComponentTuplizer extends AbstractComponentTuplizer {
   private final Class componentClass;
   private ReflectionOptimizer optimizer;
   private final Getter parentGetter;
   private final Setter parentSetter;

   public PojoComponentTuplizer(Component component) {
      super(component);
      this.componentClass = component.getComponentClass();
      String[] getterNames = new String[this.propertySpan];
      String[] setterNames = new String[this.propertySpan];
      Class[] propTypes = new Class[this.propertySpan];

      for(int i = 0; i < this.propertySpan; ++i) {
         getterNames[i] = this.getters[i].getMethodName();
         setterNames[i] = this.setters[i].getMethodName();
         propTypes[i] = this.getters[i].getReturnType();
      }

      String parentPropertyName = component.getParentProperty();
      if (parentPropertyName == null) {
         this.parentSetter = null;
         this.parentGetter = null;
      } else {
         PropertyAccessor pa = PropertyAccessorFactory.getPropertyAccessor((String)null);
         this.parentSetter = pa.getSetter(this.componentClass, parentPropertyName);
         this.parentGetter = pa.getGetter(this.componentClass, parentPropertyName);
      }

      if (!this.hasCustomAccessors && Environment.useReflectionOptimizer()) {
         this.optimizer = Environment.getBytecodeProvider().getReflectionOptimizer(this.componentClass, getterNames, setterNames, propTypes);
      } else {
         this.optimizer = null;
      }

   }

   public Class getMappedClass() {
      return this.componentClass;
   }

   public Object[] getPropertyValues(Object component) throws HibernateException {
      if (component == BackrefPropertyAccessor.UNKNOWN) {
         return new Object[this.propertySpan];
      } else {
         return this.optimizer != null && this.optimizer.getAccessOptimizer() != null ? this.optimizer.getAccessOptimizer().getPropertyValues(component) : super.getPropertyValues(component);
      }
   }

   public void setPropertyValues(Object component, Object[] values) throws HibernateException {
      if (this.optimizer != null && this.optimizer.getAccessOptimizer() != null) {
         this.optimizer.getAccessOptimizer().setPropertyValues(component, values);
      } else {
         super.setPropertyValues(component, values);
      }

   }

   public Object getParent(Object component) {
      return this.parentGetter.get(component);
   }

   public boolean hasParentProperty() {
      return this.parentGetter != null;
   }

   public boolean isMethodOf(Method method) {
      for(int i = 0; i < this.propertySpan; ++i) {
         Method getterMethod = this.getters[i].getMethod();
         if (getterMethod != null && getterMethod.equals(method)) {
            return true;
         }
      }

      return false;
   }

   public void setParent(Object component, Object parent, SessionFactoryImplementor factory) {
      this.parentSetter.set(component, parent, factory);
   }

   protected Instantiator buildInstantiator(Component component) {
      if (component.isEmbedded() && ReflectHelper.isAbstractClass(component.getComponentClass())) {
         return new ProxiedInstantiator(component);
      } else {
         return this.optimizer == null ? new PojoInstantiator(component, (ReflectionOptimizer.InstantiationOptimizer)null) : new PojoInstantiator(component, this.optimizer.getInstantiationOptimizer());
      }
   }

   protected Getter buildGetter(Component component, Property prop) {
      return prop.getGetter(component.getComponentClass());
   }

   protected Setter buildSetter(Component component, Property prop) {
      return prop.getSetter(component.getComponentClass());
   }

   private static class ProxiedInstantiator implements Instantiator {
      private final Class proxiedClass;
      private final BasicProxyFactory factory;

      public ProxiedInstantiator(Component component) {
         this.proxiedClass = component.getComponentClass();
         if (this.proxiedClass.isInterface()) {
            this.factory = Environment.getBytecodeProvider().getProxyFactoryFactory().buildBasicProxyFactory((Class)null, new Class[]{this.proxiedClass});
         } else {
            this.factory = Environment.getBytecodeProvider().getProxyFactoryFactory().buildBasicProxyFactory(this.proxiedClass, (Class[])null);
         }

      }

      public Object instantiate(Serializable id) {
         throw new AssertionFailure("ProxiedInstantiator can only be used to instantiate component");
      }

      public Object instantiate() {
         return this.factory.getProxy();
      }

      public boolean isInstance(Object object) {
         return this.proxiedClass.isInstance(object);
      }
   }
}
