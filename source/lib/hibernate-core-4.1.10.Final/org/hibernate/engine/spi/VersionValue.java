package org.hibernate.engine.spi;

import org.hibernate.MappingException;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class VersionValue implements UnsavedValueStrategy {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, VersionValue.class.getName());
   private final Object value;
   public static final VersionValue NULL = new VersionValue() {
      public final Boolean isUnsaved(Object version) {
         VersionValue.LOG.trace("Version unsaved-value strategy NULL");
         return version == null;
      }

      public Object getDefaultValue(Object currentValue) {
         return null;
      }

      public String toString() {
         return "VERSION_SAVE_NULL";
      }
   };
   public static final VersionValue UNDEFINED = new VersionValue() {
      public final Boolean isUnsaved(Object version) {
         VersionValue.LOG.trace("Version unsaved-value strategy UNDEFINED");
         return version == null ? Boolean.TRUE : null;
      }

      public Object getDefaultValue(Object currentValue) {
         return currentValue;
      }

      public String toString() {
         return "VERSION_UNDEFINED";
      }
   };
   public static final VersionValue NEGATIVE = new VersionValue() {
      public final Boolean isUnsaved(Object version) throws MappingException {
         VersionValue.LOG.trace("Version unsaved-value strategy NEGATIVE");
         if (version == null) {
            return Boolean.TRUE;
         } else if (version instanceof Number) {
            return ((Number)version).longValue() < 0L;
         } else {
            throw new MappingException("unsaved-value NEGATIVE may only be used with short, int and long types");
         }
      }

      public Object getDefaultValue(Object currentValue) {
         return IdentifierGeneratorHelper.getIntegralDataTypeHolder(currentValue.getClass()).initialize(-1L).makeValue();
      }

      public String toString() {
         return "VERSION_NEGATIVE";
      }
   };

   protected VersionValue() {
      this.value = null;
   }

   public VersionValue(Object value) {
      this.value = value;
   }

   public Boolean isUnsaved(Object version) throws MappingException {
      LOG.tracev("Version unsaved-value: {0}", this.value);
      return version == null || version.equals(this.value);
   }

   public Object getDefaultValue(Object currentValue) {
      return this.value;
   }

   public String toString() {
      return "version unsaved-value: " + this.value;
   }
}
