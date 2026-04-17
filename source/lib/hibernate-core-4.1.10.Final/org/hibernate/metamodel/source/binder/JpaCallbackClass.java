package org.hibernate.metamodel.source.binder;

public interface JpaCallbackClass {
   String getCallbackMethod(Class var1);

   String getName();

   boolean isListener();
}
