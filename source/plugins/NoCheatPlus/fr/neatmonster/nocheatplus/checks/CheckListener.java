package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.IHoldSubComponents;
import fr.neatmonster.nocheatplus.components.MCAccessHolder;
import fr.neatmonster.nocheatplus.components.NCPListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CheckListener extends NCPListener implements MCAccessHolder, IHoldSubComponents {
   protected final CheckType checkType;
   protected MCAccess mcAccess;
   protected final List queuedComponents;

   public CheckListener() {
      this((CheckType)null);
   }

   public CheckListener(CheckType checkType) {
      this.queuedComponents = new LinkedList();
      this.checkType = checkType;
      this.mcAccess = NCPAPIProvider.getNoCheatPlusAPI().getMCAccess();
   }

   public String getComponentName() {
      String part = super.getComponentName();
      return this.checkType == null ? part : part + "_" + this.checkType.name();
   }

   public void setMCAccess(MCAccess mcAccess) {
      this.mcAccess = mcAccess;
   }

   public MCAccess getMCAccess() {
      return this.mcAccess;
   }

   protected Check addCheck(Check check) {
      this.queuedComponents.add(check);
      return check;
   }

   public Collection getSubComponents() {
      List<Object> res = new ArrayList(this.queuedComponents);
      this.queuedComponents.clear();
      return res;
   }
}
