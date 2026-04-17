package org.hibernate.type;

public class ObjectType extends AnyType implements BasicType {
   public static final ObjectType INSTANCE = new ObjectType();

   public ObjectType() {
      super(StringType.INSTANCE, SerializableType.INSTANCE);
   }

   public String getName() {
      return "object";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Object.class.getName()};
   }
}
