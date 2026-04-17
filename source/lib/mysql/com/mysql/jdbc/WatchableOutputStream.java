package com.mysql.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class WatchableOutputStream extends ByteArrayOutputStream {
   private OutputStreamWatcher watcher;

   public void close() throws IOException {
      super.close();
      if (this.watcher != null) {
         this.watcher.streamClosed(this);
      }

   }

   public void setWatcher(OutputStreamWatcher watcher) {
      this.watcher = watcher;
   }
}
