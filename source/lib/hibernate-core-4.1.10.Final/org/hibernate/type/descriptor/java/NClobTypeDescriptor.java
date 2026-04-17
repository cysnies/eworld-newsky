package org.hibernate.type.descriptor.java;

import java.io.Reader;
import java.io.Serializable;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;
import java.util.Comparator;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.NClobImplementer;
import org.hibernate.engine.jdbc.NClobProxy;
import org.hibernate.engine.jdbc.WrappedNClob;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;

public class NClobTypeDescriptor extends AbstractTypeDescriptor {
   public static final NClobTypeDescriptor INSTANCE = new NClobTypeDescriptor();

   public NClobTypeDescriptor() {
      super(NClob.class, NClobTypeDescriptor.NClobMutabilityPlan.INSTANCE);
   }

   public String toString(NClob value) {
      return DataHelper.extractString((Clob)value);
   }

   public NClob fromString(String string) {
      return NClobProxy.generateProxy(string);
   }

   public Comparator getComparator() {
      return IncomparableComparator.INSTANCE;
   }

   public int extractHashCode(NClob value) {
      return System.identityHashCode(value);
   }

   public boolean areEqual(NClob one, NClob another) {
      return one == another;
   }

   public Object unwrap(NClob value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else {
         try {
            if (CharacterStream.class.isAssignableFrom(type)) {
               if (NClobImplementer.class.isInstance(value)) {
                  return ((NClobImplementer)value).getUnderlyingStream();
               }

               return new CharacterStreamImpl(DataHelper.extractString(value.getCharacterStream()));
            }

            if (NClob.class.isAssignableFrom(type)) {
               NClob nclob = WrappedNClob.class.isInstance(value) ? ((WrappedNClob)value).getWrappedNClob() : value;
               return nclob;
            }
         } catch (SQLException e) {
            throw new HibernateException("Unable to access nclob stream", e);
         }

         throw this.unknownUnwrap(type);
      }
   }

   public NClob wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (NClob.class.isAssignableFrom(value.getClass())) {
         return options.getLobCreator().wrap((NClob)value);
      } else if (Reader.class.isAssignableFrom(value.getClass())) {
         Reader reader = (Reader)value;
         return options.getLobCreator().createNClob(DataHelper.extractString(reader));
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   public static class NClobMutabilityPlan implements MutabilityPlan {
      public static final NClobMutabilityPlan INSTANCE = new NClobMutabilityPlan();

      public boolean isMutable() {
         return false;
      }

      public NClob deepCopy(NClob value) {
         return value;
      }

      public Serializable disassemble(NClob value) {
         throw new UnsupportedOperationException("Clobs are not cacheable");
      }

      public NClob assemble(Serializable cached) {
         throw new UnsupportedOperationException("Clobs are not cacheable");
      }
   }
}
