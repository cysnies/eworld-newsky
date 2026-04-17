package fr.neatmonster.nocheatplus.components;

import org.bukkit.event.Listener;

public abstract class NCPListener implements Listener, ComponentWithName {
   public String getComponentName() {
      return "NoCheatPlus_Listener";
   }
}
