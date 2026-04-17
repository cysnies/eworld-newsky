package org.hibernate.type.descriptor.sql;

import java.io.Serializable;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public interface SqlTypeDescriptor extends Serializable {
   int getSqlType();

   boolean canBeRemapped();

   ValueBinder getBinder(JavaTypeDescriptor var1);

   ValueExtractor getExtractor(JavaTypeDescriptor var1);
}
