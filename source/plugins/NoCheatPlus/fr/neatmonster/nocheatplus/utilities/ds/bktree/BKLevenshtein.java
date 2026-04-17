package fr.neatmonster.nocheatplus.utilities.ds.bktree;

public class BKLevenshtein extends BKModTree {
   public BKLevenshtein(BKModTree.NodeFactory nodeFactory, BKModTree.LookupEntryFactory resultFactory) {
      super(nodeFactory, resultFactory);
   }

   public int distance(char[] s, char[] t) {
      int n = s.length;
      int m = t.length;
      if (n == m) {
         for(int i = 0; i < n && s[i] == t[i]; ++i) {
            --m;
         }

         if (m == 0) {
            return 0;
         }

         m = n;
      }

      if (n == 0) {
         return m;
      } else if (m == 0) {
         return n;
      } else {
         if (n > m) {
            char[] tmp = s;
            s = t;
            t = tmp;
            n = m;
            m = tmp.length;
         }

         int[] p = new int[n + 1];
         int[] d = new int[n + 1];

         for(int i = 0; i <= n; p[i] = i++) {
         }

         for(int j = 1; j <= m; ++j) {
            char t_j = t[j - 1];
            d[0] = j;

            for(int var14 = 1; var14 <= n; ++var14) {
               int cost = s[var14 - 1] == t_j ? 0 : 1;
               d[var14] = Math.min(Math.min(d[var14 - 1] + 1, p[var14] + 1), p[var14 - 1] + cost);
            }

            int[] _d = p;
            p = d;
            d = _d;
         }

         return p[n];
      }
   }

   public static class LevenNode extends BKModTree.HashMapNode {
      public LevenNode(char[] value) {
         super(value);
      }
   }
}
