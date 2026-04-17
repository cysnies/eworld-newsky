package org.hibernate.type;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.HibernateException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import org.jboss.logging.Logger;

public class BasicTypeRegistry implements Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BasicTypeRegistry.class.getName());
   private Map registry = new ConcurrentHashMap(100, 0.75F, 1);
   private boolean locked = false;

   public BasicTypeRegistry() {
      this.register(BooleanType.INSTANCE);
      this.register(NumericBooleanType.INSTANCE);
      this.register(TrueFalseType.INSTANCE);
      this.register(YesNoType.INSTANCE);
      this.register(ByteType.INSTANCE);
      this.register(CharacterType.INSTANCE);
      this.register(ShortType.INSTANCE);
      this.register(IntegerType.INSTANCE);
      this.register(LongType.INSTANCE);
      this.register(FloatType.INSTANCE);
      this.register(DoubleType.INSTANCE);
      this.register(BigDecimalType.INSTANCE);
      this.register(BigIntegerType.INSTANCE);
      this.register(StringType.INSTANCE);
      this.register(StringNVarcharType.INSTANCE);
      this.register(CharacterNCharType.INSTANCE);
      this.register(UrlType.INSTANCE);
      this.register(DateType.INSTANCE);
      this.register(TimeType.INSTANCE);
      this.register(TimestampType.INSTANCE);
      this.register(DbTimestampType.INSTANCE);
      this.register(CalendarType.INSTANCE);
      this.register(CalendarDateType.INSTANCE);
      this.register(LocaleType.INSTANCE);
      this.register(CurrencyType.INSTANCE);
      this.register(TimeZoneType.INSTANCE);
      this.register(ClassType.INSTANCE);
      this.register(UUIDBinaryType.INSTANCE);
      this.register(UUIDCharType.INSTANCE);
      this.register(PostgresUUIDType.INSTANCE);
      this.register(BinaryType.INSTANCE);
      this.register(WrapperBinaryType.INSTANCE);
      this.register(ImageType.INSTANCE);
      this.register(CharArrayType.INSTANCE);
      this.register(CharacterArrayType.INSTANCE);
      this.register(TextType.INSTANCE);
      this.register(NTextType.INSTANCE);
      this.register(BlobType.INSTANCE);
      this.register(MaterializedBlobType.INSTANCE);
      this.register(ClobType.INSTANCE);
      this.register(NClobType.INSTANCE);
      this.register(MaterializedClobType.INSTANCE);
      this.register(MaterializedNClobType.INSTANCE);
      this.register(SerializableType.INSTANCE);
      this.register(ObjectType.INSTANCE);
      this.register(new AdaptedImmutableType(DateType.INSTANCE));
      this.register(new AdaptedImmutableType(TimeType.INSTANCE));
      this.register(new AdaptedImmutableType(TimestampType.INSTANCE));
      this.register(new AdaptedImmutableType(DbTimestampType.INSTANCE));
      this.register(new AdaptedImmutableType(CalendarType.INSTANCE));
      this.register(new AdaptedImmutableType(CalendarDateType.INSTANCE));
      this.register(new AdaptedImmutableType(BinaryType.INSTANCE));
      this.register(new AdaptedImmutableType(SerializableType.INSTANCE));
   }

   private BasicTypeRegistry(Map registeredTypes) {
      this.registry.putAll(registeredTypes);
      this.locked = true;
   }

   public void register(BasicType type) {
      if (this.locked) {
         throw new HibernateException("Can not alter TypeRegistry at this time");
      } else if (type == null) {
         throw new HibernateException("Type to register cannot be null");
      } else {
         if (type.getRegistrationKeys() == null || type.getRegistrationKeys().length == 0) {
            LOG.typeDefinedNoRegistrationKeys(type);
         }

         for(String key : type.getRegistrationKeys()) {
            if (key != null) {
               LOG.debugf("Adding type registration %s -> %s", key, type);
               Type old = (Type)this.registry.put(key, type);
               if (old != null && old != type) {
                  LOG.typeRegistrationOverridesPrevious(key, old);
               }
            }
         }

      }
   }

   public void register(UserType type, String[] keys) {
      this.register(new CustomType(type, keys));
   }

   public void register(CompositeUserType type, String[] keys) {
      this.register(new CompositeCustomType(type, keys));
   }

   public BasicType getRegisteredType(String key) {
      return (BasicType)this.registry.get(key);
   }

   public BasicTypeRegistry shallowCopy() {
      return new BasicTypeRegistry(this.registry);
   }
}
