package fr.neatmonster.nocheatplus.compat.blocks.blockbreak;

public class BlockBreakDurations {
   public static final BlockBreakEntry INSTANT_BREAK = new BlockBreakEntry() {
      public long getBreakingDuration() {
         return 0L;
      }
   };

   public interface BlockBreakEntry {
      long getBreakingDuration();
   }
}
