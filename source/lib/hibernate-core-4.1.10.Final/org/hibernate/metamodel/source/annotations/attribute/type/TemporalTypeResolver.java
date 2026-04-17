package org.hibernate.metamodel.source.annotations.attribute.type;

import java.util.Calendar;
import java.util.Date;
import javax.persistence.TemporalType;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.attribute.MappedAttribute;
import org.hibernate.type.StandardBasicTypes;
import org.jboss.jandex.AnnotationInstance;

public class TemporalTypeResolver extends AbstractAttributeTypeResolver {
   private final MappedAttribute mappedAttribute;
   private final boolean isMapKey;

   public TemporalTypeResolver(MappedAttribute mappedAttribute) {
      if (mappedAttribute == null) {
         throw new AssertionFailure("MappedAttribute is null");
      } else {
         this.mappedAttribute = mappedAttribute;
         this.isMapKey = false;
      }
   }

   public String resolveHibernateTypeName(AnnotationInstance temporalAnnotation) {
      if (isTemporalType(this.mappedAttribute.getAttributeType())) {
         if (temporalAnnotation == null) {
            throw new AnnotationException("Attribute " + this.mappedAttribute.getName() + " is a Temporal type, but no @Temporal annotation found.");
         } else {
            TemporalType temporalType = (TemporalType)JandexHelper.getEnumValue(temporalAnnotation, "value", TemporalType.class);
            boolean isDate = Date.class.isAssignableFrom(this.mappedAttribute.getAttributeType());
            String type;
            switch (temporalType) {
               case DATE:
                  type = isDate ? StandardBasicTypes.DATE.getName() : StandardBasicTypes.CALENDAR_DATE.getName();
                  break;
               case TIME:
                  type = StandardBasicTypes.TIME.getName();
                  if (!isDate) {
                     throw new NotYetImplementedException("Calendar cannot persist TIME only");
                  }
                  break;
               case TIMESTAMP:
                  type = isDate ? StandardBasicTypes.TIMESTAMP.getName() : StandardBasicTypes.CALENDAR.getName();
                  break;
               default:
                  throw new AssertionFailure("Unknown temporal type: " + temporalType);
            }

            return type;
         }
      } else if (temporalAnnotation != null) {
         throw new AnnotationException("@Temporal should only be set on a java.util.Date or java.util.Calendar property: " + this.mappedAttribute.getName());
      } else {
         return null;
      }
   }

   protected AnnotationInstance getTypeDeterminingAnnotationInstance() {
      return JandexHelper.getSingleAnnotation(this.mappedAttribute.annotations(), JPADotNames.TEMPORAL);
   }

   private static boolean isTemporalType(Class type) {
      return Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type);
   }
}
