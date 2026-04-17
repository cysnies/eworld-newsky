package javassist.bytecode.stackmap;

import java.util.ArrayList;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ConstPool;

public abstract class TypeData {
   protected TypeData() {
   }

   public abstract void merge(TypeData var1);

   static void setType(TypeData td, String className, ClassPool cp) throws BadBytecode {
      if (td == TypeTag.TOP) {
         throw new BadBytecode("unset variable");
      } else {
         td.setType(className, cp);
      }
   }

   public abstract boolean equals(Object var1);

   public abstract int getTypeTag();

   public abstract int getTypeData(ConstPool var1);

   public TypeData getSelf() {
      return this;
   }

   public abstract TypeData copy();

   public abstract boolean isObjectType();

   public boolean is2WordType() {
      return false;
   }

   public boolean isNullType() {
      return false;
   }

   public abstract String getName() throws BadBytecode;

   protected abstract void setType(String var1, ClassPool var2) throws BadBytecode;

   public abstract void evalExpectedType(ClassPool var1) throws BadBytecode;

   public abstract String getExpected() throws BadBytecode;

   protected static class BasicType extends TypeData {
      private String name;
      private int typeTag;

      public BasicType(String type, int tag) {
         this.name = type;
         this.typeTag = tag;
      }

      public void merge(TypeData neighbor) {
      }

      public boolean equals(Object obj) {
         return this == obj;
      }

      public int getTypeTag() {
         return this.typeTag;
      }

      public int getTypeData(ConstPool cp) {
         return 0;
      }

      public boolean isObjectType() {
         return false;
      }

      public boolean is2WordType() {
         return this.typeTag == 4 || this.typeTag == 3;
      }

      public TypeData copy() {
         return this;
      }

      public void evalExpectedType(ClassPool cp) throws BadBytecode {
      }

      public String getExpected() throws BadBytecode {
         return this.name;
      }

      public String getName() {
         return this.name;
      }

      protected void setType(String s, ClassPool cp) throws BadBytecode {
         throw new BadBytecode("conflict: " + this.name + " and " + s);
      }

      public String toString() {
         return this.name;
      }
   }

   protected abstract static class TypeName extends TypeData {
      protected ArrayList equivalences = new ArrayList();
      protected String expectedName;
      private CtClass cache;
      private boolean evalDone;

      protected TypeName() {
         this.equivalences.add(this);
         this.expectedName = null;
         this.cache = null;
         this.evalDone = false;
      }

      public void merge(TypeData neighbor) {
         if (this != neighbor) {
            if (neighbor instanceof TypeName) {
               TypeName neighbor2 = (TypeName)neighbor;
               ArrayList list = this.equivalences;
               ArrayList list2 = neighbor2.equivalences;
               if (list != list2) {
                  int n = list2.size();

                  for(int i = 0; i < n; ++i) {
                     TypeName tn = (TypeName)list2.get(i);
                     add(list, tn);
                     tn.equivalences = list;
                  }

               }
            }
         }
      }

      private static void add(ArrayList list, TypeData td) {
         int n = list.size();

         for(int i = 0; i < n; ++i) {
            if (list.get(i) == td) {
               return;
            }
         }

         list.add(td);
      }

      public int getTypeTag() {
         return 7;
      }

      public int getTypeData(ConstPool cp) {
         String type;
         try {
            type = this.getExpected();
         } catch (BadBytecode e) {
            throw new RuntimeException("fatal error: ", e);
         }

         return this.getTypeData2(cp, type);
      }

      protected int getTypeData2(ConstPool cp, String type) {
         return cp.addClassInfo(type);
      }

      public boolean equals(Object obj) {
         if (obj instanceof TypeName) {
            try {
               TypeName tn = (TypeName)obj;
               return this.getExpected().equals(tn.getExpected());
            } catch (BadBytecode var3) {
            }
         }

         return false;
      }

      public boolean isObjectType() {
         return true;
      }

      protected void setType(String typeName, ClassPool cp) throws BadBytecode {
         if (this.update(cp, this.expectedName, typeName)) {
            this.expectedName = typeName;
         }

      }

      public void evalExpectedType(ClassPool cp) throws BadBytecode {
         if (!this.evalDone) {
            ArrayList equiv = this.equivalences;
            int n = equiv.size();
            String name = this.evalExpectedType2(equiv, n);
            if (name == null) {
               name = this.expectedName;

               for(int i = 0; i < n; ++i) {
                  TypeData td = (TypeData)equiv.get(i);
                  if (td instanceof TypeName) {
                     TypeName tn = (TypeName)td;
                     if (this.update(cp, name, tn.expectedName)) {
                        name = tn.expectedName;
                     }
                  }
               }
            }

            for(int i = 0; i < n; ++i) {
               TypeData td = (TypeData)equiv.get(i);
               if (td instanceof TypeName) {
                  TypeName tn = (TypeName)td;
                  tn.expectedName = name;
                  tn.cache = null;
                  tn.evalDone = true;
               }
            }

         }
      }

      private String evalExpectedType2(ArrayList equiv, int n) throws BadBytecode {
         String origName = null;

         for(int i = 0; i < n; ++i) {
            TypeData td = (TypeData)equiv.get(i);
            if (!td.isNullType()) {
               if (origName == null) {
                  origName = td.getName();
               } else if (!origName.equals(td.getName())) {
                  return null;
               }
            }
         }

         return origName;
      }

      protected boolean isTypeName() {
         return true;
      }

      private boolean update(ClassPool cp, String oldName, String typeName) throws BadBytecode {
         if (typeName == null) {
            return false;
         } else if (oldName == null) {
            return true;
         } else if (oldName.equals(typeName)) {
            return false;
         } else if (typeName.charAt(0) == '[' && oldName.equals("[Ljava.lang.Object;")) {
            return true;
         } else {
            try {
               if (this.cache == null) {
                  this.cache = cp.get(oldName);
               }

               CtClass cache2 = cp.get(typeName);
               if (cache2.subtypeOf(this.cache)) {
                  this.cache = cache2;
                  return true;
               } else {
                  return false;
               }
            } catch (NotFoundException e) {
               throw new BadBytecode("cannot find " + e.getMessage());
            }
         }
      }

      public String getExpected() throws BadBytecode {
         ArrayList equiv = this.equivalences;
         if (equiv.size() == 1) {
            return this.getName();
         } else {
            String en = this.expectedName;
            return en == null ? "java.lang.Object" : en;
         }
      }

      public String toString() {
         try {
            String en = this.expectedName;
            if (en != null) {
               return en;
            } else {
               String name = this.getName();
               return this.equivalences.size() == 1 ? name : name + "?";
            }
         } catch (BadBytecode e) {
            return "<" + e.getMessage() + ">";
         }
      }
   }

   public static class ClassName extends TypeName {
      private String name;

      public ClassName(String n) {
         this.name = n;
      }

      public TypeData copy() {
         return new ClassName(this.name);
      }

      public String getName() {
         return this.name;
      }
   }

   public static class NullType extends ClassName {
      public NullType() {
         super("null");
      }

      public TypeData copy() {
         return new NullType();
      }

      public boolean isNullType() {
         return true;
      }

      public int getTypeTag() {
         try {
            return "null".equals(this.getExpected()) ? 5 : super.getTypeTag();
         } catch (BadBytecode e) {
            throw new RuntimeException("fatal error: ", e);
         }
      }

      protected int getTypeData2(ConstPool cp, String type) {
         return "null".equals(type) ? 0 : super.getTypeData2(cp, type);
      }

      public String getExpected() throws BadBytecode {
         String en = this.expectedName;
         return en == null ? "java.lang.Object" : en;
      }
   }

   public static class ArrayElement extends TypeName {
      TypeData array;

      public ArrayElement(TypeData a) {
         this.array = a;
      }

      public TypeData copy() {
         return new ArrayElement(this.array);
      }

      protected void setType(String typeName, ClassPool cp) throws BadBytecode {
         super.setType(typeName, cp);
         this.array.setType(getArrayType(typeName), cp);
      }

      public String getName() throws BadBytecode {
         String name = this.array.getName();
         if (name.length() > 1 && name.charAt(0) == '[') {
            char c = name.charAt(1);
            if (c == 'L') {
               return name.substring(2, name.length() - 1).replace('/', '.');
            }

            if (c == '[') {
               return name.substring(1);
            }
         }

         throw new BadBytecode("bad array type for AALOAD: " + name);
      }

      public static String getArrayType(String elementType) {
         return elementType.charAt(0) == '[' ? "[" + elementType : "[L" + elementType.replace('.', '/') + ";";
      }

      public static String getElementType(String arrayType) {
         char c = arrayType.charAt(1);
         if (c == 'L') {
            return arrayType.substring(2, arrayType.length() - 1).replace('/', '.');
         } else {
            return c == '[' ? arrayType.substring(1) : arrayType;
         }
      }
   }

   public static class UninitData extends TypeData {
      String className;
      int offset;
      boolean initialized;

      UninitData(int offset, String className) {
         this.className = className;
         this.offset = offset;
         this.initialized = false;
      }

      public void merge(TypeData neighbor) {
      }

      public int getTypeTag() {
         return 8;
      }

      public int getTypeData(ConstPool cp) {
         return this.offset;
      }

      public boolean equals(Object obj) {
         if (!(obj instanceof UninitData)) {
            return false;
         } else {
            UninitData ud = (UninitData)obj;
            return this.offset == ud.offset && this.className.equals(ud.className);
         }
      }

      public TypeData getSelf() {
         return (TypeData)(this.initialized ? this.copy() : this);
      }

      public TypeData copy() {
         return new ClassName(this.className);
      }

      public boolean isObjectType() {
         return true;
      }

      protected void setType(String typeName, ClassPool cp) throws BadBytecode {
         this.initialized = true;
      }

      public void evalExpectedType(ClassPool cp) throws BadBytecode {
      }

      public String getName() {
         return this.className;
      }

      public String getExpected() {
         return this.className;
      }

      public String toString() {
         return "uninit:" + this.className + "@" + this.offset;
      }
   }

   public static class UninitThis extends UninitData {
      UninitThis(String className) {
         super(-1, className);
      }

      public int getTypeTag() {
         return 6;
      }

      public int getTypeData(ConstPool cp) {
         return 0;
      }

      public boolean equals(Object obj) {
         return obj instanceof UninitThis;
      }

      public String toString() {
         return "uninit:this";
      }
   }
}
