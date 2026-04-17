package org.hibernate.internal.jaxb.mapping.hbm;

public interface SingularAttributeSource extends MetaAttributeContainer {
   String getName();

   String getTypeAttribute();

   JaxbTypeElement getType();

   String getAccess();
}
