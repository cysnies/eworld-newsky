package org.hibernate.type.descriptor.java;

import java.util.Currency;
import org.hibernate.type.descriptor.WrapperOptions;

public class CurrencyTypeDescriptor extends AbstractTypeDescriptor {
   public static final CurrencyTypeDescriptor INSTANCE = new CurrencyTypeDescriptor();

   public CurrencyTypeDescriptor() {
      super(Currency.class);
   }

   public String toString(Currency value) {
      return value.getCurrencyCode();
   }

   public Currency fromString(String string) {
      return Currency.getInstance(string);
   }

   public Object unwrap(Currency value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isAssignableFrom(type)) {
         return value.getCurrencyCode();
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Currency wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isInstance(value)) {
         return Currency.getInstance((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
