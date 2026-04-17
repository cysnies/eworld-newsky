package org.hibernate.cfg.annotations;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.MapKeyTemporal;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.AccessType;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.Ejb3Column;
import org.hibernate.cfg.Ejb3JoinColumn;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.cfg.PkDrivenByDefaultMapsIdSecondPass;
import org.hibernate.cfg.SetSimpleValueTypeSecondPass;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.TypeDef;
import org.hibernate.type.CharacterArrayClobType;
import org.hibernate.type.CharacterArrayNClobType;
import org.hibernate.type.CharacterNCharType;
import org.hibernate.type.EnumType;
import org.hibernate.type.PrimitiveCharacterArrayClobType;
import org.hibernate.type.PrimitiveCharacterArrayNClobType;
import org.hibernate.type.SerializableToBlobType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.StringNVarcharType;
import org.hibernate.type.WrappedMaterializedBlobType;
import org.hibernate.usertype.DynamicParameterizedType;
import org.jboss.logging.Logger;

public class SimpleValueBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SimpleValueBinder.class.getName());
   private String propertyName;
   private String returnedClassName;
   private Ejb3Column[] columns;
   private String persistentClassName;
   private String explicitType = "";
   private String defaultType = "";
   private Properties typeParameters = new Properties();
   private Mappings mappings;
   private Table table;
   private SimpleValue simpleValue;
   private boolean isVersion;
   private String timeStampVersionType;
   private boolean key;
   private String referencedEntityName;
   private XProperty xproperty;
   private AccessType accessType;

   public void setReferencedEntityName(String referencedEntityName) {
      this.referencedEntityName = referencedEntityName;
   }

   public boolean isVersion() {
      return this.isVersion;
   }

   public void setVersion(boolean isVersion) {
      this.isVersion = isVersion;
   }

   public void setTimestampVersionType(String versionType) {
      this.timeStampVersionType = versionType;
   }

   public void setPropertyName(String propertyName) {
      this.propertyName = propertyName;
   }

   public void setReturnedClassName(String returnedClassName) {
      this.returnedClassName = returnedClassName;
      if (this.defaultType.length() == 0) {
         this.defaultType = returnedClassName;
      }

   }

   public void setTable(Table table) {
      this.table = table;
   }

   public void setColumns(Ejb3Column[] columns) {
      this.columns = columns;
   }

   public void setPersistentClassName(String persistentClassName) {
      this.persistentClassName = persistentClassName;
   }

   public void setType(XProperty property, XClass returnedClass, String declaringClassName) {
      if (returnedClass != null) {
         XClass returnedClassOrElement = returnedClass;
         boolean isArray = false;
         if (property.isArray()) {
            returnedClassOrElement = property.getElementClass();
            isArray = true;
         }

         this.xproperty = property;
         String type = "";
         boolean isNationalized = property.isAnnotationPresent(Nationalized.class) || this.mappings.useNationalizedCharacterData();
         Type annType = (Type)property.getAnnotation(Type.class);
         if (annType != null) {
            this.setExplicitType(annType);
            type = this.explicitType;
         } else if ((this.key || !property.isAnnotationPresent(Temporal.class)) && (!this.key || !property.isAnnotationPresent(MapKeyTemporal.class))) {
            if (property.isAnnotationPresent(Lob.class)) {
               if (this.mappings.getReflectionManager().equals(returnedClassOrElement, Clob.class)) {
                  type = isNationalized ? StandardBasicTypes.NCLOB.getName() : StandardBasicTypes.CLOB.getName();
               } else if (this.mappings.getReflectionManager().equals(returnedClassOrElement, NClob.class)) {
                  type = StandardBasicTypes.NCLOB.getName();
               } else if (this.mappings.getReflectionManager().equals(returnedClassOrElement, Blob.class)) {
                  type = "blob";
               } else if (this.mappings.getReflectionManager().equals(returnedClassOrElement, String.class)) {
                  type = isNationalized ? StandardBasicTypes.MATERIALIZED_NCLOB.getName() : StandardBasicTypes.MATERIALIZED_CLOB.getName();
               } else if (this.mappings.getReflectionManager().equals(returnedClassOrElement, Character.class) && isArray) {
                  type = isNationalized ? CharacterArrayNClobType.class.getName() : CharacterArrayClobType.class.getName();
               } else if (this.mappings.getReflectionManager().equals(returnedClassOrElement, Character.TYPE) && isArray) {
                  type = isNationalized ? PrimitiveCharacterArrayNClobType.class.getName() : PrimitiveCharacterArrayClobType.class.getName();
               } else if (this.mappings.getReflectionManager().equals(returnedClassOrElement, Byte.class) && isArray) {
                  type = WrappedMaterializedBlobType.class.getName();
               } else if (this.mappings.getReflectionManager().equals(returnedClassOrElement, Byte.TYPE) && isArray) {
                  type = StandardBasicTypes.MATERIALIZED_BLOB.getName();
               } else if (this.mappings.getReflectionManager().toXClass(Serializable.class).isAssignableFrom(returnedClassOrElement)) {
                  type = SerializableToBlobType.class.getName();
               } else {
                  type = "blob";
               }

               this.explicitType = type;
            } else if (!this.key && property.isAnnotationPresent(Enumerated.class) || this.key && property.isAnnotationPresent(MapKeyEnumerated.class)) {
               Class attributeJavaType = this.mappings.getReflectionManager().toClass(returnedClassOrElement);
               if (!Enum.class.isAssignableFrom(attributeJavaType)) {
                  throw new AnnotationException(String.format("Attribute [%s.%s] was annotated as enumerated, but its java type is not an enum [%s]", declaringClassName, this.xproperty.getName(), attributeJavaType.getName()));
               }

               type = EnumType.class.getName();
               this.explicitType = type;
            } else if (isNationalized) {
               if (this.mappings.getReflectionManager().equals(returnedClassOrElement, String.class)) {
                  type = StringNVarcharType.INSTANCE.getName();
                  this.explicitType = type;
               } else if (this.mappings.getReflectionManager().equals(returnedClassOrElement, Character.class)) {
                  if (isArray) {
                     type = StringNVarcharType.INSTANCE.getName();
                  } else {
                     type = CharacterNCharType.INSTANCE.getName();
                  }

                  this.explicitType = type;
               }
            }
         } else {
            boolean isDate;
            if (this.mappings.getReflectionManager().equals(returnedClassOrElement, Date.class)) {
               isDate = true;
            } else {
               if (!this.mappings.getReflectionManager().equals(returnedClassOrElement, Calendar.class)) {
                  throw new AnnotationException("@Temporal should only be set on a java.util.Date or java.util.Calendar property: " + StringHelper.qualify(this.persistentClassName, this.propertyName));
               }

               isDate = false;
            }

            TemporalType temporalType = this.getTemporalType(property);
            switch (temporalType) {
               case DATE:
                  type = isDate ? "date" : "calendar_date";
                  break;
               case TIME:
                  type = "time";
                  if (!isDate) {
                     throw new NotYetImplementedException("Calendar cannot persist TIME only" + StringHelper.qualify(this.persistentClassName, this.propertyName));
                  }
                  break;
               case TIMESTAMP:
                  type = isDate ? "timestamp" : "calendar";
                  break;
               default:
                  throw new AssertionFailure("Unknown temporal type: " + temporalType);
            }

            this.explicitType = type;
         }

         if (this.columns == null) {
            throw new AssertionFailure("SimpleValueBinder.setColumns should be set before SimpleValueBinder.setType");
         } else {
            if ("".equals(type) && returnedClassOrElement.isEnum()) {
               type = EnumType.class.getName();
            }

            this.defaultType = BinderHelper.isEmptyAnnotationValue(type) ? this.returnedClassName : type;
         }
      }
   }

   private TemporalType getTemporalType(XProperty property) {
      if (this.key) {
         MapKeyTemporal ann = (MapKeyTemporal)property.getAnnotation(MapKeyTemporal.class);
         return ann.value();
      } else {
         Temporal ann = (Temporal)property.getAnnotation(Temporal.class);
         return ann.value();
      }
   }

   public void setExplicitType(String explicitType) {
      this.explicitType = explicitType;
   }

   public void setExplicitType(Type typeAnn) {
      if (typeAnn != null) {
         this.explicitType = typeAnn.type();
         this.typeParameters.clear();

         for(Parameter param : typeAnn.parameters()) {
            this.typeParameters.setProperty(param.name(), param.value());
         }
      }

   }

   public void setMappings(Mappings mappings) {
      this.mappings = mappings;
   }

   private void validate() {
      Ejb3Column.checkPropertyConsistency(this.columns, this.propertyName);
   }

   public SimpleValue make() {
      this.validate();
      LOG.debugf("building SimpleValue for %s", this.propertyName);
      if (this.table == null) {
         this.table = this.columns[0].getTable();
      }

      this.simpleValue = new SimpleValue(this.mappings, this.table);
      this.linkWithValue();
      boolean isInSecondPass = this.mappings.isInSecondPass();
      SetSimpleValueTypeSecondPass secondPass = new SetSimpleValueTypeSecondPass(this);
      if (!isInSecondPass) {
         this.mappings.addSecondPass(secondPass);
      } else {
         this.fillSimpleValue();
      }

      return this.simpleValue;
   }

   public void linkWithValue() {
      if (this.columns[0].isNameDeferred() && !this.mappings.isInSecondPass() && this.referencedEntityName != null) {
         this.mappings.addSecondPass(new PkDrivenByDefaultMapsIdSecondPass(this.referencedEntityName, (Ejb3JoinColumn[])this.columns, this.simpleValue));
      } else {
         for(Ejb3Column column : this.columns) {
            column.linkWithValue(this.simpleValue);
         }
      }

   }

   public void fillSimpleValue() {
      LOG.debugf("Setting SimpleValue typeName for %s", this.propertyName);
      String type;
      TypeDef typeDef;
      if (!BinderHelper.isEmptyAnnotationValue(this.explicitType)) {
         type = this.explicitType;
         typeDef = this.mappings.getTypeDef(type);
      } else {
         TypeDef implicitTypeDef = this.mappings.getTypeDef(this.returnedClassName);
         if (implicitTypeDef != null) {
            typeDef = implicitTypeDef;
            type = this.returnedClassName;
         } else {
            typeDef = this.mappings.getTypeDef(this.defaultType);
            type = this.defaultType;
         }
      }

      if (typeDef != null) {
         type = typeDef.getTypeClass();
         this.simpleValue.setTypeParameters(typeDef.getParameters());
      }

      if (this.typeParameters != null && this.typeParameters.size() != 0) {
         this.simpleValue.setTypeParameters(this.typeParameters);
      }

      this.simpleValue.setTypeName(type);
      if (this.persistentClassName != null) {
         this.simpleValue.setTypeUsingReflection(this.persistentClassName, this.propertyName);
      }

      if (!this.simpleValue.isTypeSpecified() && this.isVersion()) {
         this.simpleValue.setTypeName("integer");
      }

      if (this.timeStampVersionType != null) {
         this.simpleValue.setTypeName(this.timeStampVersionType);
      }

      if (this.simpleValue.getTypeName() != null && this.simpleValue.getTypeName().length() > 0 && this.simpleValue.getMappings().getTypeResolver().basic(this.simpleValue.getTypeName()) == null) {
         try {
            Class typeClass = ReflectHelper.classForName(this.simpleValue.getTypeName());
            if (typeClass != null && DynamicParameterizedType.class.isAssignableFrom(typeClass)) {
               Properties parameters = this.simpleValue.getTypeParameters();
               if (parameters == null) {
                  parameters = new Properties();
               }

               parameters.put("org.hibernate.type.ParameterType.dynamic", Boolean.toString(true));
               parameters.put("org.hibernate.type.ParameterType.returnedClass", this.returnedClassName);
               parameters.put("org.hibernate.type.ParameterType.primaryKey", Boolean.toString(this.key));
               parameters.put("org.hibernate.type.ParameterType.entityClass", this.persistentClassName);
               parameters.put("org.hibernate.type.ParameterType.xproperty", this.xproperty);
               parameters.put("org.hibernate.type.ParameterType.propertyName", this.xproperty.getName());
               parameters.put("org.hibernate.type.ParameterType.accessType", this.accessType.getType());
               this.simpleValue.setTypeParameters(parameters);
            }
         } catch (ClassNotFoundException cnfe) {
            throw new MappingException("Could not determine type for: " + this.simpleValue.getTypeName(), cnfe);
         }
      }

   }

   public void setKey(boolean key) {
      this.key = key;
   }

   public AccessType getAccessType() {
      return this.accessType;
   }

   public void setAccessType(AccessType accessType) {
      this.accessType = accessType;
   }
}
