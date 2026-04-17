package org.hibernate.type;

import org.hibernate.type.descriptor.java.ClassTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class ClassType extends AbstractSingleColumnStandardBasicType {
   public static final ClassType INSTANCE = new ClassType();

   public ClassType() {
      super(VarcharTypeDescriptor.INSTANCE, ClassTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "class";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }
}
