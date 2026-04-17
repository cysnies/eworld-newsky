package javax.persistence.metamodel;

public interface PluralAttribute extends Attribute, Bindable {
   CollectionType getCollectionType();

   Type getElementType();

   public static enum CollectionType {
      COLLECTION,
      SET,
      LIST,
      MAP;
   }
}
