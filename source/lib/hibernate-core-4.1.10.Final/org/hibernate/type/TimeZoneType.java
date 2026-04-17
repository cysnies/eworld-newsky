package org.hibernate.type;

import java.util.TimeZone;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.TimeZoneTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class TimeZoneType extends AbstractSingleColumnStandardBasicType implements LiteralType {
   public static final TimeZoneType INSTANCE = new TimeZoneType();

   public TimeZoneType() {
      super(VarcharTypeDescriptor.INSTANCE, TimeZoneTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "timezone";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }

   public String objectToSQLString(TimeZone value, Dialect dialect) throws Exception {
      return StringType.INSTANCE.objectToSQLString(value.getID(), dialect);
   }
}
