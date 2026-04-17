package org.ibex.nestedvm;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.ibex.nestedvm.util.ELF;
import org.ibex.nestedvm.util.Seekable;

public class Interpreter extends UnixRuntime implements Cloneable {
   private int[] registers;
   private int hi;
   private int lo;
   private int[] fpregs;
   private int fcsr;
   private int pc;
   public String image;
   private ELF.Symtab symtab;
   private int gp;
   private ELF.Symbol userInfo;
   private int entryPoint;
   private int heapStart;
   private HashMap sourceLineCache;

   private final void setFC(boolean var1) {
      this.fcsr = this.fcsr & -8388609 | (var1 ? 8388608 : 0);
   }

   private final int roundingMode() {
      return this.fcsr & 3;
   }

   private final double getDouble(int var1) {
      return Double.longBitsToDouble(((long)this.fpregs[var1 + 1] & 4294967295L) << 32 | (long)this.fpregs[var1] & 4294967295L);
   }

   private final void setDouble(int var1, double var2) {
      long var4 = Double.doubleToLongBits(var2);
      this.fpregs[var1 + 1] = (int)(var4 >>> 32);
      this.fpregs[var1] = (int)var4;
   }

   private final float getFloat(int var1) {
      return Float.intBitsToFloat(this.fpregs[var1]);
   }

   private final void setFloat(int var1, float var2) {
      this.fpregs[var1] = Float.floatToRawIntBits(var2);
   }

   protected void _execute() throws Runtime.ExecutionException {
      try {
         this.runSome();
      } catch (Runtime.ExecutionException var2) {
         var2.setLocation(toHex(this.pc) + ": " + this.sourceLine(this.pc));
         throw var2;
      }
   }

   protected Object clone() throws CloneNotSupportedException {
      Interpreter var1 = (Interpreter)super.clone();
      var1.registers = (int[])this.registers.clone();
      var1.fpregs = (int[])this.fpregs.clone();
      return var1;
   }

   private final int runSome() throws Runtime.FaultException, Runtime.ExecutionException {
      int var1 = 1 << this.pageShift >> 2;
      int[] var2 = this.registers;
      int[] var3 = this.fpregs;
      int var4 = this.pc;
      int var5 = var4 + 4;

      try {
         while(true) {
            int var6;
            try {
               var6 = this.readPages[var4 >>> this.pageShift][var4 >>> 2 & var1 - 1];
            } catch (RuntimeException var38) {
               if (var4 == -559038737) {
                  throw new Error("fell off cpu: r2: " + var2[2]);
               }

               var6 = this.memRead(var4);
            }

            int var7 = var6 >>> 26 & 255;
            int var8 = var6 >>> 21 & 31;
            int var9 = var6 >>> 16 & 31;
            int var10 = var6 >>> 16 & 31;
            int var11 = var6 >>> 11 & 31;
            int var12 = var6 >>> 11 & 31;
            int var13 = var6 >>> 6 & 31;
            int var14 = var6 >>> 6 & 31;
            int var15 = var6 & 63;
            int var16 = var6 & 67108863;
            int var17 = var6 & '\uffff';
            int var18 = var6 << 16 >> 16;
            var2[0] = 0;
            label486:
            switch (var7) {
               case 0:
                  switch (var15) {
                     case 0:
                        if (var6 != 0) {
                           var2[var11] = var2[var9] << var13;
                        }
                        break label486;
                     case 1:
                     case 5:
                     case 10:
                     case 11:
                     case 14:
                     case 15:
                     case 20:
                     case 21:
                     case 22:
                     case 23:
                     case 28:
                     case 29:
                     case 30:
                     case 31:
                     case 40:
                     case 41:
                     default:
                        throw new Runtime.ExecutionException("Illegal instruction 0/" + var15);
                     case 2:
                        var2[var11] = var2[var9] >>> var13;
                        break label486;
                     case 3:
                        var2[var11] = var2[var9] >> var13;
                        break label486;
                     case 4:
                        var2[var11] = var2[var9] << (var2[var8] & 31);
                        break label486;
                     case 6:
                        var2[var11] = var2[var9] >>> (var2[var8] & 31);
                        break label486;
                     case 7:
                        var2[var11] = var2[var9] >> (var2[var8] & 31);
                        break label486;
                     case 8:
                        int var63 = var2[var8];
                        var4 += 4;
                        var5 = var63;
                        continue;
                     case 9:
                        int var62 = var2[var8];
                        var4 += 4;
                        var2[var11] = var4 + 4;
                        var5 = var62;
                        continue;
                     case 12:
                        this.pc = var4;
                        var2[2] = this.syscall(var2[2], var2[4], var2[5], var2[6], var2[7], var2[8], var2[9]);
                        if (this.state != 0) {
                           this.pc = var5;
                           return 0;
                        }
                        break label486;
                     case 13:
                        throw new Runtime.ExecutionException("Break");
                     case 16:
                        var2[var11] = this.hi;
                        break label486;
                     case 17:
                        this.hi = var2[var8];
                        break label486;
                     case 18:
                        var2[var11] = this.lo;
                        break label486;
                     case 19:
                        this.lo = var2[var8];
                        break label486;
                     case 24:
                        long var76 = (long)var2[var8] * (long)var2[var9];
                        this.hi = (int)(var76 >>> 32);
                        this.lo = (int)var76;
                        break label486;
                     case 25:
                        long var75 = ((long)var2[var8] & 4294967295L) * ((long)var2[var9] & 4294967295L);
                        this.hi = (int)(var75 >>> 32);
                        this.lo = (int)var75;
                        break label486;
                     case 26:
                        this.hi = var2[var8] % var2[var9];
                        this.lo = var2[var8] / var2[var9];
                        break label486;
                     case 27:
                        if (var9 != 0) {
                           this.hi = (int)(((long)var2[var8] & 4294967295L) % ((long)var2[var9] & 4294967295L));
                           this.lo = (int)(((long)var2[var8] & 4294967295L) / ((long)var2[var9] & 4294967295L));
                        }
                        break label486;
                     case 32:
                        throw new Runtime.ExecutionException("ADD (add with oveflow trap) not suported");
                     case 33:
                        var2[var11] = var2[var8] + var2[var9];
                        break label486;
                     case 34:
                        throw new Runtime.ExecutionException("SUB (sub with oveflow trap) not suported");
                     case 35:
                        var2[var11] = var2[var8] - var2[var9];
                        break label486;
                     case 36:
                        var2[var11] = var2[var8] & var2[var9];
                        break label486;
                     case 37:
                        var2[var11] = var2[var8] | var2[var9];
                        break label486;
                     case 38:
                        var2[var11] = var2[var8] ^ var2[var9];
                        break label486;
                     case 39:
                        var2[var11] = ~(var2[var8] | var2[var9]);
                        break label486;
                     case 42:
                        var2[var11] = var2[var8] < var2[var9] ? 1 : 0;
                        break label486;
                     case 43:
                        var2[var11] = ((long)var2[var8] & 4294967295L) < ((long)var2[var9] & 4294967295L) ? 1 : 0;
                        break label486;
                  }
               case 1:
                  switch (var9) {
                     case 0:
                        if (var2[var8] < 0) {
                           var4 += 4;
                           int var61 = var4 + var18 * 4;
                           var5 = var61;
                           continue;
                        }
                        break label486;
                     case 1:
                        if (var2[var8] >= 0) {
                           var4 += 4;
                           int var60 = var4 + var18 * 4;
                           var5 = var60;
                           continue;
                        }
                        break label486;
                     case 16:
                        if (var2[var8] < 0) {
                           var4 += 4;
                           var2[31] = var4 + 4;
                           int var59 = var4 + var18 * 4;
                           var5 = var59;
                           continue;
                        }
                        break label486;
                     case 17:
                        if (var2[var8] >= 0) {
                           var4 += 4;
                           var2[31] = var4 + 4;
                           int var58 = var4 + var18 * 4;
                           var5 = var58;
                           continue;
                        }
                        break label486;
                     default:
                        throw new Runtime.ExecutionException("Illegal Instruction");
                  }
               case 2:
                  int var57 = var4 & -268435456 | var16 << 2;
                  var4 += 4;
                  var5 = var57;
                  continue;
               case 3:
                  int var56 = var4 & -268435456 | var16 << 2;
                  var4 += 4;
                  var2[31] = var4 + 4;
                  var5 = var56;
                  continue;
               case 4:
                  if (var2[var8] == var2[var9]) {
                     var4 += 4;
                     int var55 = var4 + var18 * 4;
                     var5 = var55;
                     continue;
                  }
                  break;
               case 5:
                  if (var2[var8] != var2[var9]) {
                     var4 += 4;
                     int var54 = var4 + var18 * 4;
                     var5 = var54;
                     continue;
                  }
                  break;
               case 6:
                  if (var2[var8] <= 0) {
                     var4 += 4;
                     int var53 = var4 + var18 * 4;
                     var5 = var53;
                     continue;
                  }
                  break;
               case 7:
                  if (var2[var8] > 0) {
                     var4 += 4;
                     int var52 = var4 + var18 * 4;
                     var5 = var52;
                     continue;
                  }
                  break;
               case 8:
                  var2[var9] = var2[var8] + var18;
                  break;
               case 9:
                  var2[var9] = var2[var8] + var18;
                  break;
               case 10:
                  var2[var9] = var2[var8] < var18 ? 1 : 0;
                  break;
               case 11:
                  var2[var9] = ((long)var2[var8] & 4294967295L) < ((long)var18 & 4294967295L) ? 1 : 0;
                  break;
               case 12:
                  var2[var9] = var2[var8] & var17;
                  break;
               case 13:
                  var2[var9] = var2[var8] | var17;
                  break;
               case 14:
                  var2[var9] = var2[var8] ^ var17;
                  break;
               case 15:
                  var2[var9] = var17 << 16;
                  break;
               case 16:
                  throw new Runtime.ExecutionException("TLB/Exception support not implemented");
               case 17:
                  boolean var22 = false;
                  String var23 = var22 ? this.sourceLine(var4) : "";
                  boolean var24 = var22 && (var23.indexOf("dtoa.c:51") >= 0 || var23.indexOf("dtoa.c:52") >= 0 || var23.indexOf("test.c") >= 0);
                  if (var8 > 8 && var24) {
                     System.out.println("               FP Op: " + var7 + "/" + var8 + "/" + var15 + " " + var23);
                  }

                  if (this.roundingMode() != 0 && var8 != 6 && (var8 != 16 && var8 != 17 || var15 != 36)) {
                     throw new Runtime.ExecutionException("Non-cvt.w.z operation attempted with roundingMode != round to nearest");
                  }

                  switch (var8) {
                     case 0:
                        var2[var9] = var3[var11];
                        break label486;
                     case 1:
                     case 3:
                     case 5:
                     case 7:
                     case 9:
                     case 10:
                     case 11:
                     case 12:
                     case 13:
                     case 14:
                     case 15:
                     case 18:
                     case 19:
                     default:
                        throw new Runtime.ExecutionException("Invalid Instruction 17/" + var8);
                     case 2:
                        if (var12 != 31) {
                           throw new Runtime.ExecutionException("FCR " + var12 + " unavailable");
                        }

                        var2[var9] = this.fcsr;
                        break label486;
                     case 4:
                        var3[var11] = var2[var9];
                        break label486;
                     case 6:
                        if (var12 != 31) {
                           throw new Runtime.ExecutionException("FCR " + var12 + " unavailable");
                        }

                        this.fcsr = var2[var9];
                        break label486;
                     case 8:
                        if ((this.fcsr & 8388608) != 0 == ((var6 >>> 16 & 1) != 0)) {
                           var4 += 4;
                           int var51 = var4 + var18 * 4;
                           var5 = var51;
                           continue;
                        }
                        break label486;
                     case 16:
                        switch (var15) {
                           case 0:
                              this.setFloat(var14, this.getFloat(var12) + this.getFloat(var10));
                              break label486;
                           case 1:
                              this.setFloat(var14, this.getFloat(var12) - this.getFloat(var10));
                              break label486;
                           case 2:
                              this.setFloat(var14, this.getFloat(var12) * this.getFloat(var10));
                              break label486;
                           case 3:
                              this.setFloat(var14, this.getFloat(var12) / this.getFloat(var10));
                              break label486;
                           case 5:
                              this.setFloat(var14, Math.abs(this.getFloat(var12)));
                              break label486;
                           case 6:
                              var3[var14] = var3[var12];
                              break label486;
                           case 7:
                              this.setFloat(var14, -this.getFloat(var12));
                              break label486;
                           case 33:
                              this.setDouble(var14, (double)this.getFloat(var12));
                              break label486;
                           case 36:
                              switch (this.roundingMode()) {
                                 case 0:
                                    var3[var14] = (int)Math.floor((double)(this.getFloat(var12) + 0.5F));
                                    break label486;
                                 case 1:
                                    var3[var14] = (int)this.getFloat(var12);
                                    break label486;
                                 case 2:
                                    var3[var14] = (int)Math.ceil((double)this.getFloat(var12));
                                    break label486;
                                 case 3:
                                    var3[var14] = (int)Math.floor((double)this.getFloat(var12));
                                 default:
                                    break label486;
                              }
                           case 50:
                              this.setFC(this.getFloat(var12) == this.getFloat(var10));
                              break label486;
                           case 60:
                              this.setFC(this.getFloat(var12) < this.getFloat(var10));
                              break label486;
                           case 62:
                              this.setFC(this.getFloat(var12) <= this.getFloat(var10));
                              break label486;
                           default:
                              throw new Runtime.ExecutionException("Invalid Instruction 17/" + var8 + "/" + var15 + " at " + this.sourceLine(var4));
                        }
                     case 17:
                        switch (var15) {
                           case 0:
                              this.setDouble(var14, this.getDouble(var12) + this.getDouble(var10));
                              break label486;
                           case 1:
                              if (var24) {
                                 System.out.println("f" + var14 + " = f" + var12 + " (" + this.getDouble(var12) + ") - f" + var10 + " (" + this.getDouble(var10) + ")");
                              }

                              this.setDouble(var14, this.getDouble(var12) - this.getDouble(var10));
                              break label486;
                           case 2:
                              if (var24) {
                                 System.out.println("f" + var14 + " = f" + var12 + " (" + this.getDouble(var12) + ") * f" + var10 + " (" + this.getDouble(var10) + ")");
                              }

                              this.setDouble(var14, this.getDouble(var12) * this.getDouble(var10));
                              if (var24) {
                                 System.out.println("f" + var14 + " = " + this.getDouble(var14));
                              }
                              break label486;
                           case 3:
                              this.setDouble(var14, this.getDouble(var12) / this.getDouble(var10));
                              break label486;
                           case 5:
                              this.setDouble(var14, Math.abs(this.getDouble(var12)));
                              break label486;
                           case 6:
                              var3[var14] = var3[var12];
                              var3[var14 + 1] = var3[var12 + 1];
                              break label486;
                           case 7:
                              this.setDouble(var14, -this.getDouble(var12));
                              break label486;
                           case 32:
                              this.setFloat(var14, (float)this.getDouble(var12));
                              break label486;
                           case 36:
                              if (var24) {
                                 System.out.println("CVT.W.D rm: " + this.roundingMode() + " f" + var12 + ":" + this.getDouble(var12));
                              }

                              switch (this.roundingMode()) {
                                 case 0:
                                    var3[var14] = (int)Math.floor(this.getDouble(var12) + (double)0.5F);
                                    break;
                                 case 1:
                                    var3[var14] = (int)this.getDouble(var12);
                                    break;
                                 case 2:
                                    var3[var14] = (int)Math.ceil(this.getDouble(var12));
                                    break;
                                 case 3:
                                    var3[var14] = (int)Math.floor(this.getDouble(var12));
                              }

                              if (var24) {
                                 System.out.println("CVT.W.D: f" + var14 + ":" + var3[var14]);
                              }
                              break label486;
                           case 50:
                              this.setFC(this.getDouble(var12) == this.getDouble(var10));
                              break label486;
                           case 60:
                              this.setFC(this.getDouble(var12) < this.getDouble(var10));
                              break label486;
                           case 62:
                              this.setFC(this.getDouble(var12) <= this.getDouble(var10));
                              break label486;
                           default:
                              throw new Runtime.ExecutionException("Invalid Instruction 17/" + var8 + "/" + var15 + " at " + this.sourceLine(var4));
                        }
                     case 20:
                        switch (var15) {
                           case 32:
                              this.setFloat(var14, (float)var3[var12]);
                              break label486;
                           case 33:
                              this.setDouble(var14, (double)var3[var12]);
                              break label486;
                           default:
                              throw new Runtime.ExecutionException("Invalid Instruction 17/" + var8 + "/" + var15 + " at " + this.sourceLine(var4));
                        }
                  }
               case 18:
               case 19:
                  throw new Runtime.ExecutionException("No coprocessor installed");
               case 20:
               case 21:
               case 22:
               case 23:
               case 24:
               case 25:
               case 26:
               case 27:
               case 28:
               case 29:
               case 30:
               case 31:
               case 39:
               case 44:
               case 45:
               case 47:
               case 50:
               case 51:
               case 52:
               case 53:
               case 54:
               case 55:
               default:
                  throw new Runtime.ExecutionException("Invalid Instruction: " + var7);
               case 32:
                  int var74 = var2[var8] + var18;

                  int var50;
                  try {
                     var50 = this.readPages[var74 >>> this.pageShift][var74 >>> 2 & var1 - 1];
                  } catch (RuntimeException var37) {
                     var50 = this.memRead(var74 & -4);
                  }

                  switch (var74 & 3) {
                     case 0:
                        var50 = var50 >>> 24 & 255;
                        break;
                     case 1:
                        var50 = var50 >>> 16 & 255;
                        break;
                     case 2:
                        var50 = var50 >>> 8 & 255;
                        break;
                     case 3:
                        var50 = var50 >>> 0 & 255;
                  }

                  if ((var50 & 128) != 0) {
                     var50 |= -256;
                  }

                  var2[var9] = var50;
                  break;
               case 33:
                  int var73 = var2[var8] + var18;

                  int var48;
                  try {
                     var48 = this.readPages[var73 >>> this.pageShift][var73 >>> 2 & var1 - 1];
                  } catch (RuntimeException var36) {
                     var48 = this.memRead(var73 & -4);
                  }

                  switch (var73 & 3) {
                     case 0:
                        var48 = var48 >>> 16 & '\uffff';
                        break;
                     case 2:
                        var48 = var48 >>> 0 & '\uffff';
                        break;
                     default:
                        throw new Runtime.ReadFaultException(var73);
                  }

                  if ((var48 & '耀') != 0) {
                     var48 |= -65536;
                  }

                  var2[var9] = var48;
                  break;
               case 34:
                  int var72 = var2[var8] + var18;

                  int var47;
                  try {
                     var47 = this.readPages[var72 >>> this.pageShift][var72 >>> 2 & var1 - 1];
                  } catch (RuntimeException var35) {
                     var47 = this.memRead(var72 & -4);
                  }

                  switch (var72 & 3) {
                     case 0:
                        var2[var9] = var2[var9] & 0 | var47 << 0;
                        break label486;
                     case 1:
                        var2[var9] = var2[var9] & 255 | var47 << 8;
                        break label486;
                     case 2:
                        var2[var9] = var2[var9] & '\uffff' | var47 << 16;
                        break label486;
                     case 3:
                        var2[var9] = var2[var9] & 16777215 | var47 << 24;
                     default:
                        break label486;
                  }
               case 35:
                  int var71 = var2[var8] + var18;

                  try {
                     var2[var9] = this.readPages[var71 >>> this.pageShift][var71 >>> 2 & var1 - 1];
                  } catch (RuntimeException var34) {
                     var2[var9] = this.memRead(var71);
                  }
                  break;
               case 36:
                  int var70 = var2[var8] + var18;

                  int var46;
                  try {
                     var46 = this.readPages[var70 >>> this.pageShift][var70 >>> 2 & var1 - 1];
                  } catch (RuntimeException var33) {
                     var46 = this.memRead(var70);
                  }

                  switch (var70 & 3) {
                     case 0:
                        var2[var9] = var46 >>> 24 & 255;
                        break label486;
                     case 1:
                        var2[var9] = var46 >>> 16 & 255;
                        break label486;
                     case 2:
                        var2[var9] = var46 >>> 8 & 255;
                        break label486;
                     case 3:
                        var2[var9] = var46 >>> 0 & 255;
                     default:
                        break label486;
                  }
               case 37:
                  int var69 = var2[var8] + var18;

                  int var45;
                  try {
                     var45 = this.readPages[var69 >>> this.pageShift][var69 >>> 2 & var1 - 1];
                  } catch (RuntimeException var32) {
                     var45 = this.memRead(var69 & -4);
                  }

                  switch (var69 & 3) {
                     case 0:
                        var2[var9] = var45 >>> 16 & '\uffff';
                        break label486;
                     case 2:
                        var2[var9] = var45 >>> 0 & '\uffff';
                        break label486;
                     default:
                        throw new Runtime.ReadFaultException(var69);
                  }
               case 38:
                  int var68 = var2[var8] + var18;

                  int var44;
                  try {
                     var44 = this.readPages[var68 >>> this.pageShift][var68 >>> 2 & var1 - 1];
                  } catch (RuntimeException var31) {
                     var44 = this.memRead(var68 & -4);
                  }

                  switch (var68 & 3) {
                     case 0:
                        var2[var9] = var2[var9] & -256 | var44 >>> 24;
                        break label486;
                     case 1:
                        var2[var9] = var2[var9] & -65536 | var44 >>> 16;
                        break label486;
                     case 2:
                        var2[var9] = var2[var9] & -16777216 | var44 >>> 8;
                        break label486;
                     case 3:
                        var2[var9] = var2[var9] & 0 | var44 >>> 0;
                     default:
                        break label486;
                  }
               case 40:
                  int var67 = var2[var8] + var18;

                  int var43;
                  try {
                     var43 = this.readPages[var67 >>> this.pageShift][var67 >>> 2 & var1 - 1];
                  } catch (RuntimeException var30) {
                     var43 = this.memRead(var67 & -4);
                  }

                  switch (var67 & 3) {
                     case 0:
                        var43 = var43 & 16777215 | (var2[var9] & 255) << 24;
                        break;
                     case 1:
                        var43 = var43 & -16711681 | (var2[var9] & 255) << 16;
                        break;
                     case 2:
                        var43 = var43 & -65281 | (var2[var9] & 255) << 8;
                        break;
                     case 3:
                        var43 = var43 & -256 | (var2[var9] & 255) << 0;
                  }

                  try {
                     this.writePages[var67 >>> this.pageShift][var67 >>> 2 & var1 - 1] = var43;
                  } catch (RuntimeException var29) {
                     this.memWrite(var67 & -4, var43);
                  }
                  break;
               case 41:
                  int var66 = var2[var8] + var18;

                  int var41;
                  try {
                     var41 = this.readPages[var66 >>> this.pageShift][var66 >>> 2 & var1 - 1];
                  } catch (RuntimeException var28) {
                     var41 = this.memRead(var66 & -4);
                  }

                  switch (var66 & 3) {
                     case 0:
                        var41 = var41 & '\uffff' | (var2[var9] & '\uffff') << 16;
                        break;
                     case 2:
                        var41 = var41 & -65536 | (var2[var9] & '\uffff') << 0;
                        break;
                     default:
                        throw new Runtime.WriteFaultException(var66);
                  }

                  try {
                     this.writePages[var66 >>> this.pageShift][var66 >>> 2 & var1 - 1] = var41;
                  } catch (RuntimeException var27) {
                     this.memWrite(var66 & -4, var41);
                  }
                  break;
               case 42:
                  int var65 = var2[var8] + var18;
                  int var40 = this.memRead(var65 & -4);
                  switch (var65 & 3) {
                     case 0:
                        var40 = var40 & 0 | var2[var9] >>> 0;
                        break;
                     case 1:
                        var40 = var40 & -16777216 | var2[var9] >>> 8;
                        break;
                     case 2:
                        var40 = var40 & -65536 | var2[var9] >>> 16;
                        break;
                     case 3:
                        var40 = var40 & -256 | var2[var9] >>> 24;
                  }

                  try {
                     this.writePages[var65 >>> this.pageShift][var65 >>> 2 & var1 - 1] = var40;
                  } catch (RuntimeException var26) {
                     this.memWrite(var65 & -4, var40);
                  }
                  break;
               case 43:
                  int var64 = var2[var8] + var18;

                  try {
                     this.writePages[var64 >>> this.pageShift][var64 >>> 2 & var1 - 1] = var2[var9];
                  } catch (RuntimeException var25) {
                     this.memWrite(var64 & -4, var2[var9]);
                  }
                  break;
               case 46:
                  int var21 = var2[var8] + var18;
                  int var20 = this.memRead(var21 & -4);
                  switch (var21 & 3) {
                     case 0:
                        var20 = var20 & 16777215 | var2[var9] << 24;
                        break;
                     case 1:
                        var20 = var20 & '\uffff' | var2[var9] << 16;
                        break;
                     case 2:
                        var20 = var20 & 255 | var2[var9] << 8;
                        break;
                     case 3:
                        var20 = var20 & 0 | var2[var9] << 0;
                  }

                  this.memWrite(var21 & -4, var20);
                  break;
               case 48:
                  var2[var9] = this.memRead(var2[var8] + var18);
                  break;
               case 49:
                  var3[var9] = this.memRead(var2[var8] + var18);
                  break;
               case 56:
                  this.memWrite(var2[var8] + var18, var2[var9]);
                  var2[var9] = 1;
                  break;
               case 57:
                  this.memWrite(var2[var8] + var18, var3[var9]);
            }

            var4 = var5;
            var5 += 4;
         }
      } catch (Runtime.ExecutionException var39) {
         this.pc = var4;
         throw var39;
      }
   }

   public int lookupSymbol(String var1) {
      ELF.Symbol var2 = this.symtab.getGlobalSymbol(var1);
      return var2 == null ? -1 : var2.addr;
   }

   protected int gp() {
      return this.gp;
   }

   protected int userInfoBae() {
      return this.userInfo == null ? 0 : this.userInfo.addr;
   }

   protected int userInfoSize() {
      return this.userInfo == null ? 0 : this.userInfo.size;
   }

   protected int entryPoint() {
      return this.entryPoint;
   }

   protected int heapStart() {
      return this.heapStart;
   }

   private void loadImage(Seekable var1) throws IOException {
      ELF var2 = new ELF(var1);
      this.symtab = var2.getSymtab();
      if (var2.header.type != 2) {
         throw new IOException("Binary is not an executable");
      } else if (var2.header.machine != 8) {
         throw new IOException("Binary is not for the MIPS I Architecture");
      } else if (var2.ident.data != 2) {
         throw new IOException("Binary is not big endian");
      } else {
         this.entryPoint = var2.header.entry;
         ELF.Symtab var3 = var2.getSymtab();
         if (var3 == null) {
            throw new IOException("No symtab in binary (did you strip it?)");
         } else {
            this.userInfo = var3.getGlobalSymbol("user_info");
            ELF.Symbol var4 = var3.getGlobalSymbol("_gp");
            if (var4 == null) {
               throw new IOException("NO _gp symbol!");
            } else {
               this.gp = var4.addr;
               this.entryPoint = var2.header.entry;
               ELF.PHeader[] var5 = var2.pheaders;
               int var6 = 0;
               int var7 = 1 << this.pageShift;
               int var8 = 1 << this.pageShift >> 2;

               for(int var9 = 0; var9 < var5.length; ++var9) {
                  ELF.PHeader var10 = var5[var9];
                  if (var10.type == 1) {
                     int var11 = var10.memsz;
                     int var12 = var10.filesz;
                     if (var11 != 0) {
                        if (var11 < 0) {
                           throw new IOException("pheader size too large");
                        }

                        int var13 = var10.vaddr;
                        if (var13 == 0) {
                           throw new IOException("pheader vaddr == 0x0");
                        }

                        var6 = max(var13 + var11, var6);

                        for(int var14 = 0; var14 < var11 + var7 - 1; var14 += var7) {
                           int var15 = var14 + var13 >>> this.pageShift;
                           if (this.readPages[var15] == null) {
                              this.readPages[var15] = new int[var8];
                           }

                           if (var10.writable()) {
                              this.writePages[var15] = this.readPages[var15];
                           }
                        }

                        if (var12 != 0) {
                           var12 &= -4;
                           DataInputStream var17 = new DataInputStream(var10.getInputStream());

                           do {
                              this.readPages[var13 >>> this.pageShift][var13 >>> 2 & var8 - 1] = var17.readInt();
                              var13 += 4;
                              var12 -= 4;
                           } while(var12 > 0);

                           var17.close();
                        }
                     }
                  }
               }

               this.heapStart = var6 + var7 - 1 & ~(var7 - 1);
            }
         }
      }
   }

   protected void setCPUState(Runtime.CPUState var1) {
      for(int var2 = 1; var2 < 32; ++var2) {
         this.registers[var2] = var1.r[var2];
      }

      for(int var3 = 0; var3 < 32; ++var3) {
         this.fpregs[var3] = var1.f[var3];
      }

      this.hi = var1.hi;
      this.lo = var1.lo;
      this.fcsr = var1.fcsr;
      this.pc = var1.pc;
   }

   protected void getCPUState(Runtime.CPUState var1) {
      for(int var2 = 1; var2 < 32; ++var2) {
         var1.r[var2] = this.registers[var2];
      }

      for(int var3 = 0; var3 < 32; ++var3) {
         var1.f[var3] = this.fpregs[var3];
      }

      var1.hi = this.hi;
      var1.lo = this.lo;
      var1.fcsr = this.fcsr;
      var1.pc = this.pc;
   }

   public Interpreter(Seekable var1) throws IOException {
      super(4096, 65536);
      this.registers = new int[32];
      this.fpregs = new int[32];
      this.loadImage(var1);
   }

   public Interpreter(String var1) throws IOException {
      this((Seekable)(new Seekable.File(var1, false)));
      this.image = var1;
   }

   public Interpreter(InputStream var1) throws IOException {
      this((Seekable)(new Seekable.InputStream(var1)));
   }

   public String sourceLine(int var1) {
      String var3 = (String)(this.sourceLineCache == null ? null : this.sourceLineCache.get(new Integer(var1)));
      if (var3 != null) {
         return var3;
      } else if (this.image == null) {
         return null;
      } else {
         try {
            Process var4 = java.lang.Runtime.getRuntime().exec(new String[]{"mips-unknown-elf-addr2line", "-e", this.image, toHex(var1)});
            var3 = (new BufferedReader(new InputStreamReader(var4.getInputStream()))).readLine();
            if (var3 == null) {
               return null;
            } else {
               while(var3.startsWith("../")) {
                  var3 = var3.substring(3);
               }

               if (this.sourceLineCache == null) {
                  this.sourceLineCache = new HashMap();
               }

               this.sourceLineCache.put(new Integer(var1), var3);
               return var3;
            }
         } catch (IOException var5) {
            return null;
         }
      }
   }

   public static void main(String[] var0) throws Exception {
      String var1 = var0[0];
      Interpreter var2 = new Interpreter(var1);
      java.lang.Runtime.getRuntime().addShutdownHook(new Thread(var2.new DebugShutdownHook()));
      int var3 = var2.run(var0);
      System.err.println("Exit status: " + var3);
      System.exit(var3);
   }

   public class DebugShutdownHook implements Runnable {
      public void run() {
         int var1 = Interpreter.this.pc;
         if (Interpreter.this.getState() == 0) {
            System.err.print("\nCPU Executing " + Runtime.toHex(var1) + ": " + Interpreter.this.sourceLine(var1) + "\n");
         }

      }
   }
}
