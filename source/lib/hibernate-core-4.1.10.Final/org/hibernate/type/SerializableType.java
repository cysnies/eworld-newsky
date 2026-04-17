package org.hibernate.type;

import java.io.Serializable;
import org.hibernate.type.descriptor.java.SerializableTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarbinaryTypeDescriptor;

public class SerializableType extends AbstractSingleColumnStandardBasicType {
   public static final SerializableType INSTANCE = new SerializableType(Serializable.class);
   private final Class serializableClass;

   public SerializableType(Class serializableClass) {
      super(VarbinaryTypeDescriptor.INSTANCE, new SerializableTypeDescriptor(serializableClass));
      this.serializableClass = serializableClass;
   }

   public String getName() {
      return this.serializableClass == Serializable.class ? "serializable" : this.serializableClass.getName();
   }
}
