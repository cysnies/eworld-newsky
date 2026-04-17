package com.comphenix.net.sf.cglib.asm;

import java.io.IOException;
import java.io.InputStream;

public class ClassReader {
   public static final int SKIP_CODE = 1;
   public static final int SKIP_DEBUG = 2;
   public static final int SKIP_FRAMES = 4;
   public static final int EXPAND_FRAMES = 8;
   public final byte[] b;
   private final int[] a;
   private final String[] c;
   private final int d;
   public final int header;

   public ClassReader(byte[] var1) {
      this(var1, 0, var1.length);
   }

   public ClassReader(byte[] var1, int var2, int var3) {
      this.b = var1;
      this.a = new int[this.readUnsignedShort(var2 + 8)];
      int var4 = this.a.length;
      this.c = new String[var4];
      int var5 = 0;
      int var6 = var2 + 10;

      for(int var7 = 1; var7 < var4; ++var7) {
         this.a[var7] = var6 + 1;
         int var8;
         switch (var1[var6]) {
            case 1:
               var8 = 3 + this.readUnsignedShort(var6 + 1);
               if (var8 > var5) {
                  var5 = var8;
               }
               break;
            case 2:
            case 7:
            case 8:
            default:
               var8 = 3;
               break;
            case 3:
            case 4:
            case 9:
            case 10:
            case 11:
            case 12:
               var8 = 5;
               break;
            case 5:
            case 6:
               var8 = 9;
               ++var7;
         }

         var6 += var8;
      }

      this.d = var5;
      this.header = var6;
   }

   public int getAccess() {
      return this.readUnsignedShort(this.header);
   }

   public String getClassName() {
      return this.readClass(this.header + 2, new char[this.d]);
   }

   public String getSuperName() {
      int var1 = this.a[this.readUnsignedShort(this.header + 4)];
      return var1 == 0 ? null : this.readUTF8(var1, new char[this.d]);
   }

   public String[] getInterfaces() {
      int var1 = this.header + 6;
      int var2 = this.readUnsignedShort(var1);
      String[] var3 = new String[var2];
      if (var2 > 0) {
         char[] var4 = new char[this.d];

         for(int var5 = 0; var5 < var2; ++var5) {
            var1 += 2;
            var3[var5] = this.readClass(var1, var4);
         }
      }

      return var3;
   }

   void a(ClassWriter var1) {
      char[] var2 = new char[this.d];
      int var3 = this.a.length;
      Item[] var4 = new Item[var3];

      for(int var5 = 1; var5 < var3; ++var5) {
         int var6 = this.a[var5];
         byte var7 = this.b[var6 - 1];
         Item var8 = new Item(var5);
         switch (var7) {
            case 1:
               String var10 = this.c[var5];
               if (var10 == null) {
                  var6 = this.a[var5];
                  var10 = this.c[var5] = this.a(var6 + 2, this.readUnsignedShort(var6), var2);
               }

               var8.a(var7, var10, (String)null, (String)null);
               break;
            case 2:
            case 7:
            case 8:
            default:
               var8.a(var7, this.readUTF8(var6, var2), (String)null, (String)null);
               break;
            case 3:
               var8.a(this.readInt(var6));
               break;
            case 4:
               var8.a(Float.intBitsToFloat(this.readInt(var6)));
               break;
            case 5:
               var8.a(this.readLong(var6));
               ++var5;
               break;
            case 6:
               var8.a(Double.longBitsToDouble(this.readLong(var6)));
               ++var5;
               break;
            case 9:
            case 10:
            case 11:
               int var9 = this.a[this.readUnsignedShort(var6 + 2)];
               var8.a(var7, this.readClass(var6, var2), this.readUTF8(var9, var2), this.readUTF8(var9 + 2, var2));
               break;
            case 12:
               var8.a(var7, this.readUTF8(var6, var2), this.readUTF8(var6 + 2, var2), (String)null);
         }

         int var13 = var8.j % var4.length;
         var8.k = var4[var13];
         var4[var13] = var8;
      }

      int var11 = this.a[1] - 1;
      var1.d.putByteArray(this.b, var11, this.header - var11);
      var1.e = var4;
      var1.f = (int)((double)0.75F * (double)var3);
      var1.c = var3;
   }

   public ClassReader(InputStream var1) throws IOException {
      this(a(var1));
   }

   public ClassReader(String var1) throws IOException {
      this(ClassLoader.getSystemResourceAsStream(var1.replace('.', '/') + ".class"));
   }

   private static byte[] a(InputStream var0) throws IOException {
      if (var0 == null) {
         throw new IOException("Class not found");
      } else {
         byte[] var1 = new byte[var0.available()];
         int var2 = 0;

         while(true) {
            int var3 = var0.read(var1, var2, var1.length - var2);
            if (var3 == -1) {
               if (var2 < var1.length) {
                  byte[] var6 = new byte[var2];
                  System.arraycopy(var1, 0, var6, 0, var2);
                  var1 = var6;
               }

               return var1;
            }

            var2 += var3;
            if (var2 == var1.length) {
               int var4 = var0.read();
               if (var4 < 0) {
                  return var1;
               }

               byte[] var5 = new byte[var1.length + 1000];
               System.arraycopy(var1, 0, var5, 0, var2);
               var5[var2++] = (byte)var4;
               var1 = var5;
            }
         }
      }
   }

   public void accept(ClassVisitor var1, int var2) {
      this.accept(var1, new Attribute[0], var2);
   }

   public void accept(ClassVisitor var1, Attribute[] var2, int var3) {
      byte[] var4 = this.b;
      char[] var5 = new char[this.d];
      int var6 = 0;
      int var7 = 0;
      Attribute var8 = null;
      int var9 = this.header;
      int var10 = this.readUnsignedShort(var9);
      String var11 = this.readClass(var9 + 2, var5);
      int var12 = this.a[this.readUnsignedShort(var9 + 4)];
      String var13 = var12 == 0 ? null : this.readUTF8(var12, var5);
      String[] var14 = new String[this.readUnsignedShort(var9 + 6)];
      int var15 = 0;
      var9 += 8;

      for(int var16 = 0; var16 < var14.length; ++var16) {
         var14[var16] = this.readClass(var9, var5);
         var9 += 2;
      }

      boolean var17 = (var3 & 1) != 0;
      boolean var18 = (var3 & 2) != 0;
      boolean var19 = (var3 & 8) != 0;
      int var109 = this.readUnsignedShort(var9);

      for(var12 = var9 + 2; var109 > 0; --var109) {
         int var20 = this.readUnsignedShort(var12 + 6);

         for(var12 += 8; var20 > 0; --var20) {
            var12 += 6 + this.readInt(var12 + 2);
         }
      }

      var109 = this.readUnsignedShort(var12);

      for(var12 += 2; var109 > 0; --var109) {
         int var116 = this.readUnsignedShort(var12 + 6);

         for(var12 += 8; var116 > 0; --var116) {
            var12 += 6 + this.readInt(var12 + 2);
         }
      }

      String var21 = null;
      String var22 = null;
      String var23 = null;
      String var24 = null;
      String var25 = null;
      String var26 = null;
      var109 = this.readUnsignedShort(var12);

      for(int var85 = var12 + 2; var109 > 0; --var109) {
         String var27 = this.readUTF8(var85, var5);
         if ("SourceFile".equals(var27)) {
            var22 = this.readUTF8(var85 + 6, var5);
         } else if ("InnerClasses".equals(var27)) {
            var15 = var85 + 6;
         } else if ("EnclosingMethod".equals(var27)) {
            var24 = this.readClass(var85 + 6, var5);
            int var28 = this.readUnsignedShort(var85 + 8);
            if (var28 != 0) {
               var25 = this.readUTF8(this.a[var28], var5);
               var26 = this.readUTF8(this.a[var28] + 2, var5);
            }
         } else if ("Signature".equals(var27)) {
            var21 = this.readUTF8(var85 + 6, var5);
         } else if ("RuntimeVisibleAnnotations".equals(var27)) {
            var6 = var85 + 6;
         } else if ("Deprecated".equals(var27)) {
            var10 |= 131072;
         } else if ("Synthetic".equals(var27)) {
            var10 |= 266240;
         } else if ("SourceDebugExtension".equals(var27)) {
            int var141 = this.readInt(var85 + 2);
            var23 = this.a(var85 + 6, var141, new char[var141]);
         } else if ("RuntimeInvisibleAnnotations".equals(var27)) {
            var7 = var85 + 6;
         } else {
            Attribute var29 = this.a(var2, var27, var85 + 6, this.readInt(var85 + 2), var5, -1, (Label[])null);
            if (var29 != null) {
               var29.a = var8;
               var8 = var29;
            }
         }

         var85 += 6 + this.readInt(var85 + 2);
      }

      var1.visit(this.readInt(4), var10, var11, var21, var13, var14);
      if (!var18 && (var22 != null || var23 != null)) {
         var1.visitSource(var22, var23);
      }

      if (var24 != null) {
         var1.visitOuterClass(var24, var25, var26);
      }

      for(int var112 = 1; var112 >= 0; --var112) {
         var12 = var112 == 0 ? var7 : var6;
         if (var12 != 0) {
            int var117 = this.readUnsignedShort(var12);

            for(int var87 = var12 + 2; var117 > 0; --var117) {
               var87 = this.a(var87 + 2, var5, true, var1.visitAnnotation(this.readUTF8(var87, var5), var112 != 0));
            }
         }
      }

      while(var8 != null) {
         Attribute var144 = var8.a;
         var8.a = null;
         var1.visitAttribute(var8);
         var8 = var144;
      }

      if (var15 != 0) {
         var109 = this.readUnsignedShort(var15);

         for(int var99 = var15 + 2; var109 > 0; --var109) {
            var1.visitInnerClass(this.readUnsignedShort(var99) == 0 ? null : this.readClass(var99, var5), this.readUnsignedShort(var99 + 2) == 0 ? null : this.readClass(var99 + 2, var5), this.readUnsignedShort(var99 + 4) == 0 ? null : this.readUTF8(var99 + 4, var5), this.readUnsignedShort(var99 + 6));
            var99 += 8;
         }
      }

      var109 = this.readUnsignedShort(var9);

      for(var9 += 2; var109 > 0; --var109) {
         var10 = this.readUnsignedShort(var9);
         var11 = this.readUTF8(var9 + 2, var5);
         String var30 = this.readUTF8(var9 + 4, var5);
         int var142 = 0;
         var21 = null;
         var6 = 0;
         var7 = 0;
         var8 = null;
         int var118 = this.readUnsignedShort(var9 + 6);

         for(var9 += 8; var118 > 0; --var118) {
            String var138 = this.readUTF8(var9, var5);
            if ("ConstantValue".equals(var138)) {
               var142 = this.readUnsignedShort(var9 + 6);
            } else if ("Signature".equals(var138)) {
               var21 = this.readUTF8(var9 + 6, var5);
            } else if ("Deprecated".equals(var138)) {
               var10 |= 131072;
            } else if ("Synthetic".equals(var138)) {
               var10 |= 266240;
            } else if ("RuntimeVisibleAnnotations".equals(var138)) {
               var6 = var9 + 6;
            } else if ("RuntimeInvisibleAnnotations".equals(var138)) {
               var7 = var9 + 6;
            } else {
               Attribute var145 = this.a(var2, var138, var9 + 6, this.readInt(var9 + 2), var5, -1, (Label[])null);
               if (var145 != null) {
                  var145.a = var8;
                  var8 = var145;
               }
            }

            var9 += 6 + this.readInt(var9 + 2);
         }

         FieldVisitor var31 = var1.visitField(var10, var11, var30, var21, var142 == 0 ? null : this.readConst(var142, var5));
         if (var31 != null) {
            for(int var119 = 1; var119 >= 0; --var119) {
               var12 = var119 == 0 ? var7 : var6;
               if (var12 != 0) {
                  int var32 = this.readUnsignedShort(var12);

                  for(int var89 = var12 + 2; var32 > 0; --var32) {
                     var89 = this.a(var89 + 2, var5, true, var31.visitAnnotation(this.readUTF8(var89, var5), var119 != 0));
                  }
               }
            }

            while(var8 != null) {
               Attribute var146 = var8.a;
               var8.a = null;
               var31.visitAttribute(var8);
               var8 = var146;
            }

            var31.visitEnd();
         }
      }

      var109 = this.readUnsignedShort(var9);

      for(var9 += 2; var109 > 0; --var109) {
         int var143 = var9 + 6;
         var10 = this.readUnsignedShort(var9);
         var11 = this.readUTF8(var9 + 2, var5);
         String var151 = this.readUTF8(var9 + 4, var5);
         var21 = null;
         var6 = 0;
         var7 = 0;
         int var152 = 0;
         int var33 = 0;
         int var34 = 0;
         var8 = null;
         var12 = 0;
         var15 = 0;
         int var120 = this.readUnsignedShort(var9 + 6);

         for(var9 += 8; var120 > 0; --var120) {
            String var139 = this.readUTF8(var9, var5);
            int var35 = this.readInt(var9 + 2);
            var9 += 6;
            if ("Code".equals(var139)) {
               if (!var17) {
                  var12 = var9;
               }
            } else if ("Exceptions".equals(var139)) {
               var15 = var9;
            } else if ("Signature".equals(var139)) {
               var21 = this.readUTF8(var9, var5);
            } else if ("Deprecated".equals(var139)) {
               var10 |= 131072;
            } else if ("RuntimeVisibleAnnotations".equals(var139)) {
               var6 = var9;
            } else if ("AnnotationDefault".equals(var139)) {
               var152 = var9;
            } else if ("Synthetic".equals(var139)) {
               var10 |= 266240;
            } else if ("RuntimeInvisibleAnnotations".equals(var139)) {
               var7 = var9;
            } else if ("RuntimeVisibleParameterAnnotations".equals(var139)) {
               var33 = var9;
            } else if ("RuntimeInvisibleParameterAnnotations".equals(var139)) {
               var34 = var9;
            } else {
               Attribute var147 = this.a(var2, var139, var9, var35, var5, -1, (Label[])null);
               if (var147 != null) {
                  var147.a = var8;
                  var8 = var147;
               }
            }

            var9 += var35;
         }

         String[] var164;
         if (var15 == 0) {
            var164 = null;
         } else {
            var164 = new String[this.readUnsignedShort(var15)];
            var15 += 2;

            for(int var121 = 0; var121 < var164.length; ++var121) {
               var164[var121] = this.readClass(var15, var5);
               var15 += 2;
            }
         }

         MethodVisitor var36 = var1.visitMethod(var10, var11, var151, var21, var164);
         if (var36 != null) {
            if (var36 instanceof MethodWriter) {
               MethodWriter var37 = (MethodWriter)var36;
               if (var37.b.J == this && var21 == var37.g) {
                  boolean var38 = false;
                  if (var164 == null) {
                     var38 = var37.j == 0;
                  } else if (var164.length == var37.j) {
                     var38 = true;

                     for(int var122 = var164.length - 1; var122 >= 0; --var122) {
                        var15 -= 2;
                        if (var37.k[var122] != this.readUnsignedShort(var15)) {
                           var38 = false;
                           break;
                        }
                     }
                  }

                  if (var38) {
                     var37.h = var143;
                     var37.i = var9 - var143;
                     continue;
                  }
               }
            }

            if (var152 != 0) {
               AnnotationVisitor var165 = var36.visitAnnotationDefault();
               this.a(var152, var5, (String)null, var165);
               if (var165 != null) {
                  var165.visitEnd();
               }
            }

            for(int var123 = 1; var123 >= 0; --var123) {
               var15 = var123 == 0 ? var7 : var6;
               if (var15 != 0) {
                  int var153 = this.readUnsignedShort(var15);

                  for(int var102 = var15 + 2; var153 > 0; --var153) {
                     var102 = this.a(var102 + 2, var5, true, var36.visitAnnotation(this.readUTF8(var102, var5), var123 != 0));
                  }
               }
            }

            if (var33 != 0) {
               this.a(var33, var151, var5, true, var36);
            }

            if (var34 != 0) {
               this.a(var34, var151, var5, false, var36);
            }

            while(var8 != null) {
               Attribute var148 = var8.a;
               var8.a = null;
               var36.visitAttribute(var8);
               var8 = var148;
            }
         }

         if (var36 != null && var12 != 0) {
            int var166 = this.readUnsignedShort(var12);
            int var167 = this.readUnsignedShort(var12 + 2);
            int var39 = this.readInt(var12 + 4);
            var12 += 8;
            int var40 = var12;
            int var41 = var12 + var39;
            var36.visitCode();
            Label[] var42 = new Label[var39 + 2];
            this.readLabel(var39 + 1, var42);

            label737:
            while(var12 < var41) {
               var15 = var12 - var40;
               int var43 = var4[var12] & 255;
               switch (ClassWriter.a[var43]) {
                  case 0:
                  case 4:
                     ++var12;
                     continue;
                  case 1:
                  case 3:
                  case 10:
                     var12 += 2;
                     continue;
                  case 2:
                  case 5:
                  case 6:
                  case 11:
                  case 12:
                     var12 += 3;
                     continue;
                  case 7:
                     var12 += 5;
                     continue;
                  case 8:
                     this.readLabel(var15 + this.readShort(var12 + 1), var42);
                     var12 += 3;
                     continue;
                  case 9:
                     this.readLabel(var15 + this.readInt(var12 + 1), var42);
                     var12 += 5;
                     continue;
                  case 13:
                     var12 = var12 + 4 - (var15 & 3);
                     this.readLabel(var15 + this.readInt(var12), var42);
                     var120 = this.readInt(var12 + 8) - this.readInt(var12 + 4) + 1;
                     var12 += 12;

                     while(true) {
                        if (var120 <= 0) {
                           continue label737;
                        }

                        this.readLabel(var15 + this.readInt(var12), var42);
                        var12 += 4;
                        --var120;
                     }
                  case 14:
                     var12 = var12 + 4 - (var15 & 3);
                     this.readLabel(var15 + this.readInt(var12), var42);
                     var120 = this.readInt(var12 + 4);
                     var12 += 8;

                     while(true) {
                        if (var120 <= 0) {
                           continue label737;
                        }

                        this.readLabel(var15 + this.readInt(var12 + 4), var42);
                        var12 += 8;
                        --var120;
                     }
                  case 15:
                  default:
                     var12 += 4;
                     continue;
                  case 16:
               }

               var43 = var4[var12 + 1] & 255;
               if (var43 == 132) {
                  var12 += 6;
               } else {
                  var12 += 4;
               }
            }

            var120 = this.readUnsignedShort(var12);

            for(var12 += 2; var120 > 0; --var120) {
               Label var169 = this.readLabel(this.readUnsignedShort(var12), var42);
               Label var44 = this.readLabel(this.readUnsignedShort(var12 + 2), var42);
               Label var45 = this.readLabel(this.readUnsignedShort(var12 + 4), var42);
               int var46 = this.readUnsignedShort(var12 + 6);
               if (var46 == 0) {
                  var36.visitTryCatchBlock(var169, var44, var45, (String)null);
               } else {
                  var36.visitTryCatchBlock(var169, var44, var45, this.readUTF8(this.a[var46], var5));
               }

               var12 += 8;
            }

            int var170 = 0;
            int var171 = 0;
            int var172 = 0;
            int var174 = 0;
            int var47 = 0;
            byte var48 = 0;
            int var49 = 0;
            int var50 = 0;
            int var51 = 0;
            int var52 = 0;
            Object[] var53 = null;
            Object[] var54 = null;
            boolean var55 = true;
            var8 = null;
            var120 = this.readUnsignedShort(var12);

            for(int var95 = var12 + 2; var120 > 0; --var120) {
               String var140 = this.readUTF8(var95, var5);
               if ("LocalVariableTable".equals(var140)) {
                  if (!var18) {
                     var170 = var95 + 6;
                     int var156 = this.readUnsignedShort(var95 + 6);

                     for(int var105 = var95 + 8; var156 > 0; --var156) {
                        int var175 = this.readUnsignedShort(var105);
                        if (var42[var175] == null) {
                           Label var194 = this.readLabel(var175, var42);
                           var194.a |= 1;
                        }

                        var175 += this.readUnsignedShort(var105 + 2);
                        if (var42[var175] == null) {
                           Label var195 = this.readLabel(var175, var42);
                           var195.a |= 1;
                        }

                        var105 += 10;
                     }
                  }
               } else if ("LocalVariableTypeTable".equals(var140)) {
                  var171 = var95 + 6;
               } else if ("LineNumberTable".equals(var140)) {
                  if (!var18) {
                     int var155 = this.readUnsignedShort(var95 + 6);

                     for(int var104 = var95 + 8; var155 > 0; --var155) {
                        int var56 = this.readUnsignedShort(var104);
                        if (var42[var56] == null) {
                           Label var10000 = this.readLabel(var56, var42);
                           var10000.a |= 1;
                        }

                        var42[var56].b = this.readUnsignedShort(var104 + 2);
                        var104 += 4;
                     }
                  }
               } else if ("StackMapTable".equals(var140)) {
                  if ((var3 & 4) == 0) {
                     var172 = var95 + 8;
                     var174 = this.readInt(var95 + 2);
                     var47 = this.readUnsignedShort(var95 + 6);
                  }
               } else if ("StackMap".equals(var140)) {
                  if ((var3 & 4) == 0) {
                     var172 = var95 + 8;
                     var174 = this.readInt(var95 + 2);
                     var47 = this.readUnsignedShort(var95 + 6);
                     var55 = false;
                  }
               } else {
                  for(int var154 = 0; var154 < var2.length; ++var154) {
                     if (var2[var154].type.equals(var140)) {
                        Attribute var149 = var2[var154].read(this, var95 + 6, this.readInt(var95 + 2), var5, var40 - 8, var42);
                        if (var149 != null) {
                           var149.a = var8;
                           var8 = var149;
                        }
                     }
                  }
               }

               var95 += 6 + this.readInt(var95 + 2);
            }

            if (var172 != 0) {
               var53 = new Object[var167];
               var54 = new Object[var166];
               if (var19) {
                  int var57 = 0;
                  if ((var10 & 8) == 0) {
                     if ("<init>".equals(var11)) {
                        var53[var57++] = Opcodes.UNINITIALIZED_THIS;
                     } else {
                        var53[var57++] = this.readClass(this.header + 2, var5);
                     }
                  }

                  var120 = 1;

                  label673:
                  while(true) {
                     int var157 = var120;
                     switch (var151.charAt(var120++)) {
                        case 'B':
                        case 'C':
                        case 'I':
                        case 'S':
                        case 'Z':
                           var53[var57++] = Opcodes.INTEGER;
                           break;
                        case 'D':
                           var53[var57++] = Opcodes.DOUBLE;
                           break;
                        case 'E':
                        case 'G':
                        case 'H':
                        case 'K':
                        case 'M':
                        case 'N':
                        case 'O':
                        case 'P':
                        case 'Q':
                        case 'R':
                        case 'T':
                        case 'U':
                        case 'V':
                        case 'W':
                        case 'X':
                        case 'Y':
                        default:
                           var50 = var57;
                           break label673;
                        case 'F':
                           var53[var57++] = Opcodes.FLOAT;
                           break;
                        case 'J':
                           var53[var57++] = Opcodes.LONG;
                           break;
                        case 'L':
                           while(var151.charAt(var120) != ';') {
                              ++var120;
                           }

                           var53[var57++] = var151.substring(var157 + 1, var120++);
                           break;
                        case '[':
                           while(var151.charAt(var120) == '[') {
                              ++var120;
                           }

                           if (var151.charAt(var120) == 'L') {
                              ++var120;

                              while(var151.charAt(var120) != ';') {
                                 ++var120;
                              }
                           }

                           int var198 = var57++;
                           ++var120;
                           var53[var198] = var151.substring(var157, var120);
                     }
                  }
               }

               var49 = -1;

               for(int var129 = var172; var129 < var172 + var174 - 2; ++var129) {
                  if (var4[var129] == 8) {
                     int var158 = this.readUnsignedShort(var129 + 1);
                     if (var158 >= 0 && var158 < var39 && (var4[var40 + var158] & 255) == 187) {
                        this.readLabel(var158, var42);
                     }
                  }
               }
            }

            var12 = var40;

            while(var12 < var41) {
               var15 = var12 - var40;
               Label var179 = var42[var15];
               if (var179 != null) {
                  var36.visitLabel(var179);
                  if (!var18 && var179.b > 0) {
                     var36.visitLineNumber(var179.b, var179);
                  }
               }

               while(var53 != null && (var49 == var15 || var49 == -1)) {
                  if (var55 && !var19) {
                     if (var49 != -1) {
                        var36.visitFrame(var48, var51, var53, var52, var54);
                     }
                  } else {
                     var36.visitFrame(-1, var50, var53, var52, var54);
                  }

                  if (var47 > 0) {
                     int var58;
                     if (var55) {
                        var58 = var4[var172++] & 255;
                     } else {
                        var58 = 255;
                        var49 = -1;
                     }

                     var51 = 0;
                     int var59;
                     if (var58 < 64) {
                        var59 = var58;
                        var48 = 3;
                        var52 = 0;
                     } else if (var58 < 128) {
                        var59 = var58 - 64;
                        var172 = this.a(var54, 0, var172, var5, var42);
                        var48 = 4;
                        var52 = 1;
                     } else {
                        var59 = this.readUnsignedShort(var172);
                        var172 += 2;
                        if (var58 == 247) {
                           var172 = this.a(var54, 0, var172, var5, var42);
                           var48 = 4;
                           var52 = 1;
                        } else if (var58 >= 248 && var58 < 251) {
                           var48 = 2;
                           var51 = 251 - var58;
                           var50 -= var51;
                           var52 = 0;
                        } else if (var58 == 251) {
                           var48 = 3;
                           var52 = 0;
                        } else if (var58 < 255) {
                           var120 = var19 ? var50 : 0;

                           for(int var159 = var58 - 251; var159 > 0; --var159) {
                              var172 = this.a(var53, var120++, var172, var5, var42);
                           }

                           var48 = 1;
                           var51 = var58 - 251;
                           var50 += var51;
                           var52 = 0;
                        } else {
                           var48 = 0;
                           int var60 = var51 = var50 = this.readUnsignedShort(var172);
                           var172 += 2;

                           for(int var130 = 0; var60 > 0; --var60) {
                              var172 = this.a(var53, var130++, var172, var5, var42);
                           }

                           var60 = var52 = this.readUnsignedShort(var172);
                           var172 += 2;

                           for(int var131 = 0; var60 > 0; --var60) {
                              var172 = this.a(var54, var131++, var172, var5, var42);
                           }
                        }
                     }

                     var49 += var59 + 1;
                     this.readLabel(var49, var42);
                     --var47;
                  } else {
                     var53 = null;
                  }
               }

               int var181 = var4[var12] & 255;
               switch (ClassWriter.a[var181]) {
                  case 0:
                     var36.visitInsn(var181);
                     ++var12;
                     break;
                  case 1:
                     var36.visitIntInsn(var181, var4[var12 + 1]);
                     var12 += 2;
                     break;
                  case 2:
                     var36.visitIntInsn(var181, this.readShort(var12 + 1));
                     var12 += 3;
                     break;
                  case 3:
                     var36.visitVarInsn(var181, var4[var12 + 1] & 255);
                     var12 += 2;
                     break;
                  case 4:
                     if (var181 > 54) {
                        var181 -= 59;
                        var36.visitVarInsn(54 + (var181 >> 2), var181 & 3);
                     } else {
                        var181 -= 26;
                        var36.visitVarInsn(21 + (var181 >> 2), var181 & 3);
                     }

                     ++var12;
                     break;
                  case 5:
                     var36.visitTypeInsn(var181, this.readClass(var12 + 1, var5));
                     var12 += 3;
                     break;
                  case 6:
                  case 7:
                     int var64 = this.a[this.readUnsignedShort(var12 + 1)];
                     String var65;
                     if (var181 == 186) {
                        var65 = "java/lang/dyn/Dynamic";
                     } else {
                        var65 = this.readClass(var64, var5);
                        var64 = this.a[this.readUnsignedShort(var64 + 2)];
                     }

                     String var66 = this.readUTF8(var64, var5);
                     String var67 = this.readUTF8(var64 + 2, var5);
                     if (var181 < 182) {
                        var36.visitFieldInsn(var181, var65, var66, var67);
                     } else {
                        var36.visitMethodInsn(var181, var65, var66, var67);
                     }

                     if (var181 != 185 && var181 != 186) {
                        var12 += 3;
                        break;
                     }

                     var12 += 5;
                     break;
                  case 8:
                     var36.visitJumpInsn(var181, var42[var15 + this.readShort(var12 + 1)]);
                     var12 += 3;
                     break;
                  case 9:
                     var36.visitJumpInsn(var181 - 33, var42[var15 + this.readInt(var12 + 1)]);
                     var12 += 5;
                     break;
                  case 10:
                     var36.visitLdcInsn(this.readConst(var4[var12 + 1] & 255, var5));
                     var12 += 2;
                     break;
                  case 11:
                     var36.visitLdcInsn(this.readConst(this.readUnsignedShort(var12 + 1), var5));
                     var12 += 3;
                     break;
                  case 12:
                     var36.visitIincInsn(var4[var12 + 1] & 255, var4[var12 + 2]);
                     var12 += 3;
                     break;
                  case 13:
                     var12 = var12 + 4 - (var15 & 3);
                     int var178 = var15 + this.readInt(var12);
                     int var186 = this.readInt(var12 + 4);
                     int var189 = this.readInt(var12 + 8);
                     var12 += 12;
                     Label[] var61 = new Label[var189 - var186 + 1];

                     for(int var135 = 0; var135 < var61.length; ++var135) {
                        var61[var135] = var42[var15 + this.readInt(var12)];
                        var12 += 4;
                     }

                     var36.visitTableSwitchInsn(var186, var189, var42[var178], var61);
                     break;
                  case 14:
                     var12 = var12 + 4 - (var15 & 3);
                     int var177 = var15 + this.readInt(var12);
                     var120 = this.readInt(var12 + 4);
                     var12 += 8;
                     int[] var62 = new int[var120];
                     Label[] var63 = new Label[var120];

                     for(int var134 = 0; var134 < var62.length; ++var134) {
                        var62[var134] = this.readInt(var12);
                        var63[var134] = var42[var15 + this.readInt(var12 + 4)];
                        var12 += 8;
                     }

                     var36.visitLookupSwitchInsn(var42[var177], var62, var63);
                     break;
                  case 15:
                  default:
                     var36.visitMultiANewArrayInsn(this.readClass(var12 + 1, var5), var4[var12 + 3] & 255);
                     var12 += 4;
                     break;
                  case 16:
                     var181 = var4[var12 + 1] & 255;
                     if (var181 == 132) {
                        var36.visitIincInsn(this.readUnsignedShort(var12 + 2), this.readShort(var12 + 4));
                        var12 += 6;
                     } else {
                        var36.visitVarInsn(var181, this.readUnsignedShort(var12 + 2));
                        var12 += 4;
                     }
               }
            }

            Label var180 = var42[var41 - var40];
            if (var180 != null) {
               var36.visitLabel(var180);
            }

            if (!var18 && var170 != 0) {
               int[] var185 = null;
               if (var171 != 0) {
                  int var160 = this.readUnsignedShort(var171) * 3;
                  var15 = var171 + 2;

                  for(var185 = new int[var160]; var160 > 0; var15 += 10) {
                     --var160;
                     var185[var160] = var15 + 6;
                     --var160;
                     var185[var160] = this.readUnsignedShort(var15 + 8);
                     --var160;
                     var185[var160] = this.readUnsignedShort(var15);
                  }
               }

               int var163 = this.readUnsignedShort(var170);

               for(int var108 = var170 + 2; var163 > 0; --var163) {
                  int var187 = this.readUnsignedShort(var108);
                  int var190 = this.readUnsignedShort(var108 + 2);
                  int var191 = this.readUnsignedShort(var108 + 8);
                  String var192 = null;
                  if (var185 != null) {
                     for(int var193 = 0; var193 < var185.length; var193 += 3) {
                        if (var185[var193] == var187 && var185[var193 + 1] == var191) {
                           var192 = this.readUTF8(var185[var193 + 2], var5);
                           break;
                        }
                     }
                  }

                  var36.visitLocalVariable(this.readUTF8(var108 + 4, var5), this.readUTF8(var108 + 6, var5), var192, var42[var187], var42[var187 + var190], var191);
                  var108 += 10;
               }
            }

            while(var8 != null) {
               Attribute var150 = var8.a;
               var8.a = null;
               var36.visitAttribute(var8);
               var8 = var150;
            }

            var36.visitMaxs(var166, var167);
         }

         if (var36 != null) {
            var36.visitEnd();
         }
      }

      var1.visitEnd();
   }

   private void a(int var1, String var2, char[] var3, boolean var4, MethodVisitor var5) {
      int var6 = this.b[var1++] & 255;
      int var7 = Type.getArgumentTypes(var2).length - var6;

      int var8;
      for(var8 = 0; var8 < var7; ++var8) {
         AnnotationVisitor var9 = var5.visitParameterAnnotation(var8, "Ljava/lang/Synthetic;", false);
         if (var9 != null) {
            var9.visitEnd();
         }
      }

      while(var8 < var6 + var7) {
         int var10 = this.readUnsignedShort(var1);

         for(var1 += 2; var10 > 0; --var10) {
            AnnotationVisitor var12 = var5.visitParameterAnnotation(var8, this.readUTF8(var1, var3), var4);
            var1 = this.a(var1 + 2, var3, true, var12);
         }

         ++var8;
      }

   }

   private int a(int var1, char[] var2, boolean var3, AnnotationVisitor var4) {
      int var5 = this.readUnsignedShort(var1);
      var1 += 2;
      if (var3) {
         while(var5 > 0) {
            var1 = this.a(var1 + 2, var2, this.readUTF8(var1, var2), var4);
            --var5;
         }
      } else {
         while(var5 > 0) {
            var1 = this.a(var1, var2, (String)null, var4);
            --var5;
         }
      }

      if (var4 != null) {
         var4.visitEnd();
      }

      return var1;
   }

   private int a(int param1, char[] param2, String param3, AnnotationVisitor param4) {
      // $FF: Couldn't be decompiled
   }

   private int a(Object[] var1, int var2, int var3, char[] var4, Label[] var5) {
      int var6 = this.b[var3++] & 255;
      switch (var6) {
         case 0:
            var1[var2] = Opcodes.TOP;
            break;
         case 1:
            var1[var2] = Opcodes.INTEGER;
            break;
         case 2:
            var1[var2] = Opcodes.FLOAT;
            break;
         case 3:
            var1[var2] = Opcodes.DOUBLE;
            break;
         case 4:
            var1[var2] = Opcodes.LONG;
            break;
         case 5:
            var1[var2] = Opcodes.NULL;
            break;
         case 6:
            var1[var2] = Opcodes.UNINITIALIZED_THIS;
            break;
         case 7:
            var1[var2] = this.readClass(var3, var4);
            var3 += 2;
            break;
         default:
            var1[var2] = this.readLabel(this.readUnsignedShort(var3), var5);
            var3 += 2;
      }

      return var3;
   }

   protected Label readLabel(int var1, Label[] var2) {
      if (var2[var1] == null) {
         var2[var1] = new Label();
      }

      return var2[var1];
   }

   private Attribute a(Attribute[] var1, String var2, int var3, int var4, char[] var5, int var6, Label[] var7) {
      for(int var8 = 0; var8 < var1.length; ++var8) {
         if (var1[var8].type.equals(var2)) {
            return var1[var8].read(this, var3, var4, var5, var6, var7);
         }
      }

      return (new Attribute(var2)).read(this, var3, var4, (char[])null, -1, (Label[])null);
   }

   public int getItem(int var1) {
      return this.a[var1];
   }

   public int readByte(int var1) {
      return this.b[var1] & 255;
   }

   public int readUnsignedShort(int var1) {
      byte[] var2 = this.b;
      return (var2[var1] & 255) << 8 | var2[var1 + 1] & 255;
   }

   public short readShort(int var1) {
      byte[] var2 = this.b;
      return (short)((var2[var1] & 255) << 8 | var2[var1 + 1] & 255);
   }

   public int readInt(int var1) {
      byte[] var2 = this.b;
      return (var2[var1] & 255) << 24 | (var2[var1 + 1] & 255) << 16 | (var2[var1 + 2] & 255) << 8 | var2[var1 + 3] & 255;
   }

   public long readLong(int var1) {
      long var2 = (long)this.readInt(var1);
      long var4 = (long)this.readInt(var1 + 4) & 4294967295L;
      return var2 << 32 | var4;
   }

   public String readUTF8(int var1, char[] var2) {
      int var3 = this.readUnsignedShort(var1);
      String var4 = this.c[var3];
      if (var4 != null) {
         return var4;
      } else {
         var1 = this.a[var3];
         return this.c[var3] = this.a(var1 + 2, this.readUnsignedShort(var1), var2);
      }
   }

   private String a(int var1, int var2, char[] var3) {
      int var4 = var1 + var2;
      byte[] var5 = this.b;
      int var6 = 0;
      byte var7 = 0;
      char var8 = 0;

      while(var1 < var4) {
         int var9 = var5[var1++];
         switch (var7) {
            case 0:
               var9 &= 255;
               if (var9 < 128) {
                  var3[var6++] = (char)var9;
               } else {
                  if (var9 < 224 && var9 > 191) {
                     var8 = (char)(var9 & 31);
                     var7 = 1;
                     continue;
                  }

                  var8 = (char)(var9 & 15);
                  var7 = 2;
               }
               break;
            case 1:
               var3[var6++] = (char)(var8 << 6 | var9 & 63);
               var7 = 0;
               break;
            case 2:
               var8 = (char)(var8 << 6 | var9 & 63);
               var7 = 1;
         }
      }

      return new String(var3, 0, var6);
   }

   public String readClass(int var1, char[] var2) {
      return this.readUTF8(this.a[this.readUnsignedShort(var1)], var2);
   }

   public Object readConst(int var1, char[] var2) {
      int var3 = this.a[var1];
      switch (this.b[var3 - 1]) {
         case 3:
            return new Integer(this.readInt(var3));
         case 4:
            return new Float(Float.intBitsToFloat(this.readInt(var3)));
         case 5:
            return new Long(this.readLong(var3));
         case 6:
            return new Double(Double.longBitsToDouble(this.readLong(var3)));
         case 7:
            return Type.getObjectType(this.readUTF8(var3, var2));
         default:
            return this.readUTF8(var3, var2);
      }
   }
}
