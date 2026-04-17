package org.hibernate.bytecode.instrumentation.internal.javassist;

import java.io.Serializable;
import java.util.Set;
import org.hibernate.bytecode.instrumentation.spi.AbstractFieldInterceptor;
import org.hibernate.bytecode.internal.javassist.FieldHandler;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public final class FieldInterceptorImpl extends AbstractFieldInterceptor implements FieldHandler, Serializable {
   FieldInterceptorImpl(SessionImplementor session, Set uninitializedFields, String entityName) {
      super(session, uninitializedFields, entityName);
   }

   public boolean readBoolean(Object target, String name, boolean oldValue) {
      return (Boolean)this.intercept(target, name, oldValue);
   }

   public byte readByte(Object target, String name, byte oldValue) {
      return (Byte)this.intercept(target, name, oldValue);
   }

   public char readChar(Object target, String name, char oldValue) {
      return (Character)this.intercept(target, name, oldValue);
   }

   public double readDouble(Object target, String name, double oldValue) {
      return (Double)this.intercept(target, name, oldValue);
   }

   public float readFloat(Object target, String name, float oldValue) {
      return (Float)this.intercept(target, name, oldValue);
   }

   public int readInt(Object target, String name, int oldValue) {
      return (Integer)this.intercept(target, name, oldValue);
   }

   public long readLong(Object target, String name, long oldValue) {
      return (Long)this.intercept(target, name, oldValue);
   }

   public short readShort(Object target, String name, short oldValue) {
      return (Short)this.intercept(target, name, oldValue);
   }

   public Object readObject(Object target, String name, Object oldValue) {
      Object value = this.intercept(target, name, oldValue);
      if (value instanceof HibernateProxy) {
         LazyInitializer li = ((HibernateProxy)value).getHibernateLazyInitializer();
         if (li.isUnwrap()) {
            value = li.getImplementation();
         }
      }

      return value;
   }

   public boolean writeBoolean(Object target, String name, boolean oldValue, boolean newValue) {
      this.dirty();
      this.intercept(target, name, oldValue);
      return newValue;
   }

   public byte writeByte(Object target, String name, byte oldValue, byte newValue) {
      this.dirty();
      this.intercept(target, name, oldValue);
      return newValue;
   }

   public char writeChar(Object target, String name, char oldValue, char newValue) {
      this.dirty();
      this.intercept(target, name, oldValue);
      return newValue;
   }

   public double writeDouble(Object target, String name, double oldValue, double newValue) {
      this.dirty();
      this.intercept(target, name, oldValue);
      return newValue;
   }

   public float writeFloat(Object target, String name, float oldValue, float newValue) {
      this.dirty();
      this.intercept(target, name, oldValue);
      return newValue;
   }

   public int writeInt(Object target, String name, int oldValue, int newValue) {
      this.dirty();
      this.intercept(target, name, oldValue);
      return newValue;
   }

   public long writeLong(Object target, String name, long oldValue, long newValue) {
      this.dirty();
      this.intercept(target, name, oldValue);
      return newValue;
   }

   public short writeShort(Object target, String name, short oldValue, short newValue) {
      this.dirty();
      this.intercept(target, name, oldValue);
      return newValue;
   }

   public Object writeObject(Object target, String name, Object oldValue, Object newValue) {
      this.dirty();
      this.intercept(target, name, oldValue);
      return newValue;
   }

   public String toString() {
      return "FieldInterceptorImpl(entityName=" + this.getEntityName() + ",dirty=" + this.isDirty() + ",uninitializedFields=" + this.getUninitializedFields() + ')';
   }
}
