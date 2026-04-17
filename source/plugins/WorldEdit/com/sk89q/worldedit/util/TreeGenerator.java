package com.sk89q.worldedit.util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TreeGenerator {
   private static Random rand = new Random();
   private TreeType type;

   /** @deprecated */
   @Deprecated
   public TreeGenerator(TreeType type) {
      this.type = type;
   }

   public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
      return this.type.generate(editSession, pos);
   }

   private static void makePineTree(EditSession editSession, Vector basePos) throws MaxChangedBlocksException {
      int trunkHeight = (int)Math.floor(Math.random() * (double)2.0F) + 3;
      int height = (int)Math.floor(Math.random() * (double)5.0F) + 8;
      BaseBlock logBlock = new BaseBlock(17);
      BaseBlock leavesBlock = new BaseBlock(18);

      for(int i = 0; i < trunkHeight; ++i) {
         if (!editSession.setBlockIfAir(basePos.add(0, i, 0), logBlock)) {
            return;
         }
      }

      basePos = basePos.add(0, trunkHeight, 0);

      for(int i = 0; i < height; ++i) {
         editSession.setBlockIfAir(basePos.add(0, i, 0), logBlock);
         double chance = i != 0 && i != height - 1 ? (double)1.0F : 0.6;
         editSession.setChanceBlockIfAir(basePos.add(-1, i, 0), leavesBlock, chance);
         editSession.setChanceBlockIfAir(basePos.add(1, i, 0), leavesBlock, chance);
         editSession.setChanceBlockIfAir(basePos.add(0, i, -1), leavesBlock, chance);
         editSession.setChanceBlockIfAir(basePos.add(0, i, 1), leavesBlock, chance);
         editSession.setChanceBlockIfAir(basePos.add(1, i, 1), leavesBlock, chance);
         editSession.setChanceBlockIfAir(basePos.add(-1, i, 1), leavesBlock, chance);
         editSession.setChanceBlockIfAir(basePos.add(1, i, -1), leavesBlock, chance);
         editSession.setChanceBlockIfAir(basePos.add(-1, i, -1), leavesBlock, chance);
         if (i != 0 && i != height - 1) {
            for(int j = -2; j <= 2; ++j) {
               editSession.setChanceBlockIfAir(basePos.add(-2, i, j), leavesBlock, 0.6);
            }

            for(int j = -2; j <= 2; ++j) {
               editSession.setChanceBlockIfAir(basePos.add(2, i, j), leavesBlock, 0.6);
            }

            for(int j = -2; j <= 2; ++j) {
               editSession.setChanceBlockIfAir(basePos.add(j, i, -2), leavesBlock, 0.6);
            }

            for(int j = -2; j <= 2; ++j) {
               editSession.setChanceBlockIfAir(basePos.add(j, i, 2), leavesBlock, 0.6);
            }
         }
      }

      editSession.setBlockIfAir(basePos.add(0, height, 0), leavesBlock);
   }

   public static TreeType lookup(String type) {
      return TreeGenerator.TreeType.lookup(type);
   }

   public static enum TreeType {
      TREE("Regular tree", new String[]{"tree", "regular"}),
      BIG_TREE("Big tree", new String[]{"big", "bigtree"}),
      REDWOOD("Redwood", new String[]{"redwood", "sequoia", "sequoioideae"}),
      TALL_REDWOOD("Tall redwood", new String[]{"tallredwood", "tallsequoia", "tallsequoioideae"}),
      BIRCH("Birch", new String[]{"birch", "white", "whitebark"}),
      PINE("Pine", new String[]{"pine"}) {
         public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
            TreeGenerator.makePineTree(editSession, pos);
            return true;
         }
      },
      RANDOM_REDWOOD("Random redwood", new String[]{"randredwood", "randomredwood", "anyredwood"}) {
         public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
            TreeType[] choices = new TreeType[]{TreeGenerator.TreeType.REDWOOD, TreeGenerator.TreeType.TALL_REDWOOD};
            return choices[TreeGenerator.rand.nextInt(choices.length)].generate(editSession, pos);
         }
      },
      JUNGLE("Jungle", new String[]{"jungle"}),
      SHORT_JUNGLE("Short jungle", new String[]{"shortjungle", "smalljungle"}),
      JUNGLE_BUSH("Jungle bush", new String[]{"junglebush", "jungleshrub"}),
      RED_MUSHROOM("Red Mushroom", new String[]{"redmushroom", "redgiantmushroom"}),
      BROWN_MUSHROOM("Brown Mushroom", new String[]{"brownmushroom", "browngiantmushroom"}),
      SWAMP("Swamp", new String[]{"swamp", "swamptree"}),
      RANDOM("Random", new String[]{"rand", "random"}) {
         public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
            TreeType[] choices = new TreeType[]{TreeGenerator.TreeType.TREE, TreeGenerator.TreeType.BIG_TREE, TreeGenerator.TreeType.BIRCH, TreeGenerator.TreeType.REDWOOD, TreeGenerator.TreeType.TALL_REDWOOD, TreeGenerator.TreeType.PINE};
            return choices[TreeGenerator.rand.nextInt(choices.length)].generate(editSession, pos);
         }
      };

      private static final Map lookup = new HashMap();
      private final String name;
      private final String[] lookupKeys;

      private TreeType(String name, String... lookupKeys) {
         this.name = name;
         this.lookupKeys = lookupKeys;
      }

      public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
         return editSession.getWorld().generateTree(this, editSession, pos);
      }

      public String getName() {
         return this.name;
      }

      public static TreeType lookup(String name) {
         return (TreeType)lookup.get(name.toLowerCase());
      }

      static {
         for(TreeType type : EnumSet.allOf(TreeType.class)) {
            for(String key : type.lookupKeys) {
               lookup.put(key, type);
            }
         }

      }
   }
}
