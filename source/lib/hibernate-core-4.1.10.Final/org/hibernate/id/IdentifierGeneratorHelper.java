package org.hibernate.id;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public final class IdentifierGeneratorHelper {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, IdentifierGeneratorHelper.class.getName());
   public static final Serializable SHORT_CIRCUIT_INDICATOR = new Serializable() {
      public String toString() {
         return "SHORT_CIRCUIT_INDICATOR";
      }
   };
   public static final Serializable POST_INSERT_INDICATOR = new Serializable() {
      public String toString() {
         return "POST_INSERT_INDICATOR";
      }
   };

   public static Serializable getGeneratedIdentity(ResultSet rs, String identifier, Type type) throws SQLException, HibernateException {
      if (!rs.next()) {
         throw new HibernateException("The database returned no natively generated identity value");
      } else {
         Serializable id = get(rs, identifier, type);
         LOG.debugf("Natively generated identity: %s", id);
         return id;
      }
   }

   public static Serializable get(ResultSet rs, String identifier, Type type) throws SQLException, IdentifierGenerationException {
      if (ResultSetIdentifierConsumer.class.isInstance(type)) {
         return ((ResultSetIdentifierConsumer)type).consumeIdentifier(rs);
      } else {
         if (CustomType.class.isInstance(type)) {
            CustomType customType = (CustomType)type;
            if (ResultSetIdentifierConsumer.class.isInstance(customType.getUserType())) {
               return ((ResultSetIdentifierConsumer)customType.getUserType()).consumeIdentifier(rs);
            }
         }

         Class clazz = type.getReturnedClass();
         if (rs.getMetaData().getColumnCount() == 1) {
            if (clazz == Long.class) {
               return rs.getLong(1);
            } else if (clazz == Integer.class) {
               return rs.getInt(1);
            } else if (clazz == Short.class) {
               return rs.getShort(1);
            } else if (clazz == String.class) {
               return rs.getString(1);
            } else if (clazz == BigInteger.class) {
               return rs.getBigDecimal(1).setScale(0, 7).toBigInteger();
            } else if (clazz == BigDecimal.class) {
               return rs.getBigDecimal(1).setScale(0, 7);
            } else {
               throw new IdentifierGenerationException("unrecognized id type : " + type.getName() + " -> " + clazz.getName());
            }
         } else if (clazz == Long.class) {
            return rs.getLong(identifier);
         } else if (clazz == Integer.class) {
            return rs.getInt(identifier);
         } else if (clazz == Short.class) {
            return rs.getShort(identifier);
         } else if (clazz == String.class) {
            return rs.getString(identifier);
         } else if (clazz == BigInteger.class) {
            return rs.getBigDecimal(identifier).setScale(0, 7).toBigInteger();
         } else if (clazz == BigDecimal.class) {
            return rs.getBigDecimal(identifier).setScale(0, 7);
         } else {
            throw new IdentifierGenerationException("unrecognized id type : " + type.getName() + " -> " + clazz.getName());
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public static Number createNumber(long value, Class clazz) throws IdentifierGenerationException {
      if (clazz == Long.class) {
         return value;
      } else if (clazz == Integer.class) {
         return (int)value;
      } else if (clazz == Short.class) {
         return (short)((int)value);
      } else {
         throw new IdentifierGenerationException("unrecognized id type : " + clazz.getName());
      }
   }

   public static IntegralDataTypeHolder getIntegralDataTypeHolder(Class integralType) {
      if (integralType != Long.class && integralType != Integer.class && integralType != Short.class) {
         if (integralType == BigInteger.class) {
            return new BigIntegerHolder();
         } else if (integralType == BigDecimal.class) {
            return new BigDecimalHolder();
         } else {
            throw new IdentifierGenerationException("Unknown integral data type for ids : " + integralType.getName());
         }
      } else {
         return new BasicHolder(integralType);
      }
   }

   public static long extractLong(IntegralDataTypeHolder holder) {
      if (holder.getClass() == BasicHolder.class) {
         ((BasicHolder)holder).checkInitialized();
         return ((BasicHolder)holder).value;
      } else if (holder.getClass() == BigIntegerHolder.class) {
         ((BigIntegerHolder)holder).checkInitialized();
         return ((BigIntegerHolder)holder).value.longValue();
      } else if (holder.getClass() == BigDecimalHolder.class) {
         ((BigDecimalHolder)holder).checkInitialized();
         return ((BigDecimalHolder)holder).value.longValue();
      } else {
         throw new IdentifierGenerationException("Unknown IntegralDataTypeHolder impl [" + holder + "]");
      }
   }

   public static BigInteger extractBigInteger(IntegralDataTypeHolder holder) {
      if (holder.getClass() == BasicHolder.class) {
         ((BasicHolder)holder).checkInitialized();
         return BigInteger.valueOf(((BasicHolder)holder).value);
      } else if (holder.getClass() == BigIntegerHolder.class) {
         ((BigIntegerHolder)holder).checkInitialized();
         return ((BigIntegerHolder)holder).value;
      } else if (holder.getClass() == BigDecimalHolder.class) {
         ((BigDecimalHolder)holder).checkInitialized();
         return ((BigDecimalHolder)holder).value.toBigInteger();
      } else {
         throw new IdentifierGenerationException("Unknown IntegralDataTypeHolder impl [" + holder + "]");
      }
   }

   public static BigDecimal extractBigDecimal(IntegralDataTypeHolder holder) {
      if (holder.getClass() == BasicHolder.class) {
         ((BasicHolder)holder).checkInitialized();
         return BigDecimal.valueOf(((BasicHolder)holder).value);
      } else if (holder.getClass() == BigIntegerHolder.class) {
         ((BigIntegerHolder)holder).checkInitialized();
         return new BigDecimal(((BigIntegerHolder)holder).value);
      } else if (holder.getClass() == BigDecimalHolder.class) {
         ((BigDecimalHolder)holder).checkInitialized();
         return ((BigDecimalHolder)holder).value;
      } else {
         throw new IdentifierGenerationException("Unknown IntegralDataTypeHolder impl [" + holder + "]");
      }
   }

   private IdentifierGeneratorHelper() {
   }

   public static class BasicHolder implements IntegralDataTypeHolder {
      private final Class exactType;
      private long value = Long.MIN_VALUE;

      public BasicHolder(Class exactType) {
         this.exactType = exactType;
         if (exactType != Long.class && exactType != Integer.class && exactType != Short.class) {
            throw new IdentifierGenerationException("Invalid type for basic integral holder : " + exactType);
         }
      }

      public long getActualLongValue() {
         return this.value;
      }

      public IntegralDataTypeHolder initialize(long value) {
         this.value = value;
         return this;
      }

      public IntegralDataTypeHolder initialize(ResultSet resultSet, long defaultValue) throws SQLException {
         long value = resultSet.getLong(1);
         if (resultSet.wasNull()) {
            value = defaultValue;
         }

         return this.initialize(value);
      }

      public void bind(PreparedStatement preparedStatement, int position) throws SQLException {
         preparedStatement.setLong(position, this.value);
      }

      public IntegralDataTypeHolder increment() {
         this.checkInitialized();
         ++this.value;
         return this;
      }

      private void checkInitialized() {
         if (this.value == Long.MIN_VALUE) {
            throw new IdentifierGenerationException("integral holder was not initialized");
         }
      }

      public IntegralDataTypeHolder add(long addend) {
         this.checkInitialized();
         this.value += addend;
         return this;
      }

      public IntegralDataTypeHolder decrement() {
         this.checkInitialized();
         --this.value;
         return this;
      }

      public IntegralDataTypeHolder subtract(long subtrahend) {
         this.checkInitialized();
         this.value -= subtrahend;
         return this;
      }

      public IntegralDataTypeHolder multiplyBy(IntegralDataTypeHolder factor) {
         return this.multiplyBy(IdentifierGeneratorHelper.extractLong(factor));
      }

      public IntegralDataTypeHolder multiplyBy(long factor) {
         this.checkInitialized();
         this.value *= factor;
         return this;
      }

      public boolean eq(IntegralDataTypeHolder other) {
         return this.eq(IdentifierGeneratorHelper.extractLong(other));
      }

      public boolean eq(long value) {
         this.checkInitialized();
         return this.value == value;
      }

      public boolean lt(IntegralDataTypeHolder other) {
         return this.lt(IdentifierGeneratorHelper.extractLong(other));
      }

      public boolean lt(long value) {
         this.checkInitialized();
         return this.value < value;
      }

      public boolean gt(IntegralDataTypeHolder other) {
         return this.gt(IdentifierGeneratorHelper.extractLong(other));
      }

      public boolean gt(long value) {
         this.checkInitialized();
         return this.value > value;
      }

      public IntegralDataTypeHolder copy() {
         BasicHolder copy = new BasicHolder(this.exactType);
         copy.value = this.value;
         return copy;
      }

      public Number makeValue() {
         this.checkInitialized();
         if (this.exactType == Long.class) {
            return this.value;
         } else {
            return (Number)(this.exactType == Integer.class ? (int)this.value : (short)((int)this.value));
         }
      }

      public Number makeValueThenIncrement() {
         Number result = this.makeValue();
         ++this.value;
         return result;
      }

      public Number makeValueThenAdd(long addend) {
         Number result = this.makeValue();
         this.value += addend;
         return result;
      }

      public String toString() {
         return "BasicHolder[" + this.exactType.getName() + "[" + this.value + "]]";
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BasicHolder that = (BasicHolder)o;
            return this.value == that.value;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return (int)(this.value ^ this.value >>> 32);
      }
   }

   public static class BigIntegerHolder implements IntegralDataTypeHolder {
      private BigInteger value;

      public IntegralDataTypeHolder initialize(long value) {
         this.value = BigInteger.valueOf(value);
         return this;
      }

      public IntegralDataTypeHolder initialize(ResultSet resultSet, long defaultValue) throws SQLException {
         BigDecimal rsValue = resultSet.getBigDecimal(1);
         if (resultSet.wasNull()) {
            return this.initialize(defaultValue);
         } else {
            this.value = rsValue.setScale(0, 7).toBigInteger();
            return this;
         }
      }

      public void bind(PreparedStatement preparedStatement, int position) throws SQLException {
         preparedStatement.setBigDecimal(position, new BigDecimal(this.value));
      }

      public IntegralDataTypeHolder increment() {
         this.checkInitialized();
         this.value = this.value.add(BigInteger.ONE);
         return this;
      }

      private void checkInitialized() {
         if (this.value == null) {
            throw new IdentifierGenerationException("integral holder was not initialized");
         }
      }

      public IntegralDataTypeHolder add(long increment) {
         this.checkInitialized();
         this.value = this.value.add(BigInteger.valueOf(increment));
         return this;
      }

      public IntegralDataTypeHolder decrement() {
         this.checkInitialized();
         this.value = this.value.subtract(BigInteger.ONE);
         return this;
      }

      public IntegralDataTypeHolder subtract(long subtrahend) {
         this.checkInitialized();
         this.value = this.value.subtract(BigInteger.valueOf(subtrahend));
         return this;
      }

      public IntegralDataTypeHolder multiplyBy(IntegralDataTypeHolder factor) {
         this.checkInitialized();
         this.value = this.value.multiply(IdentifierGeneratorHelper.extractBigInteger(factor));
         return this;
      }

      public IntegralDataTypeHolder multiplyBy(long factor) {
         this.checkInitialized();
         this.value = this.value.multiply(BigInteger.valueOf(factor));
         return this;
      }

      public boolean eq(IntegralDataTypeHolder other) {
         this.checkInitialized();
         return this.value.compareTo(IdentifierGeneratorHelper.extractBigInteger(other)) == 0;
      }

      public boolean eq(long value) {
         this.checkInitialized();
         return this.value.compareTo(BigInteger.valueOf(value)) == 0;
      }

      public boolean lt(IntegralDataTypeHolder other) {
         this.checkInitialized();
         return this.value.compareTo(IdentifierGeneratorHelper.extractBigInteger(other)) < 0;
      }

      public boolean lt(long value) {
         this.checkInitialized();
         return this.value.compareTo(BigInteger.valueOf(value)) < 0;
      }

      public boolean gt(IntegralDataTypeHolder other) {
         this.checkInitialized();
         return this.value.compareTo(IdentifierGeneratorHelper.extractBigInteger(other)) > 0;
      }

      public boolean gt(long value) {
         this.checkInitialized();
         return this.value.compareTo(BigInteger.valueOf(value)) > 0;
      }

      public IntegralDataTypeHolder copy() {
         BigIntegerHolder copy = new BigIntegerHolder();
         copy.value = this.value;
         return copy;
      }

      public Number makeValue() {
         this.checkInitialized();
         return this.value;
      }

      public Number makeValueThenIncrement() {
         Number result = this.makeValue();
         this.value = this.value.add(BigInteger.ONE);
         return result;
      }

      public Number makeValueThenAdd(long addend) {
         Number result = this.makeValue();
         this.value = this.value.add(BigInteger.valueOf(addend));
         return result;
      }

      public String toString() {
         return "BigIntegerHolder[" + this.value + "]";
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BigIntegerHolder that = (BigIntegerHolder)o;
            return this.value == null ? that.value == null : this.value.equals(that.value);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.value != null ? this.value.hashCode() : 0;
      }
   }

   public static class BigDecimalHolder implements IntegralDataTypeHolder {
      private BigDecimal value;

      public IntegralDataTypeHolder initialize(long value) {
         this.value = BigDecimal.valueOf(value);
         return this;
      }

      public IntegralDataTypeHolder initialize(ResultSet resultSet, long defaultValue) throws SQLException {
         BigDecimal rsValue = resultSet.getBigDecimal(1);
         if (resultSet.wasNull()) {
            return this.initialize(defaultValue);
         } else {
            this.value = rsValue.setScale(0, 7);
            return this;
         }
      }

      public void bind(PreparedStatement preparedStatement, int position) throws SQLException {
         preparedStatement.setBigDecimal(position, this.value);
      }

      public IntegralDataTypeHolder increment() {
         this.checkInitialized();
         this.value = this.value.add(BigDecimal.ONE);
         return this;
      }

      private void checkInitialized() {
         if (this.value == null) {
            throw new IdentifierGenerationException("integral holder was not initialized");
         }
      }

      public IntegralDataTypeHolder add(long increment) {
         this.checkInitialized();
         this.value = this.value.add(BigDecimal.valueOf(increment));
         return this;
      }

      public IntegralDataTypeHolder decrement() {
         this.checkInitialized();
         this.value = this.value.subtract(BigDecimal.ONE);
         return this;
      }

      public IntegralDataTypeHolder subtract(long subtrahend) {
         this.checkInitialized();
         this.value = this.value.subtract(BigDecimal.valueOf(subtrahend));
         return this;
      }

      public IntegralDataTypeHolder multiplyBy(IntegralDataTypeHolder factor) {
         this.checkInitialized();
         this.value = this.value.multiply(IdentifierGeneratorHelper.extractBigDecimal(factor));
         return this;
      }

      public IntegralDataTypeHolder multiplyBy(long factor) {
         this.checkInitialized();
         this.value = this.value.multiply(BigDecimal.valueOf(factor));
         return this;
      }

      public boolean eq(IntegralDataTypeHolder other) {
         this.checkInitialized();
         return this.value.compareTo(IdentifierGeneratorHelper.extractBigDecimal(other)) == 0;
      }

      public boolean eq(long value) {
         this.checkInitialized();
         return this.value.compareTo(BigDecimal.valueOf(value)) == 0;
      }

      public boolean lt(IntegralDataTypeHolder other) {
         this.checkInitialized();
         return this.value.compareTo(IdentifierGeneratorHelper.extractBigDecimal(other)) < 0;
      }

      public boolean lt(long value) {
         this.checkInitialized();
         return this.value.compareTo(BigDecimal.valueOf(value)) < 0;
      }

      public boolean gt(IntegralDataTypeHolder other) {
         this.checkInitialized();
         return this.value.compareTo(IdentifierGeneratorHelper.extractBigDecimal(other)) > 0;
      }

      public boolean gt(long value) {
         this.checkInitialized();
         return this.value.compareTo(BigDecimal.valueOf(value)) > 0;
      }

      public IntegralDataTypeHolder copy() {
         BigDecimalHolder copy = new BigDecimalHolder();
         copy.value = this.value;
         return copy;
      }

      public Number makeValue() {
         this.checkInitialized();
         return this.value;
      }

      public Number makeValueThenIncrement() {
         Number result = this.makeValue();
         this.value = this.value.add(BigDecimal.ONE);
         return result;
      }

      public Number makeValueThenAdd(long addend) {
         Number result = this.makeValue();
         this.value = this.value.add(BigDecimal.valueOf(addend));
         return result;
      }

      public String toString() {
         return "BigDecimalHolder[" + this.value + "]";
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BigDecimalHolder that = (BigDecimalHolder)o;
            return this.value == null ? that.value == null : this.value.equals(that.value);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.value != null ? this.value.hashCode() : 0;
      }
   }
}
