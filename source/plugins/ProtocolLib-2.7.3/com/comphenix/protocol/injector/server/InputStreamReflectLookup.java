package com.comphenix.protocol.injector.server;

import com.comphenix.protocol.concurrency.BlockingHashMap;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.collect.MapMaker;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Server;

class InputStreamReflectLookup extends AbstractInputStreamLookup {
   private static Field filteredInputField;
   private static final long DEFAULT_TIMEOUT = 2000L;
   protected BlockingHashMap addressLookup;
   protected ConcurrentMap inputLookup;
   private final long injectorTimeout;

   public InputStreamReflectLookup(ErrorReporter reporter, Server server) {
      this(reporter, server, 2000L);
   }

   public InputStreamReflectLookup(ErrorReporter reporter, Server server, long injectorTimeout) {
      super(reporter, server);
      this.addressLookup = new BlockingHashMap();
      this.inputLookup = (new MapMaker()).weakValues().makeMap();
      this.injectorTimeout = injectorTimeout;
   }

   public void inject(Object container) {
   }

   public SocketInjector peekSocketInjector(SocketAddress address) {
      try {
         return (SocketInjector)this.addressLookup.get(address, 0L, TimeUnit.MILLISECONDS);
      } catch (InterruptedException var3) {
         return null;
      }
   }

   public SocketInjector waitSocketInjector(SocketAddress address) {
      try {
         return (SocketInjector)this.addressLookup.get(address, this.injectorTimeout, TimeUnit.MILLISECONDS, true);
      } catch (InterruptedException e) {
         throw new IllegalStateException("Impossible exception occured!", e);
      }
   }

   public SocketInjector waitSocketInjector(Socket socket) {
      return this.waitSocketInjector(socket.getRemoteSocketAddress());
   }

   public SocketInjector waitSocketInjector(InputStream input) {
      try {
         SocketAddress address = this.waitSocketAddress(input);
         return address != null ? this.waitSocketInjector(address) : null;
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Cannot find or access socket field for " + input, e);
      }
   }

   private SocketAddress waitSocketAddress(InputStream stream) throws IllegalAccessException {
      if (stream instanceof FilterInputStream) {
         return this.waitSocketAddress(getInputStream((FilterInputStream)stream));
      } else {
         SocketAddress result = (SocketAddress)this.inputLookup.get(stream);
         if (result == null) {
            Socket socket = lookupSocket(stream);
            result = socket.getRemoteSocketAddress();
            this.inputLookup.put(stream, result);
         }

         return result;
      }
   }

   protected static InputStream getInputStream(FilterInputStream filtered) {
      if (filteredInputField == null) {
         filteredInputField = FuzzyReflection.fromClass(FilterInputStream.class, true).getFieldByType("in", InputStream.class);
      }

      InputStream current = filtered;

      try {
         while(current instanceof FilterInputStream) {
            current = (InputStream)FieldUtils.readField((Field)filteredInputField, (Object)current, true);
         }

         return current;
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Cannot access filtered input field.", e);
      }
   }

   public void setSocketInjector(SocketAddress address, SocketInjector injector) {
      if (address == null) {
         throw new IllegalArgumentException("address cannot be NULL");
      } else if (injector == null) {
         throw new IllegalArgumentException("injector cannot be NULL.");
      } else {
         SocketInjector previous = (SocketInjector)this.addressLookup.put(address, injector);
         if (previous != null) {
            this.onPreviousSocketOverwritten(previous, injector);
         }

      }
   }

   public void cleanupAll() {
   }

   private static Socket lookupSocket(InputStream stream) throws IllegalAccessException {
      if (stream instanceof FilterInputStream) {
         return lookupSocket(getInputStream((FilterInputStream)stream));
      } else {
         Field socketField = FuzzyReflection.fromObject(stream, true).getFieldByType("socket", Socket.class);
         return (Socket)FieldUtils.readField((Field)socketField, (Object)stream, true);
      }
   }
}
