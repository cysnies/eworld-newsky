package sign;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface SignHandler {
   String getCreateFlag();

   String getCheckFlag();

   String getTip();

   boolean checkPer(Player var1, Block var2, String[] var3);

   String[] getShow(Player var1, Block var2, String[] var3);

   void onClick(Player var1, Block var2, String[] var3);

   boolean onBreak(Player var1, String[] var2);
}
