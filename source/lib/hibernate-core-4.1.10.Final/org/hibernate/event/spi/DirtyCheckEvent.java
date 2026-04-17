package org.hibernate.event.spi;

public class DirtyCheckEvent extends FlushEvent {
   private boolean dirty;

   public DirtyCheckEvent(EventSource source) {
      super(source);
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void setDirty(boolean dirty) {
      this.dirty = dirty;
   }
}
