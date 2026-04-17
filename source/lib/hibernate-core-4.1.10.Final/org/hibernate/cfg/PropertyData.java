package org.hibernate.cfg;

import org.hibernate.MappingException;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;

public interface PropertyData {
   AccessType getDefaultAccess();

   String getPropertyName() throws MappingException;

   XClass getClassOrElement() throws MappingException;

   XClass getPropertyClass() throws MappingException;

   String getClassOrElementName() throws MappingException;

   String getTypeName() throws MappingException;

   XProperty getProperty();

   XClass getDeclaringClass();
}
