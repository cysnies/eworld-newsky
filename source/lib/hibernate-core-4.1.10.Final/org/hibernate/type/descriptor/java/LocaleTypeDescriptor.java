package org.hibernate.type.descriptor.java;

import java.util.Comparator;
import java.util.Locale;
import java.util.StringTokenizer;
import org.hibernate.type.descriptor.WrapperOptions;

public class LocaleTypeDescriptor extends AbstractTypeDescriptor {
   public static final LocaleTypeDescriptor INSTANCE = new LocaleTypeDescriptor();

   public LocaleTypeDescriptor() {
      super(Locale.class);
   }

   public Comparator getComparator() {
      return LocaleTypeDescriptor.LocaleComparator.INSTANCE;
   }

   public String toString(Locale value) {
      return value.toString();
   }

   public Locale fromString(String string) {
      StringTokenizer tokens = new StringTokenizer(string, "_");
      String language = tokens.hasMoreTokens() ? tokens.nextToken() : "";
      String country = tokens.hasMoreTokens() ? tokens.nextToken() : "";
      String variant = "";

      for(String sep = ""; tokens.hasMoreTokens(); sep = "_") {
         variant = variant + sep + tokens.nextToken();
      }

      return new Locale(language, country, variant);
   }

   public Object unwrap(Locale value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isAssignableFrom(type)) {
         return value.toString();
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Locale wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isInstance(value)) {
         return this.fromString((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   public static class LocaleComparator implements Comparator {
      public static final LocaleComparator INSTANCE = new LocaleComparator();

      public int compare(Locale o1, Locale o2) {
         return o1.toString().compareTo(o2.toString());
      }
   }
}
