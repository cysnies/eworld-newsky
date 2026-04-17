package javax.persistence.metamodel;

public interface Bindable {
   BindableType getBindableType();

   Class getBindableJavaType();

   public static enum BindableType {
      SINGULAR_ATTRIBUTE,
      PLURAL_ATTRIBUTE,
      ENTITY_TYPE;
   }
}
