package com.sk89q.worldedit.filtering;

import java.awt.image.Kernel;

public class LinearKernel extends Kernel {
   public LinearKernel(int radius) {
      super(radius * 2 + 1, radius * 2 + 1, createKernel(radius));
   }

   private static float[] createKernel(int radius) {
      int diameter = radius * 2 + 1;
      float[] data = new float[diameter * diameter];

      for(int i = 0; i < data.length; data[i++] = 1.0F / (float)data.length) {
      }

      return data;
   }
}
