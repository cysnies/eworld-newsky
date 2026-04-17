package fr.neatmonster.nocheatplus.hooks;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import org.bukkit.entity.Player;

public interface NCPHook {
   String getHookName();

   String getHookVersion();

   boolean onCheckFailure(CheckType var1, Player var2, IViolationInfo var3);
}
