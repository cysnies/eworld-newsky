package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.components.IRemoveData;
import org.bukkit.entity.Player;

public interface CheckDataFactory extends IRemoveData {
   ICheckData getData(Player var1);

   ICheckData removeData(String var1);
}
