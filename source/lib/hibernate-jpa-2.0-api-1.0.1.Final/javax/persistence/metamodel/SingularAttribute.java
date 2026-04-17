package javax.persistence.metamodel;

public interface SingularAttribute extends Attribute, Bindable {
   boolean isId();

   boolean isVersion();

   boolean isOptional();

   Type getType();
}
