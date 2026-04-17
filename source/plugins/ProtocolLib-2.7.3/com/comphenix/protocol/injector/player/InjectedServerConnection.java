package com.comphenix.protocol.injector.player;

import com.comphenix.net.sf.cglib.proxy.Factory;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.server.AbstractInputStreamLookup;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftReflection;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Server;

public class InjectedServerConnection {
   public static final ReportType REPORT_CANNOT_FIND_MINECRAFT_SERVER = new ReportType("Cannot extract minecraft server from Bukkit.");
   public static final ReportType REPORT_CANNOT_INJECT_SERVER_CONNECTION = new ReportType("Cannot inject into server connection. Bad things will happen.");
   public static final ReportType REPORT_CANNOT_FIND_LISTENER_THREAD = new ReportType("Cannot find listener thread in MinecraftServer.");
   public static final ReportType REPORT_CANNOT_READ_LISTENER_THREAD = new ReportType("Unable to read the listener thread.");
   public static final ReportType REPORT_CANNOT_FIND_SERVER_CONNECTION = new ReportType("Unable to retrieve server connection");
   public static final ReportType REPORT_UNEXPECTED_THREAD_COUNT = new ReportType("Unexpected number of threads in %s: %s");
   public static final ReportType REPORT_CANNOT_FIND_NET_HANDLER_THREAD = new ReportType("Unable to retrieve net handler thread.");
   public static final ReportType REPORT_INSUFFICENT_THREAD_COUNT = new ReportType("Unable to inject %s lists in %s.");
   public static final ReportType REPORT_CANNOT_COPY_OLD_TO_NEW = new ReportType("Cannot copy old %s to new.");
   private static Field listenerThreadField;
   private static Field minecraftServerField;
   private static Field listField;
   private static Field dedicatedThreadField;
   private static Method serverConnectionMethod;
   private List listFields = new ArrayList();
   private List replacedLists = new ArrayList();
   private NetLoginInjector netLoginInjector;
   private AbstractInputStreamLookup socketInjector;
   private ServerSocketType socketType;
   private Server server;
   private ErrorReporter reporter;
   private boolean hasAttempted;
   private boolean hasSuccess;
   private Object minecraftServer = null;

   public InjectedServerConnection(ErrorReporter reporter, AbstractInputStreamLookup socketInjector, Server server, NetLoginInjector netLoginInjector) {
      this.reporter = reporter;
      this.server = server;
      this.socketInjector = socketInjector;
      this.netLoginInjector = netLoginInjector;
   }

   public static Object getServerConnection(ErrorReporter reporter, Server server) {
      try {
         InjectedServerConnection inspector = new InjectedServerConnection(reporter, (AbstractInputStreamLookup)null, server, (NetLoginInjector)null);
         return inspector.getServerConnection();
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Reflection error.", e);
      } catch (IllegalArgumentException e) {
         throw new FieldAccessException("Corrupt data.", e);
      } catch (InvocationTargetException e) {
         throw new FieldAccessException("Minecraft error.", e);
      }
   }

   public void initialize() {
      if (!this.hasAttempted) {
         this.hasAttempted = true;
         if (minecraftServerField == null) {
            minecraftServerField = FuzzyReflection.fromObject(this.server, true).getFieldByType("MinecraftServer", MinecraftReflection.getMinecraftServerClass());
         }

         try {
            this.minecraftServer = FieldUtils.readField((Field)minecraftServerField, (Object)this.server, true);
         } catch (IllegalAccessException var4) {
            this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_FIND_MINECRAFT_SERVER));
            return;
         }

         try {
            if (serverConnectionMethod == null) {
               serverConnectionMethod = FuzzyReflection.fromClass(minecraftServerField.getType()).getMethodByParameters("getServerConnection", MinecraftReflection.getServerConnectionClass(), new Class[0]);
            }

            this.socketType = InjectedServerConnection.ServerSocketType.SERVER_CONNECTION;
         } catch (IllegalArgumentException var2) {
            this.socketType = InjectedServerConnection.ServerSocketType.LISTENER_THREAD;
         } catch (Exception e) {
            this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_INJECT_SERVER_CONNECTION).error(e));
         }

      }
   }

   public ServerSocketType getServerSocketType() {
      return this.socketType;
   }

   public void injectList() {
      this.initialize();
      if (this.socketType == InjectedServerConnection.ServerSocketType.SERVER_CONNECTION) {
         this.injectServerConnection();
      } else {
         if (this.socketType != InjectedServerConnection.ServerSocketType.LISTENER_THREAD) {
            throw new IllegalStateException("Unable to detected server connection.");
         }

         this.injectListenerThread();
      }

   }

   private void initializeListenerField() {
      if (listenerThreadField == null) {
         listenerThreadField = FuzzyReflection.fromObject(this.minecraftServer).getFieldByType("networkListenThread", MinecraftReflection.getNetworkListenThreadClass());
      }

   }

   public Object getListenerThread() throws RuntimeException, IllegalAccessException {
      this.initialize();
      if (this.socketType == InjectedServerConnection.ServerSocketType.LISTENER_THREAD) {
         this.initializeListenerField();
         return listenerThreadField.get(this.minecraftServer);
      } else {
         return null;
      }
   }

   public Object getServerConnection() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      this.initialize();
      return this.socketType == InjectedServerConnection.ServerSocketType.SERVER_CONNECTION ? serverConnectionMethod.invoke(this.minecraftServer) : null;
   }

   private void injectListenerThread() {
      try {
         this.initializeListenerField();
      } catch (RuntimeException e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_FIND_LISTENER_THREAD).callerParam(this.minecraftServer).error(e));
         return;
      }

      Object listenerThread = null;

      try {
         listenerThread = this.getListenerThread();
      } catch (Exception e) {
         this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_READ_LISTENER_THREAD).error(e));
         return;
      }

      this.injectServerSocket(listenerThread);
      this.injectEveryListField(listenerThread, 1);
      this.hasSuccess = true;
   }

   private void injectServerConnection() {
      Object serverConnection = null;

      try {
         serverConnection = this.getServerConnection();
      } catch (Exception e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_FIND_SERVER_CONNECTION).callerParam(this.minecraftServer).error(e));
         return;
      }

      if (listField == null) {
         listField = FuzzyReflection.fromClass(serverConnectionMethod.getReturnType(), true).getFieldByType("netServerHandlerList", List.class);
      }

      if (dedicatedThreadField == null) {
         List<Field> matches = FuzzyReflection.fromObject(serverConnection, true).getFieldListByType(Thread.class);
         if (matches.size() != 1) {
            this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_UNEXPECTED_THREAD_COUNT).messageParam(serverConnection.getClass(), matches.size()));
         } else {
            dedicatedThreadField = (Field)matches.get(0);
         }
      }

      try {
         if (dedicatedThreadField != null) {
            Object dedicatedThread = FieldUtils.readField(dedicatedThreadField, serverConnection, true);
            this.injectServerSocket(dedicatedThread);
            this.injectEveryListField(dedicatedThread, 1);
         }
      } catch (IllegalAccessException e) {
         this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_FIND_NET_HANDLER_THREAD).error(e));
      }

      this.injectIntoList(serverConnection, listField);
      this.hasSuccess = true;
   }

   private void injectServerSocket(Object container) {
      this.socketInjector.inject(container);
   }

   private void injectEveryListField(Object container, int minimum) {
      List<Field> lists = FuzzyReflection.fromObject(container, true).getFieldListByType(List.class);

      for(Field list : lists) {
         this.injectIntoList(container, list);
      }

      if (lists.size() < minimum) {
         this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_INSUFFICENT_THREAD_COUNT).messageParam(minimum, container.getClass()));
      }

   }

   private void injectIntoList(Object instance, Field field) {
      VolatileField listFieldRef = new VolatileField(field, instance, true);
      List<Object> list = (List)listFieldRef.getValue();
      if (list instanceof ReplacedArrayList) {
         this.replacedLists.add((ReplacedArrayList)list);
      } else {
         ReplacedArrayList<Object> injectedList = this.createReplacement(list);
         this.replacedLists.add(injectedList);
         listFieldRef.setValue(injectedList);
         this.listFields.add(listFieldRef);
      }

   }

   private ReplacedArrayList createReplacement(List list) {
      return new ReplacedArrayList(list) {
         private static final long serialVersionUID = 2070481080950500367L;
         private final ObjectWriter writer = new ObjectWriter();

         protected void onReplacing(Object inserting, Object replacement) {
            if (!(inserting instanceof Factory)) {
               try {
                  this.writer.copyTo(inserting, replacement, inserting.getClass());
               } catch (Throwable e) {
                  InjectedServerConnection.this.reporter.reportDetailed(InjectedServerConnection.this, (Report.ReportBuilder)Report.newBuilder(InjectedServerConnection.REPORT_CANNOT_COPY_OLD_TO_NEW).messageParam(inserting).callerParam(inserting, replacement).error(e));
               }
            }

         }

         protected void onInserting(Object inserting) {
            if (MinecraftReflection.isLoginHandler(inserting)) {
               Object replaced = InjectedServerConnection.this.netLoginInjector.onNetLoginCreated(inserting);
               if (inserting != replaced) {
                  this.addMapping(inserting, replaced, true);
               }
            }

         }

         protected void onRemoved(Object removing) {
            if (MinecraftReflection.isLoginHandler(removing)) {
               InjectedServerConnection.this.netLoginInjector.cleanup(removing);
            }

         }
      };
   }

   public void replaceServerHandler(Object oldHandler, Object newHandler) {
      if (!this.hasAttempted) {
         this.injectList();
      }

      if (this.hasSuccess) {
         for(ReplacedArrayList replacedList : this.replacedLists) {
            replacedList.addMapping(oldHandler, newHandler);
         }
      }

   }

   public void revertServerHandler(Object oldHandler) {
      if (this.hasSuccess) {
         for(ReplacedArrayList replacedList : this.replacedLists) {
            replacedList.removeMapping(oldHandler);
         }
      }

   }

   public void cleanupAll() {
      if (this.replacedLists.size() > 0) {
         for(ReplacedArrayList replacedList : this.replacedLists) {
            replacedList.revertAll();
         }

         for(VolatileField field : this.listFields) {
            field.revertValue();
         }

         this.listFields.clear();
         this.replacedLists.clear();
      }

   }

   public static enum ServerSocketType {
      SERVER_CONNECTION,
      LISTENER_THREAD;
   }
}
