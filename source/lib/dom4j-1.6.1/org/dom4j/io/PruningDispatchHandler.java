package org.dom4j.io;

import org.dom4j.ElementPath;

class PruningDispatchHandler extends DispatchHandler {
   public void onEnd(ElementPath elementPath) {
      super.onEnd(elementPath);
      if (this.getActiveHandlerCount() == 0) {
         elementPath.getCurrent().detach();
      }

   }
}
