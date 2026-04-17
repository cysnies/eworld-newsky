package javax.persistence.criteria;

import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

public interface Path extends Expression {
   Bindable getModel();

   Path getParentPath();

   Path get(SingularAttribute var1);

   Expression get(PluralAttribute var1);

   Expression get(MapAttribute var1);

   Expression type();

   Path get(String var1);
}
