package org.hibernate.type.descriptor.java;

import java.io.Reader;
import java.io.Serializable;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Comparator;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.ClobImplementer;
import org.hibernate.engine.jdbc.ClobProxy;
import org.hibernate.engine.jdbc.WrappedClob;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;

public class ClobTypeDescriptor extends AbstractTypeDescriptor {
   public static final ClobTypeDescriptor INSTANCE = new ClobTypeDescriptor();

   public ClobTypeDescriptor() {
      super(Clob.class, ClobTypeDescriptor.ClobMutabilityPlan.INSTANCE);
   }

   public String toString(Clob value) {
      return DataHelper.extractString(value);
   }

   public Clob fromString(String string) {
      return ClobProxy.generateProxy(string);
   }

   public Comparator getComparator() {
      return IncomparableComparator.INSTANCE;
   }

   public int extractHashCode(Clob value) {
      return System.identityHashCode(value);
   }

   public boolean areEqual(Clob one, Clob another) {
      return one == another;
   }

   public Object unwrap(Clob value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else {
         try {
            if (CharacterStream.class.isAssignableFrom(type)) {
               if (ClobImplementer.class.isInstance(value)) {
                  return ((ClobImplementer)value).getUnderlyingStream();
               }

               return new CharacterStreamImpl(DataHelper.extractString(value.getCharacterStream()));
            }

            if (Clob.class.isAssignableFrom(type)) {
               Clob clob = WrappedClob.class.isInstance(value) ? ((WrappedClob)value).getWrappedClob() : value;
               return clob;
            }
         } catch (SQLException e) {
            throw new HibernateException("Unable to access clob stream", e);
         }

         throw this.unknownUnwrap(type);
      }
   }

   public Clob wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Clob.class.isAssignableFrom(value.getClass())) {
         return options.getLobCreator().wrap((Clob)value);
      } else if (Reader.class.isAssignableFrom(value.getClass())) {
         Reader reader = (Reader)value;
         return options.getLobCreator().createClob(DataHelper.extractString(reader));
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   public static class ClobMutabilityPlan implements MutabilityPlan {
      public static final ClobMutabilityPlan INSTANCE = new ClobMutabilityPlan();

      public boolean isMutable() {
         return false;
      }

      public Clob deepCopy(Clob value) {
         return value;
      }

      public Serializable disassemble(Clob value) {
         throw new UnsupportedOperationException("Clobs are not cacheable");
      }

      public Clob assemble(Serializable cached) {
         throw new UnsupportedOperationException("Clobs are not cacheable");
      }
   }
}
