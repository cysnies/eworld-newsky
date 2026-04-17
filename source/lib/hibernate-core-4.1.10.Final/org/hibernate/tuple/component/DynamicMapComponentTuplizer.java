package org.hibernate.tuple.component;

import java.util.Map;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;
import org.hibernate.tuple.DynamicMapInstantiator;
import org.hibernate.tuple.Instantiator;

public class DynamicMapComponentTuplizer extends AbstractComponentTuplizer {
   public Class getMappedClass() {
      return Map.class;
   }

   protected Instantiator buildInstantiator(Component component) {
      return new DynamicMapInstantiator();
   }

   public DynamicMapComponentTuplizer(Component component) {
      super(component);
   }

   private PropertyAccessor buildPropertyAccessor(Property property) {
      return PropertyAccessorFactory.getDynamicMapPropertyAccessor();
   }

   protected Getter buildGetter(Component component, Property prop) {
      return this.buildPropertyAccessor(prop).getGetter((Class)null, prop.getName());
   }

   protected Setter buildSetter(Component component, Property prop) {
      return this.buildPropertyAccessor(prop).getSetter((Class)null, prop.getName());
   }
}
