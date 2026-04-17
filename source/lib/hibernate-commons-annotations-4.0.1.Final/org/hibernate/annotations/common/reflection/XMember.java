package org.hibernate.annotations.common.reflection;

public interface XMember extends XAnnotatedElement {
   String getName();

   boolean isCollection();

   boolean isArray();

   Class getCollectionClass();

   XClass getType();

   XClass getElementClass();

   XClass getClassOrElementClass();

   XClass getMapKey();

   int getModifiers();

   void setAccessible(boolean var1);

   Object invoke(Object var1, Object... var2);

   boolean isTypeResolved();
}
