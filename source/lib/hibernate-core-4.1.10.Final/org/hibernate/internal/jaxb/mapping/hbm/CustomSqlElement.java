package org.hibernate.internal.jaxb.mapping.hbm;

public interface CustomSqlElement {
   String getValue();

   boolean isCallable();

   JaxbCheckAttribute getCheck();
}
