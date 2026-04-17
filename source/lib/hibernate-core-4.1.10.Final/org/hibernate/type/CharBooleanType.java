package org.hibernate.type;

import org.hibernate.type.descriptor.java.BooleanTypeDescriptor;
import org.hibernate.type.descriptor.sql.CharTypeDescriptor;

/** @deprecated */
public abstract class CharBooleanType extends BooleanType {
   private final String stringValueTrue;
   private final String stringValueFalse;

   protected CharBooleanType(char characterValueTrue, char characterValueFalse) {
      super(CharTypeDescriptor.INSTANCE, new BooleanTypeDescriptor(characterValueTrue, characterValueFalse));
      this.stringValueTrue = String.valueOf(characterValueTrue);
      this.stringValueFalse = String.valueOf(characterValueFalse);
   }

   /** @deprecated */
   protected final String getTrueString() {
      return this.stringValueTrue;
   }

   /** @deprecated */
   protected final String getFalseString() {
      return this.stringValueFalse;
   }
}
