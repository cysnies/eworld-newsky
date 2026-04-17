package javax.persistence.metamodel;

import java.util.Set;

public interface IdentifiableType extends ManagedType {
   SingularAttribute getId(Class var1);

   SingularAttribute getDeclaredId(Class var1);

   SingularAttribute getVersion(Class var1);

   SingularAttribute getDeclaredVersion(Class var1);

   IdentifiableType getSupertype();

   boolean hasSingleIdAttribute();

   boolean hasVersionAttribute();

   Set getIdClassAttributes();

   Type getIdType();
}
