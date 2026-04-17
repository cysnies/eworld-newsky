package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ParameterHolder;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import java.util.ArrayList;

public abstract class ActionWithParameters extends Action {
   protected final ArrayList messageParts = new ArrayList();
   protected final String message;
   protected boolean needsParameters = true;

   public ActionWithParameters(String name, int delay, int repeat, String message) {
      super(name, delay, repeat);
      this.message = message;
      this.needsParameters = false;
      this.parseMessage(message);
   }

   protected String getMessage(ParameterHolder violationData) {
      StringBuilder log = new StringBuilder(150);

      for(Object part : this.messageParts) {
         if (part instanceof String) {
            log.append((String)part);
         } else if (part == null) {
            log.append("[???]");
         } else {
            try {
               log.append(violationData.getParameter((ParameterName)part));
            } catch (Exception var6) {
               log.append(part.toString());
            }
         }
      }

      return log.toString();
   }

   protected void parseMessage(String message) {
      String[] parts = message.split("\\[", 2);
      if (parts.length != 2) {
         this.messageParts.add(message);
      } else {
         String[] parts2 = parts[1].split("\\]", 2);
         if (parts2.length != 2) {
            this.messageParts.add(message);
         } else {
            ParameterName w = ParameterName.get(parts2[0].toLowerCase());
            if (w != null) {
               this.needsParameters = true;
               this.messageParts.add(parts[0]);
               this.messageParts.add(w);
               this.parseMessage(parts2[1]);
            } else {
               this.messageParts.add(message);
            }
         }
      }

   }

   public boolean needsParameters() {
      return this.needsParameters;
   }
}
