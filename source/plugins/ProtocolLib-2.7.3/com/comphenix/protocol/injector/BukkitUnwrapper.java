package com.comphenix.protocol.injector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.google.common.primitives.Primitives;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitUnwrapper implements PacketConstructor.Unwrapper {
   public static final ReportType REPORT_ILLEGAL_ARGUMENT = new ReportType("Illegal argument.");
   public static final ReportType REPORT_SECURITY_LIMITATION = new ReportType("Security limitation.");
   public static final ReportType REPORT_CANNOT_FIND_UNWRAP_METHOD = new ReportType("Cannot find method.");
   public static final ReportType REPORT_CANNOT_READ_FIELD_HANDLE = new ReportType("Cannot read field 'handle'.");
   private static Map unwrapperCache = new ConcurrentHashMap();
   private final ErrorReporter reporter;

   public BukkitUnwrapper() {
      this(ProtocolLibrary.getErrorReporter());
   }

   public BukkitUnwrapper(ErrorReporter reporter) {
      this.reporter = reporter;
   }

   public Object unwrapItem(Object wrappedObject) {
      if (wrappedObject == null) {
         return null;
      } else {
         Class<?> currentClass = wrappedObject.getClass();
         if (wrappedObject instanceof Collection) {
            return this.handleCollection((Collection)wrappedObject);
         } else if (!Primitives.isWrapperType(currentClass) && !(wrappedObject instanceof String)) {
            PacketConstructor.Unwrapper specificUnwrapper = this.getSpecificUnwrapper(currentClass);
            return specificUnwrapper != null ? specificUnwrapper.unwrapItem(wrappedObject) : null;
         } else {
            return null;
         }
      }
   }

   private Object handleCollection(Collection wrappedObject) {
      Collection<Object> copy = (Collection)DefaultInstances.DEFAULT.getDefault(wrappedObject.getClass());
      if (copy == null) {
         return null;
      } else {
         for(Object element : wrappedObject) {
            copy.add(this.unwrapItem(element));
         }

         return copy;
      }
   }

   private PacketConstructor.Unwrapper getSpecificUnwrapper(Class type) {
      if (unwrapperCache.containsKey(type)) {
         return (PacketConstructor.Unwrapper)unwrapperCache.get(type);
      } else {
         try {
            final Method find = type.getMethod("getHandle");
            PacketConstructor.Unwrapper methodUnwrapper = new PacketConstructor.Unwrapper() {
               public Object unwrapItem(Object wrappedObject) {
                  try {
                     return find.invoke(wrappedObject);
                  } catch (IllegalArgumentException e) {
                     BukkitUnwrapper.this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(BukkitUnwrapper.REPORT_ILLEGAL_ARGUMENT).error(e).callerParam(wrappedObject, find));
                     return null;
                  } catch (IllegalAccessException var4) {
                     return null;
                  } catch (InvocationTargetException e) {
                     throw new RuntimeException("Minecraft error.", e);
                  }
               }
            };
            unwrapperCache.put(type, methodUnwrapper);
            return methodUnwrapper;
         } catch (SecurityException e) {
            this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_SECURITY_LIMITATION).error(e).callerParam(type));
         } catch (NoSuchMethodException e) {
            PacketConstructor.Unwrapper fieldUnwrapper = this.getFieldUnwrapper(type);
            if (fieldUnwrapper != null) {
               return fieldUnwrapper;
            }

            this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_FIND_UNWRAP_METHOD).error(e).callerParam(type));
         }

         return null;
      }
   }

   private PacketConstructor.Unwrapper getFieldUnwrapper(Class type) {
      final Field find = FieldUtils.getField(type, "handle", true);
      if (find != null) {
         PacketConstructor.Unwrapper fieldUnwrapper = new PacketConstructor.Unwrapper() {
            public Object unwrapItem(Object wrappedObject) {
               try {
                  return FieldUtils.readField(find, wrappedObject, true);
               } catch (IllegalAccessException e) {
                  BukkitUnwrapper.this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(BukkitUnwrapper.REPORT_CANNOT_READ_FIELD_HANDLE).error(e).callerParam(wrappedObject, find));
                  return null;
               }
            }
         };
         unwrapperCache.put(type, fieldUnwrapper);
         return fieldUnwrapper;
      } else {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_READ_FIELD_HANDLE).callerParam(find));
         return null;
      }
   }
}
