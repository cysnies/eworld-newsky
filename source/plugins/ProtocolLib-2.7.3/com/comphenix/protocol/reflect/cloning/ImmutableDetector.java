package com.comphenix.protocol.reflect.cloning;

import com.google.common.primitives.Primitives;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.security.PublicKey;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.SecretKey;

public class ImmutableDetector implements Cloner {
   private static final Class[] immutableClasses = new Class[]{StackTraceElement.class, BigDecimal.class, BigInteger.class, Locale.class, UUID.class, URL.class, URI.class, Inet4Address.class, Inet6Address.class, InetSocketAddress.class, SecretKey.class, PublicKey.class};

   public boolean canClone(Object source) {
      return source == null ? false : isImmutable(source.getClass());
   }

   public static boolean isImmutable(Class type) {
      if (type.isArray()) {
         return false;
      } else if (!Primitives.isWrapperType(type) && !String.class.equals(type)) {
         if (type.isEnum()) {
            return true;
         } else {
            for(Class clazz : immutableClasses) {
               if (clazz.equals(type)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }

   public Object clone(Object source) {
      return source;
   }
}
