package org.hibernate.usertype;

public interface EnhancedUserType extends UserType {
   String objectToSQLString(Object var1);

   /** @deprecated */
   @Deprecated
   String toXMLString(Object var1);

   /** @deprecated */
   @Deprecated
   Object fromXMLString(String var1);
}
