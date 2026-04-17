package org.hibernate.engine.spi;

import java.io.Serializable;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class IdentifierValue implements UnsavedValueStrategy {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, IdentifierValue.class.getName());
   private final Serializable value;
   public static final IdentifierValue ANY = new IdentifierValue() {
      public final Boolean isUnsaved(Object id) {
         IdentifierValue.LOG.trace("ID unsaved-value strategy ANY");
         return Boolean.TRUE;
      }

      public Serializable getDefaultValue(Object currentValue) {
         return (Serializable)currentValue;
      }

      public String toString() {
         return "SAVE_ANY";
      }
   };
   public static final IdentifierValue NONE = new IdentifierValue() {
      public final Boolean isUnsaved(Object id) {
         IdentifierValue.LOG.trace("ID unsaved-value strategy NONE");
         return Boolean.FALSE;
      }

      public Serializable getDefaultValue(Object currentValue) {
         return (Serializable)currentValue;
      }

      public String toString() {
         return "SAVE_NONE";
      }
   };
   public static final IdentifierValue NULL = new IdentifierValue() {
      public final Boolean isUnsaved(Object id) {
         IdentifierValue.LOG.trace("ID unsaved-value strategy NULL");
         return id == null;
      }

      public Serializable getDefaultValue(Object currentValue) {
         return null;
      }

      public String toString() {
         return "SAVE_NULL";
      }
   };
   public static final IdentifierValue UNDEFINED = new IdentifierValue() {
      public final Boolean isUnsaved(Object id) {
         IdentifierValue.LOG.trace("ID unsaved-value strategy UNDEFINED");
         return null;
      }

      public Serializable getDefaultValue(Object currentValue) {
         return null;
      }

      public String toString() {
         return "UNDEFINED";
      }
   };

   protected IdentifierValue() {
      this.value = null;
   }

   public IdentifierValue(Serializable value) {
      this.value = value;
   }

   public Boolean isUnsaved(Object id) {
      LOG.tracev("ID unsaved-value: {0}", this.value);
      return id == null || id.equals(this.value);
   }

   public Serializable getDefaultValue(Object currentValue) {
      return this.value;
   }

   public String toString() {
      return "identifier unsaved-value: " + this.value;
   }
}
