package org.hibernate.tuple.component;

import java.lang.reflect.Method;
import java.util.Iterator;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;
import org.hibernate.tuple.Instantiator;

public abstract class AbstractComponentTuplizer implements ComponentTuplizer {
   protected final Getter[] getters;
   protected final Setter[] setters;
   protected final int propertySpan;
   protected final Instantiator instantiator;
   protected final boolean hasCustomAccessors;

   protected abstract Instantiator buildInstantiator(Component var1);

   protected abstract Getter buildGetter(Component var1, Property var2);

   protected abstract Setter buildSetter(Component var1, Property var2);

   protected AbstractComponentTuplizer(Component component) {
      this.propertySpan = component.getPropertySpan();
      this.getters = new Getter[this.propertySpan];
      this.setters = new Setter[this.propertySpan];
      Iterator iter = component.getPropertyIterator();
      boolean foundCustomAccessor = false;

      for(int i = 0; iter.hasNext(); ++i) {
         Property prop = (Property)iter.next();
         this.getters[i] = this.buildGetter(component, prop);
         this.setters[i] = this.buildSetter(component, prop);
         if (!prop.isBasicPropertyAccessor()) {
            foundCustomAccessor = true;
         }
      }

      this.hasCustomAccessors = foundCustomAccessor;
      this.instantiator = this.buildInstantiator(component);
   }

   public Object getPropertyValue(Object component, int i) throws HibernateException {
      return this.getters[i].get(component);
   }

   public Object[] getPropertyValues(Object component) throws HibernateException {
      Object[] values = new Object[this.propertySpan];

      for(int i = 0; i < this.propertySpan; ++i) {
         values[i] = this.getPropertyValue(component, i);
      }

      return values;
   }

   public boolean isInstance(Object object) {
      return this.instantiator.isInstance(object);
   }

   public void setPropertyValues(Object component, Object[] values) throws HibernateException {
      for(int i = 0; i < this.propertySpan; ++i) {
         this.setters[i].set(component, values[i], (SessionFactoryImplementor)null);
      }

   }

   public Object instantiate() throws HibernateException {
      return this.instantiator.instantiate();
   }

   public Object getParent(Object component) {
      return null;
   }

   public boolean hasParentProperty() {
      return false;
   }

   public boolean isMethodOf(Method method) {
      return false;
   }

   public void setParent(Object component, Object parent, SessionFactoryImplementor factory) {
      throw new UnsupportedOperationException();
   }

   public Getter getGetter(int i) {
      return this.getters[i];
   }
}
