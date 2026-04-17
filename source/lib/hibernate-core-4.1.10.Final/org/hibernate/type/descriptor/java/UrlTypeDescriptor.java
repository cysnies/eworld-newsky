package org.hibernate.type.descriptor.java;

import java.net.MalformedURLException;
import java.net.URL;
import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;

public class UrlTypeDescriptor extends AbstractTypeDescriptor {
   public static final UrlTypeDescriptor INSTANCE = new UrlTypeDescriptor();

   public UrlTypeDescriptor() {
      super(URL.class);
   }

   public String toString(URL value) {
      return value.toExternalForm();
   }

   public URL fromString(String string) {
      try {
         return new URL(string);
      } catch (MalformedURLException e) {
         throw new HibernateException("Unable to convert string [" + string + "] to URL : " + e);
      }
   }

   public Object unwrap(URL value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isAssignableFrom(type)) {
         return this.toString(value);
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public URL wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isInstance(value)) {
         return this.fromString((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
