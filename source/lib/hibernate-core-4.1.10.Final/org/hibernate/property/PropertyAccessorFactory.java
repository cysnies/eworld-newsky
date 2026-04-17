package org.hibernate.property;

import java.util.Map;
import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.binding.AttributeBinding;

public final class PropertyAccessorFactory {
   private static final PropertyAccessor BASIC_PROPERTY_ACCESSOR = new BasicPropertyAccessor();
   private static final PropertyAccessor DIRECT_PROPERTY_ACCESSOR = new DirectPropertyAccessor();
   private static final PropertyAccessor MAP_ACCESSOR = new MapAccessor();
   private static final PropertyAccessor NOOP_ACCESSOR = new NoopAccessor();
   private static final PropertyAccessor EMBEDDED_PROPERTY_ACCESSOR = new EmbeddedPropertyAccessor();

   public static PropertyAccessor getPropertyAccessor(Property property, EntityMode mode) throws MappingException {
      if (null != mode && !EntityMode.POJO.equals(mode)) {
         if (EntityMode.MAP.equals(mode)) {
            return getDynamicMapPropertyAccessor();
         } else {
            throw new MappingException("Unknown entity mode [" + mode + "]");
         }
      } else {
         return getPojoPropertyAccessor(property.getPropertyAccessorName());
      }
   }

   public static PropertyAccessor getPropertyAccessor(AttributeBinding property, EntityMode mode) throws MappingException {
      if (null != mode && !EntityMode.POJO.equals(mode)) {
         if (EntityMode.MAP.equals(mode)) {
            return getDynamicMapPropertyAccessor();
         } else {
            throw new MappingException("Unknown entity mode [" + mode + "]");
         }
      } else {
         return getPojoPropertyAccessor(property.getPropertyAccessorName());
      }
   }

   private static PropertyAccessor getPojoPropertyAccessor(String pojoAccessorStrategy) {
      if (!StringHelper.isEmpty(pojoAccessorStrategy) && !"property".equals(pojoAccessorStrategy)) {
         if ("field".equals(pojoAccessorStrategy)) {
            return DIRECT_PROPERTY_ACCESSOR;
         } else if ("embedded".equals(pojoAccessorStrategy)) {
            return EMBEDDED_PROPERTY_ACCESSOR;
         } else {
            return "noop".equals(pojoAccessorStrategy) ? NOOP_ACCESSOR : resolveCustomAccessor(pojoAccessorStrategy);
         }
      } else {
         return BASIC_PROPERTY_ACCESSOR;
      }
   }

   public static PropertyAccessor getDynamicMapPropertyAccessor() throws MappingException {
      return MAP_ACCESSOR;
   }

   private static PropertyAccessor resolveCustomAccessor(String accessorName) {
      Class accessorClass;
      try {
         accessorClass = ReflectHelper.classForName(accessorName);
      } catch (ClassNotFoundException cnfe) {
         throw new MappingException("could not find PropertyAccessor class: " + accessorName, cnfe);
      }

      try {
         return (PropertyAccessor)accessorClass.newInstance();
      } catch (Exception e) {
         throw new MappingException("could not instantiate PropertyAccessor class: " + accessorName, e);
      }
   }

   private PropertyAccessorFactory() {
   }

   public static PropertyAccessor getPropertyAccessor(Class optionalClass, String type) throws MappingException {
      if (type == null) {
         type = optionalClass != null && optionalClass != Map.class ? "property" : "map";
      }

      return getPropertyAccessor(type);
   }

   public static PropertyAccessor getPropertyAccessor(String type) throws MappingException {
      if (type != null && !"property".equals(type)) {
         if ("field".equals(type)) {
            return DIRECT_PROPERTY_ACCESSOR;
         } else if ("map".equals(type)) {
            return MAP_ACCESSOR;
         } else if ("embedded".equals(type)) {
            return EMBEDDED_PROPERTY_ACCESSOR;
         } else {
            return "noop".equals(type) ? NOOP_ACCESSOR : resolveCustomAccessor(type);
         }
      } else {
         return BASIC_PROPERTY_ACCESSOR;
      }
   }
}
