package org.hibernate.metamodel.domain;

import java.util.Set;

public interface AttributeContainer extends Type {
   String getRoleBaseName();

   Attribute locateAttribute(String var1);

   Set attributes();

   SingularAttribute locateSingularAttribute(String var1);

   SingularAttribute createSingularAttribute(String var1);

   SingularAttribute createVirtualSingularAttribute(String var1);

   SingularAttribute locateComponentAttribute(String var1);

   SingularAttribute createComponentAttribute(String var1, Component var2);

   PluralAttribute locatePluralAttribute(String var1);

   PluralAttribute locateBag(String var1);

   PluralAttribute createBag(String var1);

   PluralAttribute locateSet(String var1);

   PluralAttribute createSet(String var1);

   IndexedPluralAttribute locateList(String var1);

   IndexedPluralAttribute createList(String var1);

   IndexedPluralAttribute locateMap(String var1);

   IndexedPluralAttribute createMap(String var1);
}
