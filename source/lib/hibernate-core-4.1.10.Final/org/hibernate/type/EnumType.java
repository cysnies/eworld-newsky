package org.hibernate.type;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.persistence.Enumerated;
import javax.persistence.MapKeyEnumerated;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.LoggableUserType;
import org.jboss.logging.Logger;

public class EnumType implements EnhancedUserType, DynamicParameterizedType, LoggableUserType, Serializable {
   private static final Logger LOG = Logger.getLogger(EnumType.class.getName());
   public static final String ENUM = "enumClass";
   public static final String NAMED = "useNamed";
   public static final String TYPE = "type";
   private Class enumClass;
   private EnumValueMapper enumValueMapper;
   private int sqlType = 4;

   public int[] sqlTypes() {
      return new int[]{this.sqlType};
   }

   public Class returnedClass() {
      return this.enumClass;
   }

   public boolean equals(Object x, Object y) throws HibernateException {
      return x == y;
   }

   public int hashCode(Object x) throws HibernateException {
      return x == null ? 0 : x.hashCode();
   }

   public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws SQLException {
      if (this.enumValueMapper == null) {
         this.resolveEnumValueMapper(rs, names[0]);
      }

      return this.enumValueMapper.getValue(rs, names);
   }

   private void resolveEnumValueMapper(ResultSet rs, String name) {
      if (this.enumValueMapper == null) {
         try {
            this.resolveEnumValueMapper(rs.getMetaData().getColumnType(rs.findColumn(name)));
         } catch (Exception e) {
            LOG.debugf("JDBC driver threw exception calling java.sql.ResultSetMetaData.getColumnType; using fallback determination [%s] : %s", this.enumClass.getName(), e.getMessage());

            try {
               Object value = rs.getObject(name);
               if (Number.class.isInstance(value)) {
                  this.treatAsOrdinal();
               } else {
                  this.treatAsNamed();
               }
            } catch (SQLException var5) {
               this.treatAsOrdinal();
            }
         }
      }

   }

   private void resolveEnumValueMapper(int columnType) {
      if (this.isOrdinal(columnType)) {
         this.treatAsOrdinal();
      } else {
         this.treatAsNamed();
      }

   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      if (this.enumValueMapper == null) {
         this.resolveEnumValueMapper(st, index);
      }

      this.enumValueMapper.setValue(st, (Enum)value, index);
   }

   private void resolveEnumValueMapper(PreparedStatement st, int index) {
      if (this.enumValueMapper == null) {
         try {
            this.resolveEnumValueMapper(st.getParameterMetaData().getParameterType(index));
         } catch (Exception e) {
            LOG.debugf("JDBC driver threw exception calling java.sql.ParameterMetaData#getParameterType; falling back to ordinal-based enum mapping [%s] : %s", this.enumClass.getName(), e.getMessage());
            this.treatAsOrdinal();
         }
      }

   }

   public Object deepCopy(Object value) throws HibernateException {
      return value;
   }

   public boolean isMutable() {
      return false;
   }

   public Serializable disassemble(Object value) throws HibernateException {
      return (Serializable)value;
   }

   public Object assemble(Serializable cached, Object owner) throws HibernateException {
      return cached;
   }

   public Object replace(Object original, Object target, Object owner) throws HibernateException {
      return original;
   }

   public void setParameterValues(Properties parameters) {
      DynamicParameterizedType.ParameterType reader = (DynamicParameterizedType.ParameterType)parameters.get("org.hibernate.type.ParameterType");
      if (reader != null) {
         this.enumClass = reader.getReturnedClass().asSubclass(Enum.class);
         javax.persistence.EnumType enumType = this.getEnumType(reader);
         boolean isOrdinal;
         if (enumType == null) {
            isOrdinal = true;
         } else if (javax.persistence.EnumType.ORDINAL.equals(enumType)) {
            isOrdinal = true;
         } else {
            if (!javax.persistence.EnumType.STRING.equals(enumType)) {
               throw new AssertionFailure("Unknown EnumType: " + enumType);
            }

            isOrdinal = false;
         }

         if (isOrdinal) {
            this.treatAsOrdinal();
         } else {
            this.treatAsNamed();
         }

         this.sqlType = this.enumValueMapper.getSqlType();
      } else {
         String enumClassName = (String)parameters.get("enumClass");

         try {
            this.enumClass = ReflectHelper.classForName(enumClassName, this.getClass()).asSubclass(Enum.class);
         } catch (ClassNotFoundException exception) {
            throw new HibernateException("Enum class not found", exception);
         }

         Object useNamedSetting = parameters.get("useNamed");
         if (useNamedSetting != null) {
            boolean useNamed = ConfigurationHelper.getBoolean("useNamed", parameters);
            if (useNamed) {
               this.treatAsNamed();
            } else {
               this.treatAsOrdinal();
            }

            this.sqlType = this.enumValueMapper.getSqlType();
         }
      }

      String type = (String)parameters.get("type");
      if (type != null) {
         this.sqlType = Integer.decode(type);
      }

   }

   private void treatAsOrdinal() {
      if (this.enumValueMapper == null || !OrdinalEnumValueMapper.class.isInstance(this.enumValueMapper)) {
         this.enumValueMapper = new OrdinalEnumValueMapper();
         this.sqlType = this.enumValueMapper.getSqlType();
      }

   }

   private void treatAsNamed() {
      if (this.enumValueMapper == null || !NamedEnumValueMapper.class.isInstance(this.enumValueMapper)) {
         this.enumValueMapper = new NamedEnumValueMapper();
         this.sqlType = this.enumValueMapper.getSqlType();
      }

   }

   private javax.persistence.EnumType getEnumType(DynamicParameterizedType.ParameterType reader) {
      javax.persistence.EnumType enumType = null;
      if (reader.isPrimaryKey()) {
         MapKeyEnumerated enumAnn = (MapKeyEnumerated)this.getAnnotation(reader.getAnnotationsMethod(), MapKeyEnumerated.class);
         if (enumAnn != null) {
            enumType = enumAnn.value();
         }
      } else {
         Enumerated enumAnn = (Enumerated)this.getAnnotation(reader.getAnnotationsMethod(), Enumerated.class);
         if (enumAnn != null) {
            enumType = enumAnn.value();
         }
      }

      return enumType;
   }

   private Annotation getAnnotation(Annotation[] annotations, Class anClass) {
      for(Annotation annotation : annotations) {
         if (anClass.isInstance(annotation)) {
            return annotation;
         }
      }

      return null;
   }

   public String objectToSQLString(Object value) {
      return this.enumValueMapper.objectToSQLString((Enum)value);
   }

   public String toXMLString(Object value) {
      return this.enumValueMapper.toXMLString((Enum)value);
   }

   public Object fromXMLString(String xmlValue) {
      return this.enumValueMapper.fromXMLString(xmlValue);
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) {
      return this.enumValueMapper != null ? this.enumValueMapper.toXMLString((Enum)value) : value.toString();
   }

   public boolean isOrdinal() {
      return this.isOrdinal(this.sqlType);
   }

   private boolean isOrdinal(int paramType) {
      switch (paramType) {
         case -6:
         case -5:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 8:
            return true;
         case -4:
         case -3:
         case -2:
         case 0:
         case 7:
         case 9:
         case 10:
         case 11:
         default:
            throw new HibernateException("Unable to persist an Enum in a column of SQL Type: " + paramType);
         case -1:
         case 1:
         case 12:
            return false;
      }
   }

   public abstract class EnumValueMapperSupport implements EnumValueMapper {
      protected abstract Object extractJdbcValue(Enum var1);

      public void setValue(PreparedStatement st, Enum value, int index) throws SQLException {
         Object jdbcValue = value == null ? null : this.extractJdbcValue(value);
         if (jdbcValue == null) {
            if (EnumType.LOG.isTraceEnabled()) {
               EnumType.LOG.trace(String.format("Binding null to parameter: [%s]", index));
            }

            st.setNull(index, this.getSqlType());
         } else {
            if (EnumType.LOG.isTraceEnabled()) {
               EnumType.LOG.trace(String.format("Binding [%s] to parameter: [%s]", jdbcValue, index));
            }

            st.setObject(index, jdbcValue, EnumType.this.sqlType);
         }
      }
   }

   private class OrdinalEnumValueMapper extends EnumValueMapperSupport implements EnumValueMapper, Serializable {
      private transient Enum[] enumsByOrdinal;

      private OrdinalEnumValueMapper() {
      }

      public int getSqlType() {
         return 4;
      }

      public Enum getValue(ResultSet rs, String[] names) throws SQLException {
         int ordinal = rs.getInt(names[0]);
         if (rs.wasNull()) {
            if (EnumType.LOG.isTraceEnabled()) {
               EnumType.LOG.trace(String.format("Returning null as column [%s]", names[0]));
            }

            return null;
         } else {
            Enum enumValue = this.fromOrdinal(ordinal);
            if (EnumType.LOG.isTraceEnabled()) {
               EnumType.LOG.trace(String.format("Returning [%s] as column [%s]", enumValue, names[0]));
            }

            return enumValue;
         }
      }

      private Enum fromOrdinal(int ordinal) {
         Enum[] enumsByOrdinal = this.enumsByOrdinal();
         if (ordinal >= 0 && ordinal < enumsByOrdinal.length) {
            return enumsByOrdinal[ordinal];
         } else {
            throw new IllegalArgumentException(String.format("Unknown ordinal value [%s] for enum class [%s]", ordinal, EnumType.this.enumClass.getName()));
         }
      }

      private Enum[] enumsByOrdinal() {
         if (this.enumsByOrdinal == null) {
            this.enumsByOrdinal = (Enum[])EnumType.this.enumClass.getEnumConstants();
            if (this.enumsByOrdinal == null) {
               throw new HibernateException("Failed to init enum values");
            }
         }

         return this.enumsByOrdinal;
      }

      public String objectToSQLString(Enum value) {
         return this.toXMLString(value);
      }

      public String toXMLString(Enum value) {
         return Integer.toString(value.ordinal());
      }

      public Enum fromXMLString(String xml) {
         return this.fromOrdinal(Integer.parseInt(xml));
      }

      protected Object extractJdbcValue(Enum value) {
         return value.ordinal();
      }
   }

   private class NamedEnumValueMapper extends EnumValueMapperSupport implements EnumValueMapper, Serializable {
      private NamedEnumValueMapper() {
      }

      public int getSqlType() {
         return 12;
      }

      public Enum getValue(ResultSet rs, String[] names) throws SQLException {
         String value = rs.getString(names[0]);
         if (rs.wasNull()) {
            if (EnumType.LOG.isTraceEnabled()) {
               EnumType.LOG.trace(String.format("Returning null as column [%s]", names[0]));
            }

            return null;
         } else {
            Enum enumValue = this.fromName(value);
            if (EnumType.LOG.isTraceEnabled()) {
               EnumType.LOG.trace(String.format("Returning [%s] as column [%s]", enumValue, names[0]));
            }

            return enumValue;
         }
      }

      private Enum fromName(String name) {
         try {
            return Enum.valueOf(EnumType.this.enumClass, name);
         } catch (IllegalArgumentException var3) {
            throw new IllegalArgumentException(String.format("Unknown name value [%s] for enum class [%s]", name, EnumType.this.enumClass.getName()));
         }
      }

      public String objectToSQLString(Enum value) {
         return '\'' + this.toXMLString(value) + '\'';
      }

      public String toXMLString(Enum value) {
         return value.name();
      }

      public Enum fromXMLString(String xml) {
         return this.fromName(xml);
      }

      protected Object extractJdbcValue(Enum value) {
         return value.name();
      }
   }

   private interface EnumValueMapper extends Serializable {
      int getSqlType();

      Enum getValue(ResultSet var1, String[] var2) throws SQLException;

      void setValue(PreparedStatement var1, Enum var2, int var3) throws SQLException;

      String objectToSQLString(Enum var1);

      String toXMLString(Enum var1);

      Enum fromXMLString(String var1);
   }
}
