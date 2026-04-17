package org.hibernate.annotations.common.reflection;

public interface Filter {
   boolean returnStatic();

   boolean returnTransient();
}
