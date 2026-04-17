package org.hibernate.metamodel.binding;

import org.hibernate.AssertionFailure;
import org.hibernate.metamodel.domain.PluralAttribute;
import org.hibernate.metamodel.domain.PluralAttributeNature;

public class Helper {
   public static void checkPluralAttributeNature(PluralAttribute attribute, PluralAttributeNature expected) {
      if (attribute.getNature() != expected) {
         throw new AssertionFailure(String.format("Mismatched collection natures; expecting %s, but found %s", expected.getName(), attribute.getNature().getName()));
      }
   }
}
