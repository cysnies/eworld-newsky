package javax.persistence.metamodel;

import java.util.Set;

public interface ManagedType extends Type {
   Set getAttributes();

   Set getDeclaredAttributes();

   SingularAttribute getSingularAttribute(String var1, Class var2);

   SingularAttribute getDeclaredSingularAttribute(String var1, Class var2);

   Set getSingularAttributes();

   Set getDeclaredSingularAttributes();

   CollectionAttribute getCollection(String var1, Class var2);

   CollectionAttribute getDeclaredCollection(String var1, Class var2);

   SetAttribute getSet(String var1, Class var2);

   SetAttribute getDeclaredSet(String var1, Class var2);

   ListAttribute getList(String var1, Class var2);

   ListAttribute getDeclaredList(String var1, Class var2);

   MapAttribute getMap(String var1, Class var2, Class var3);

   MapAttribute getDeclaredMap(String var1, Class var2, Class var3);

   Set getPluralAttributes();

   Set getDeclaredPluralAttributes();

   Attribute getAttribute(String var1);

   Attribute getDeclaredAttribute(String var1);

   SingularAttribute getSingularAttribute(String var1);

   SingularAttribute getDeclaredSingularAttribute(String var1);

   CollectionAttribute getCollection(String var1);

   CollectionAttribute getDeclaredCollection(String var1);

   SetAttribute getSet(String var1);

   SetAttribute getDeclaredSet(String var1);

   ListAttribute getList(String var1);

   ListAttribute getDeclaredList(String var1);

   MapAttribute getMap(String var1);

   MapAttribute getDeclaredMap(String var1);
}
