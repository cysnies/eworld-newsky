package javax.persistence.metamodel;

import java.util.Set;

public interface Metamodel {
   EntityType entity(Class var1);

   ManagedType managedType(Class var1);

   EmbeddableType embeddable(Class var1);

   Set getManagedTypes();

   Set getEntities();

   Set getEmbeddables();
}
