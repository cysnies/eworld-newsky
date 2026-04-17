package org.hibernate.loader.criteria;

import java.io.Serializable;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.type.Type;

interface CriteriaInfoProvider {
   String getName();

   Serializable[] getSpaces();

   PropertyMapping getPropertyMapping();

   Type getType(String var1);
}
