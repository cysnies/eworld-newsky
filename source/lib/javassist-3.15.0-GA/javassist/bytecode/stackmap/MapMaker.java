package javassist.bytecode.stackmap;

import javassist.ClassPool;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.StackMap;
import javassist.bytecode.StackMapTable;

public class MapMaker extends Tracer {
   public static StackMapTable make(ClassPool classes, MethodInfo minfo) throws BadBytecode {
      CodeAttribute ca = minfo.getCodeAttribute();
      if (ca == null) {
         return null;
      } else {
         TypedBlock[] blocks = TypedBlock.makeBlocks(minfo, ca, true);
         if (blocks == null) {
            return null;
         } else {
            MapMaker mm = new MapMaker(classes, minfo, ca);
            mm.make(blocks, ca.getCode());
            return mm.toStackMap(blocks);
         }
      }
   }

   public static StackMap make2(ClassPool classes, MethodInfo minfo) throws BadBytecode {
      CodeAttribute ca = minfo.getCodeAttribute();
      if (ca == null) {
         return null;
      } else {
         TypedBlock[] blocks = TypedBlock.makeBlocks(minfo, ca, true);
         if (blocks == null) {
            return null;
         } else {
            MapMaker mm = new MapMaker(classes, minfo, ca);
            mm.make(blocks, ca.getCode());
            return mm.toStackMap2(minfo.getConstPool(), blocks);
         }
      }
   }

   public MapMaker(ClassPool classes, MethodInfo minfo, CodeAttribute ca) {
      super(classes, minfo.getConstPool(), ca.getMaxStack(), ca.getMaxLocals(), TypedBlock.getRetType(minfo.getDescriptor()));
   }

   protected MapMaker(MapMaker old, boolean copyStack) {
      super(old, copyStack);
   }

   void make(TypedBlock[] blocks, byte[] code) throws BadBytecode {
      TypedBlock first = blocks[0];
      this.fixParamTypes(first);
      TypeData[] srcTypes = first.localsTypes;
      copyFrom(srcTypes.length, srcTypes, this.localsTypes);
      this.make(code, first);
      int n = blocks.length;

      for(int i = 0; i < n; ++i) {
         this.evalExpected(blocks[i]);
      }

   }

   private void fixParamTypes(TypedBlock first) throws BadBytecode {
      for(TypeData t : first.localsTypes) {
         if (t instanceof TypeData.ClassName) {
            TypeData.setType(t, t.getName(), this.classPool);
         }
      }

   }

   private void make(byte[] code, TypedBlock tb) throws BadBytecode {
      for(BasicBlock.Catch handlers = tb.toCatch; handlers != null; handlers = handlers.next) {
         this.traceException(code, handlers);
      }

      int pos = tb.position;

      for(int end = pos + tb.length; pos < end; pos += this.doOpcode(pos, code)) {
      }

      if (tb.exit != null) {
         for(int i = 0; i < tb.exit.length; ++i) {
            TypedBlock e = (TypedBlock)tb.exit[i];
            if (e.alreadySet()) {
               this.mergeMap(e, true);
            } else {
               this.recordStackMap(e);
               MapMaker maker = new MapMaker(this, true);
               maker.make(code, e);
            }
         }
      }

   }

   private void traceException(byte[] code, BasicBlock.Catch handler) throws BadBytecode {
      TypedBlock tb = (TypedBlock)handler.body;
      if (tb.alreadySet()) {
         this.mergeMap(tb, false);
      } else {
         this.recordStackMap(tb, handler.typeIndex);
         MapMaker maker = new MapMaker(this, false);
         maker.stackTypes[0] = tb.stackTypes[0].getSelf();
         maker.stackTop = 1;
         maker.make(code, tb);
      }

   }

   private void mergeMap(TypedBlock dest, boolean mergeStack) {
      boolean[] inputs = dest.inputs;
      int n = inputs.length;

      for(int i = 0; i < n; ++i) {
         if (inputs[i]) {
            this.merge(this.localsTypes[i], dest.localsTypes[i]);
         }
      }

      if (mergeStack) {
         n = this.stackTop;

         for(int i = 0; i < n; ++i) {
            this.merge(this.stackTypes[i], dest.stackTypes[i]);
         }
      }

   }

   private void merge(TypeData td, TypeData target) {
      boolean tdIsObj = false;
      boolean targetIsObj = false;
      if (td != TOP && td.isObjectType()) {
         tdIsObj = true;
      }

      if (target != TOP && target.isObjectType()) {
         targetIsObj = true;
      }

      if (tdIsObj && targetIsObj) {
         target.merge(td);
      }

   }

   private void recordStackMap(TypedBlock target) throws BadBytecode {
      TypeData[] tStackTypes = new TypeData[this.stackTypes.length];
      int st = this.stackTop;
      copyFrom(st, this.stackTypes, tStackTypes);
      this.recordStackMap0(target, st, tStackTypes);
   }

   private void recordStackMap(TypedBlock target, int exceptionType) throws BadBytecode {
      String type;
      if (exceptionType == 0) {
         type = "java.lang.Throwable";
      } else {
         type = this.cpool.getClassInfo(exceptionType);
      }

      TypeData[] tStackTypes = new TypeData[this.stackTypes.length];
      tStackTypes[0] = new TypeData.ClassName(type);
      this.recordStackMap0(target, 1, tStackTypes);
   }

   private void recordStackMap0(TypedBlock target, int st, TypeData[] tStackTypes) throws BadBytecode {
      int n = this.localsTypes.length;
      TypeData[] tLocalsTypes = new TypeData[n];
      int k = copyFrom(n, this.localsTypes, tLocalsTypes);
      boolean[] inputs = target.inputs;

      for(int i = 0; i < n; ++i) {
         if (!inputs[i]) {
            tLocalsTypes[i] = TOP;
         }
      }

      target.setStackMap(st, tStackTypes, k, tLocalsTypes);
   }

   void evalExpected(TypedBlock target) throws BadBytecode {
      ClassPool cp = this.classPool;
      evalExpected(cp, target.stackTop, target.stackTypes);
      TypeData[] types = target.localsTypes;
      if (types != null) {
         evalExpected(cp, types.length, types);
      }

   }

   private static void evalExpected(ClassPool cp, int n, TypeData[] types) throws BadBytecode {
      for(int i = 0; i < n; ++i) {
         TypeData td = types[i];
         if (td != null) {
            td.evalExpectedType(cp);
         }
      }

   }

   public StackMapTable toStackMap(TypedBlock[] blocks) {
      StackMapTable.Writer writer = new StackMapTable.Writer(32);
      int n = blocks.length;
      TypedBlock prev = blocks[0];
      int offsetDelta = prev.length;
      if (prev.incoming > 0) {
         writer.sameFrame(0);
         --offsetDelta;
      }

      for(int i = 1; i < n; ++i) {
         TypedBlock bb = blocks[i];
         if (this.isTarget(bb, blocks[i - 1])) {
            bb.resetNumLocals();
            int diffL = stackMapDiff(prev.numLocals, prev.localsTypes, bb.numLocals, bb.localsTypes);
            this.toStackMapBody(writer, bb, diffL, offsetDelta, prev);
            offsetDelta = bb.length - 1;
            prev = bb;
         } else {
            offsetDelta += bb.length;
         }
      }

      return writer.toStackMapTable(this.cpool);
   }

   private boolean isTarget(TypedBlock cur, TypedBlock prev) {
      int in = cur.incoming;
      if (in > 1) {
         return true;
      } else {
         return in < 1 ? false : prev.stop;
      }
   }

   private void toStackMapBody(StackMapTable.Writer writer, TypedBlock bb, int diffL, int offsetDelta, TypedBlock prev) {
      int stackTop = bb.stackTop;
      if (stackTop == 0) {
         if (diffL == 0) {
            writer.sameFrame(offsetDelta);
            return;
         }

         if (0 > diffL && diffL >= -3) {
            writer.chopFrame(offsetDelta, -diffL);
            return;
         }

         if (0 < diffL && diffL <= 3) {
            int[] data = new int[diffL];
            int[] tags = this.fillStackMap(bb.numLocals - prev.numLocals, prev.numLocals, data, bb.localsTypes);
            writer.appendFrame(offsetDelta, tags, data);
            return;
         }
      } else {
         if (stackTop == 1 && diffL == 0) {
            TypeData td = bb.stackTypes[0];
            if (td == TOP) {
               writer.sameLocals(offsetDelta, 0, 0);
            } else {
               writer.sameLocals(offsetDelta, td.getTypeTag(), td.getTypeData(this.cpool));
            }

            return;
         }

         if (stackTop == 2 && diffL == 0) {
            TypeData td = bb.stackTypes[0];
            if (td != TOP && td.is2WordType()) {
               writer.sameLocals(offsetDelta, td.getTypeTag(), td.getTypeData(this.cpool));
               return;
            }
         }
      }

      int[] sdata = new int[stackTop];
      int[] stags = this.fillStackMap(stackTop, 0, sdata, bb.stackTypes);
      int[] ldata = new int[bb.numLocals];
      int[] ltags = this.fillStackMap(bb.numLocals, 0, ldata, bb.localsTypes);
      writer.fullFrame(offsetDelta, ltags, ldata, stags, sdata);
   }

   private int[] fillStackMap(int num, int offset, int[] data, TypeData[] types) {
      int realNum = diffSize(types, offset, offset + num);
      ConstPool cp = this.cpool;
      int[] tags = new int[realNum];
      int j = 0;

      for(int i = 0; i < num; ++i) {
         TypeData td = types[offset + i];
         if (td == TOP) {
            tags[j] = 0;
            data[j] = 0;
         } else {
            tags[j] = td.getTypeTag();
            data[j] = td.getTypeData(cp);
            if (td.is2WordType()) {
               ++i;
            }
         }

         ++j;
      }

      return tags;
   }

   private static int stackMapDiff(int oldTdLen, TypeData[] oldTd, int newTdLen, TypeData[] newTd) {
      int diff = newTdLen - oldTdLen;
      int len;
      if (diff > 0) {
         len = oldTdLen;
      } else {
         len = newTdLen;
      }

      if (stackMapEq(oldTd, newTd, len)) {
         return diff > 0 ? diffSize(newTd, len, newTdLen) : -diffSize(oldTd, len, oldTdLen);
      } else {
         return -100;
      }
   }

   private static boolean stackMapEq(TypeData[] oldTd, TypeData[] newTd, int len) {
      for(int i = 0; i < len; ++i) {
         TypeData td = oldTd[i];
         if (td == TOP) {
            if (newTd[i] != TOP) {
               return false;
            }
         } else if (!oldTd[i].equals(newTd[i])) {
            return false;
         }
      }

      return true;
   }

   private static int diffSize(TypeData[] types, int offset, int len) {
      int num = 0;

      while(offset < len) {
         TypeData td = types[offset++];
         ++num;
         if (td != TOP && td.is2WordType()) {
            ++offset;
         }
      }

      return num;
   }

   public StackMap toStackMap2(ConstPool cp, TypedBlock[] blocks) {
      StackMap.Writer writer = new StackMap.Writer();
      int n = blocks.length;
      boolean[] effective = new boolean[n];
      TypedBlock prev = blocks[0];
      effective[0] = prev.incoming > 0;
      int num = effective[0] ? 1 : 0;

      for(int i = 1; i < n; ++i) {
         TypedBlock bb = blocks[i];
         if (effective[i] = this.isTarget(bb, blocks[i - 1])) {
            bb.resetNumLocals();
            ++num;
         }
      }

      if (num == 0) {
         return null;
      } else {
         writer.write16bit(num);

         for(int i = 0; i < n; ++i) {
            if (effective[i]) {
               this.writeStackFrame(writer, cp, blocks[i].position, blocks[i]);
            }
         }

         return writer.toStackMap(cp);
      }
   }

   private void writeStackFrame(StackMap.Writer writer, ConstPool cp, int offset, TypedBlock tb) {
      writer.write16bit(offset);
      this.writeVerifyTypeInfo(writer, cp, tb.localsTypes, tb.numLocals);
      this.writeVerifyTypeInfo(writer, cp, tb.stackTypes, tb.stackTop);
   }

   private void writeVerifyTypeInfo(StackMap.Writer writer, ConstPool cp, TypeData[] types, int num) {
      int numDWord = 0;

      for(int i = 0; i < num; ++i) {
         TypeData td = types[i];
         if (td != null && td.is2WordType()) {
            ++numDWord;
            ++i;
         }
      }

      writer.write16bit(num - numDWord);

      for(int i = 0; i < num; ++i) {
         TypeData td = types[i];
         if (td == TOP) {
            writer.writeVerifyTypeInfo(0, 0);
         } else {
            writer.writeVerifyTypeInfo(td.getTypeTag(), td.getTypeData(cp));
            if (td.is2WordType()) {
               ++i;
            }
         }
      }

   }
}
