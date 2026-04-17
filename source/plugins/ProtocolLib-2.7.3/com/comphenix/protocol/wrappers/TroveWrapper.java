package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class TroveWrapper {
   private static volatile Class decorators;

   public static Map getDecoratedMap(@Nonnull Object troveMap) {
      Map<TKey, TValue> result = (Map)getDecorated(troveMap);
      return result;
   }

   public static Set getDecoratedSet(@Nonnull Object troveSet) {
      Set<TValue> result = (Set)getDecorated(troveSet);
      return result;
   }

   public static List getDecoratedList(@Nonnull Object troveList) {
      List<TValue> result = (List)getDecorated(troveList);
      return result;
   }

   private static Object getDecorated(@Nonnull Object trove) {
      if (trove == null) {
         throw new IllegalArgumentException("trove instance cannot be non-null.");
      } else {
         AbstractFuzzyMatcher<Class<?>> match = FuzzyMatchers.matchSuper(trove.getClass());
         if (decorators == null) {
            try {
               decorators = TroveWrapper.class.getClassLoader().loadClass("gnu.trove.TDecorators");
            } catch (ClassNotFoundException e) {
               throw new IllegalStateException("Cannot find TDecorators in Gnu Trove.", e);
            }
         }

         for(Method method : decorators.getMethods()) {
            Class<?>[] types = method.getParameterTypes();
            if (types.length == 1 && match.isMatch(types[0], (Object)null)) {
               try {
                  Object result = method.invoke((Object)null, trove);
                  if (result == null) {
                     throw new FieldAccessException("Wrapper returned NULL.");
                  }

                  return result;
               } catch (IllegalArgumentException e) {
                  throw new FieldAccessException("Cannot invoke wrapper method.", e);
               } catch (IllegalAccessException e) {
                  throw new FieldAccessException("Illegal access.", e);
               } catch (InvocationTargetException e) {
                  throw new FieldAccessException("Error in invocation.", e);
               }
            }
         }

         throw new IllegalArgumentException("Cannot find decorator for " + trove + " (" + trove.getClass() + ")");
      }
   }
}
