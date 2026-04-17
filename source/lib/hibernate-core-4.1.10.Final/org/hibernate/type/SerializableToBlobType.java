package org.hibernate.type;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.MappingException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.descriptor.java.SerializableTypeDescriptor;
import org.hibernate.type.descriptor.sql.BlobTypeDescriptor;
import org.hibernate.usertype.DynamicParameterizedType;

public class SerializableToBlobType extends AbstractSingleColumnStandardBasicType implements DynamicParameterizedType {
   public static final String CLASS_NAME = "classname";
   private static final long serialVersionUID = 1L;

   public SerializableToBlobType() {
      super(BlobTypeDescriptor.DEFAULT, new SerializableTypeDescriptor(Serializable.class));
   }

   public String getName() {
      return this.getClass().getName();
   }

   public void setParameterValues(Properties parameters) {
      DynamicParameterizedType.ParameterType reader = (DynamicParameterizedType.ParameterType)parameters.get("org.hibernate.type.ParameterType");
      if (reader != null) {
         this.setJavaTypeDescriptor(new SerializableTypeDescriptor(reader.getReturnedClass()));
      } else {
         String className = parameters.getProperty("classname");
         if (className == null) {
            throw new MappingException("No class name defined for type: " + SerializableToBlobType.class.getName());
         }

         try {
            this.setJavaTypeDescriptor(new SerializableTypeDescriptor(ReflectHelper.classForName(className)));
         } catch (ClassNotFoundException e) {
            throw new MappingException("Unable to load class from classname parameter", e);
         }
      }

   }
}
