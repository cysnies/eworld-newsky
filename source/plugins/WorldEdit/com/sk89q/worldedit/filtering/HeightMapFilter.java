package com.sk89q.worldedit.filtering;

import java.awt.image.Kernel;

public class HeightMapFilter {
   private Kernel kernel;

   public HeightMapFilter(Kernel kernel) {
      this.kernel = kernel;
   }

   public HeightMapFilter(int kernelWidth, int kernelHeight, float[] kernelData) {
      this.kernel = new Kernel(kernelWidth, kernelHeight, kernelData);
   }

   public Kernel getKernel() {
      return this.kernel;
   }

   public void setKernel(Kernel kernel) {
      this.kernel = kernel;
   }

   public int[] filter(int[] inData, int width, int height) {
      int index = 0;
      float[] matrix = this.kernel.getKernelData((float[])null);
      int[] outData = new int[inData.length];
      int kh = this.kernel.getHeight();
      int kw = this.kernel.getWidth();
      int kox = this.kernel.getXOrigin();
      int koy = this.kernel.getYOrigin();

      for(int y = 0; y < height; ++y) {
         for(int x = 0; x < width; ++x) {
            float z = 0.0F;

            for(int ky = 0; ky < kh; ++ky) {
               int offsetY = y + ky - koy;
               if (offsetY < 0 || offsetY >= height) {
                  offsetY = y;
               }

               offsetY *= width;
               int matrixOffset = ky * kw;

               for(int kx = 0; kx < kw; ++kx) {
                  float f = matrix[matrixOffset + kx];
                  if (f != 0.0F) {
                     int offsetX = x + kx - kox;
                     if (offsetX < 0 || offsetX >= width) {
                        offsetX = x;
                     }

                     z += f * (float)inData[offsetY + offsetX];
                  }
               }
            }

            outData[index++] = (int)((double)z + (double)0.5F);
         }
      }

      return outData;
   }
}
