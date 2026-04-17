package javax.persistence.metamodel;

public interface MapAttribute extends PluralAttribute {
   Class getKeyJavaType();

   Type getKeyType();
}
