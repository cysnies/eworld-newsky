package org.hibernate.metamodel.relational;

import java.io.Serializable;

public class Size implements Serializable {
   public static final int DEFAULT_LENGTH = 255;
   public static final int DEFAULT_PRECISION = 19;
   public static final int DEFAULT_SCALE = 2;
   private long length = 255L;
   private int precision = 19;
   private int scale = 2;
   private LobMultiplier lobMultiplier;

   public Size() {
      this.lobMultiplier = Size.LobMultiplier.NONE;
   }

   public Size(int precision, int scale, long length, LobMultiplier lobMultiplier) {
      this.lobMultiplier = Size.LobMultiplier.NONE;
      this.precision = precision;
      this.scale = scale;
      this.length = length;
      this.lobMultiplier = lobMultiplier;
   }

   public static Size precision(int precision) {
      return new Size(precision, -1, -1L, (LobMultiplier)null);
   }

   public static Size precision(int precision, int scale) {
      return new Size(precision, scale, -1L, (LobMultiplier)null);
   }

   public static Size length(long length) {
      return new Size(-1, -1, length, (LobMultiplier)null);
   }

   public static Size length(long length, LobMultiplier lobMultiplier) {
      return new Size(-1, -1, length, lobMultiplier);
   }

   public int getPrecision() {
      return this.precision;
   }

   public int getScale() {
      return this.scale;
   }

   public long getLength() {
      return this.length;
   }

   public LobMultiplier getLobMultiplier() {
      return this.lobMultiplier;
   }

   public void initialize(Size size) {
      this.precision = size.precision;
      this.scale = size.scale;
      this.length = size.length;
   }

   public void setPrecision(int precision) {
      this.precision = precision;
   }

   public void setScale(int scale) {
      this.scale = scale;
   }

   public void setLength(long length) {
      this.length = length;
   }

   public void setLobMultiplier(LobMultiplier lobMultiplier) {
      this.lobMultiplier = lobMultiplier;
   }

   public static enum LobMultiplier {
      NONE(1L),
      K(NONE.factor * 1024L),
      M(K.factor * 1024L),
      G(M.factor * 1024L);

      private long factor;

      private LobMultiplier(long factor) {
         this.factor = factor;
      }

      public long getFactor() {
         return this.factor;
      }
   }
}
