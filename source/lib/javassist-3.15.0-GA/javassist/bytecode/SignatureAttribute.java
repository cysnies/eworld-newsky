package javassist.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javassist.CtClass;

public class SignatureAttribute extends AttributeInfo {
   public static final String tag = "Signature";

   SignatureAttribute(ConstPool cp, int n, DataInputStream in) throws IOException {
      super(cp, n, in);
   }

   public SignatureAttribute(ConstPool cp, String signature) {
      super(cp, "Signature");
      int index = cp.addUtf8Info(signature);
      byte[] bvalue = new byte[2];
      bvalue[0] = (byte)(index >>> 8);
      bvalue[1] = (byte)index;
      this.set(bvalue);
   }

   public String getSignature() {
      return this.getConstPool().getUtf8Info(ByteArray.readU16bit(this.get(), 0));
   }

   public void setSignature(String sig) {
      int index = this.getConstPool().addUtf8Info(sig);
      ByteArray.write16bit(index, this.info, 0);
   }

   public AttributeInfo copy(ConstPool newCp, Map classnames) {
      return new SignatureAttribute(newCp, this.getSignature());
   }

   void renameClass(String oldname, String newname) {
      String sig = renameClass(this.getSignature(), oldname, newname);
      this.setSignature(sig);
   }

   void renameClass(Map classnames) {
      String sig = renameClass(this.getSignature(), classnames);
      this.setSignature(sig);
   }

   static String renameClass(String desc, String oldname, String newname) {
      Map map = new HashMap();
      map.put(oldname, newname);
      return renameClass(desc, map);
   }

   static String renameClass(String desc, Map map) {
      if (map == null) {
         return desc;
      } else {
         StringBuilder newdesc = new StringBuilder();
         int head = 0;
         int i = 0;

         while(true) {
            int j = desc.indexOf(76, i);
            if (j < 0) {
               break;
            }

            StringBuilder nameBuf = new StringBuilder();
            int k = j;

            char c;
            try {
               while(true) {
                  ++k;
                  if ((c = desc.charAt(k)) == ';') {
                     break;
                  }

                  nameBuf.append(c);
                  if (c == '<') {
                     while(true) {
                        ++k;
                        if ((c = desc.charAt(k)) == '>') {
                           nameBuf.append(c);
                           break;
                        }

                        nameBuf.append(c);
                     }
                  }
               }
            } catch (IndexOutOfBoundsException var11) {
               break;
            }

            i = k + 1;
            String name = nameBuf.toString();
            String name2 = (String)map.get(name);
            if (name2 != null) {
               newdesc.append(desc.substring(head, j));
               newdesc.append('L');
               newdesc.append(name2);
               newdesc.append(c);
               head = i;
            }
         }

         if (head == 0) {
            return desc;
         } else {
            int len = desc.length();
            if (head < len) {
               newdesc.append(desc.substring(head, len));
            }

            return newdesc.toString();
         }
      }
   }

   private static boolean isNamePart(int c) {
      return c != 59 && c != 60;
   }

   public static ClassSignature toClassSignature(String sig) throws BadBytecode {
      try {
         return parseSig(sig);
      } catch (IndexOutOfBoundsException var2) {
         throw error(sig);
      }
   }

   public static MethodSignature toMethodSignature(String sig) throws BadBytecode {
      try {
         return parseMethodSig(sig);
      } catch (IndexOutOfBoundsException var2) {
         throw error(sig);
      }
   }

   public static ObjectType toFieldSignature(String sig) throws BadBytecode {
      try {
         return parseObjectType(sig, new Cursor(), false);
      } catch (IndexOutOfBoundsException var2) {
         throw error(sig);
      }
   }

   private static ClassSignature parseSig(String sig) throws BadBytecode, IndexOutOfBoundsException {
      Cursor cur = new Cursor();
      TypeParameter[] tp = parseTypeParams(sig, cur);
      ClassType superClass = parseClassType(sig, cur);
      int sigLen = sig.length();
      ArrayList ifArray = new ArrayList();

      while(cur.position < sigLen && sig.charAt(cur.position) == 'L') {
         ifArray.add(parseClassType(sig, cur));
      }

      ClassType[] ifs = (ClassType[])ifArray.toArray(new ClassType[ifArray.size()]);
      return new ClassSignature(tp, superClass, ifs);
   }

   private static MethodSignature parseMethodSig(String sig) throws BadBytecode {
      Cursor cur = new Cursor();
      TypeParameter[] tp = parseTypeParams(sig, cur);
      if (sig.charAt(cur.position++) != '(') {
         throw error(sig);
      } else {
         ArrayList params = new ArrayList();

         while(sig.charAt(cur.position) != ')') {
            Type t = parseType(sig, cur);
            params.add(t);
         }

         ++cur.position;
         Type ret = parseType(sig, cur);
         int sigLen = sig.length();
         ArrayList exceptions = new ArrayList();

         while(cur.position < sigLen && sig.charAt(cur.position) == '^') {
            ++cur.position;
            ObjectType t = parseObjectType(sig, cur, false);
            if (t instanceof ArrayType) {
               throw error(sig);
            }

            exceptions.add(t);
         }

         Type[] p = (Type[])params.toArray(new Type[params.size()]);
         ObjectType[] ex = (ObjectType[])exceptions.toArray(new ObjectType[exceptions.size()]);
         return new MethodSignature(tp, p, ret, ex);
      }
   }

   private static TypeParameter[] parseTypeParams(String sig, Cursor cur) throws BadBytecode {
      ArrayList typeParam = new ArrayList();
      if (sig.charAt(cur.position) == '<') {
         ++cur.position;

         while(sig.charAt(cur.position) != '>') {
            int nameBegin = cur.position;
            int nameEnd = cur.indexOf(sig, 58);
            ObjectType classBound = parseObjectType(sig, cur, true);
            ArrayList ifBound = new ArrayList();

            while(sig.charAt(cur.position) == ':') {
               ++cur.position;
               ObjectType t = parseObjectType(sig, cur, false);
               ifBound.add(t);
            }

            TypeParameter p = new TypeParameter(sig, nameBegin, nameEnd, classBound, (ObjectType[])ifBound.toArray(new ObjectType[ifBound.size()]));
            typeParam.add(p);
         }

         ++cur.position;
      }

      return (TypeParameter[])typeParam.toArray(new TypeParameter[typeParam.size()]);
   }

   private static ObjectType parseObjectType(String sig, Cursor c, boolean dontThrow) throws BadBytecode {
      int begin = c.position;
      switch (sig.charAt(begin)) {
         case 'L':
            return parseClassType2(sig, c, (ClassType)null);
         case 'T':
            int i = c.indexOf(sig, 59);
            return new TypeVariable(sig, begin + 1, i);
         case '[':
            return parseArray(sig, c);
         default:
            if (dontThrow) {
               return null;
            } else {
               throw error(sig);
            }
      }
   }

   private static ClassType parseClassType(String sig, Cursor c) throws BadBytecode {
      if (sig.charAt(c.position) == 'L') {
         return parseClassType2(sig, c, (ClassType)null);
      } else {
         throw error(sig);
      }
   }

   private static ClassType parseClassType2(String sig, Cursor c, ClassType parent) throws BadBytecode {
      int start = ++c.position;

      char t;
      do {
         t = sig.charAt(c.position++);
      } while(t != '$' && t != '<' && t != ';');

      int end = c.position - 1;
      TypeArgument[] targs;
      if (t == '<') {
         targs = parseTypeArgs(sig, c);
         t = sig.charAt(c.position++);
      } else {
         targs = null;
      }

      ClassType thisClass = SignatureAttribute.ClassType.make(sig, start, end, targs, parent);
      if (t == '$') {
         --c.position;
         return parseClassType2(sig, c, thisClass);
      } else {
         return thisClass;
      }
   }

   private static TypeArgument[] parseTypeArgs(String sig, Cursor c) throws BadBytecode {
      ArrayList args;
      char t;
      TypeArgument ta;
      for(args = new ArrayList(); (t = sig.charAt(c.position++)) != '>'; args.add(ta)) {
         if (t == '*') {
            ta = new TypeArgument((ObjectType)null, '*');
         } else {
            if (t != '+' && t != '-') {
               t = ' ';
               --c.position;
            }

            ta = new TypeArgument(parseObjectType(sig, c, false), t);
         }
      }

      return (TypeArgument[])args.toArray(new TypeArgument[args.size()]);
   }

   private static ObjectType parseArray(String sig, Cursor c) throws BadBytecode {
      int dim;
      for(dim = 1; sig.charAt(++c.position) == '['; ++dim) {
      }

      return new ArrayType(dim, parseType(sig, c));
   }

   private static Type parseType(String sig, Cursor c) throws BadBytecode {
      Type t = parseObjectType(sig, c, true);
      if (t == null) {
         t = new BaseType(sig.charAt(c.position++));
      }

      return t;
   }

   private static BadBytecode error(String sig) {
      return new BadBytecode("bad signature: " + sig);
   }

   private static class Cursor {
      int position;

      private Cursor() {
         this.position = 0;
      }

      int indexOf(String s, int ch) throws BadBytecode {
         int i = s.indexOf(ch, this.position);
         if (i < 0) {
            throw SignatureAttribute.error(s);
         } else {
            this.position = i + 1;
            return i;
         }
      }
   }

   public static class ClassSignature {
      TypeParameter[] params;
      ClassType superClass;
      ClassType[] interfaces;

      ClassSignature(TypeParameter[] p, ClassType s, ClassType[] i) {
         this.params = p;
         this.superClass = s;
         this.interfaces = i;
      }

      public TypeParameter[] getParameters() {
         return this.params;
      }

      public ClassType getSuperClass() {
         return this.superClass;
      }

      public ClassType[] getInterfaces() {
         return this.interfaces;
      }

      public String toString() {
         StringBuffer sbuf = new StringBuffer();
         SignatureAttribute.TypeParameter.toString(sbuf, this.params);
         sbuf.append(" extends ").append(this.superClass);
         if (this.interfaces.length > 0) {
            sbuf.append(" implements ");
            SignatureAttribute.Type.toString(sbuf, this.interfaces);
         }

         return sbuf.toString();
      }
   }

   public static class MethodSignature {
      TypeParameter[] typeParams;
      Type[] params;
      Type retType;
      ObjectType[] exceptions;

      MethodSignature(TypeParameter[] tp, Type[] p, Type ret, ObjectType[] ex) {
         this.typeParams = tp;
         this.params = p;
         this.retType = ret;
         this.exceptions = ex;
      }

      public TypeParameter[] getTypeParameters() {
         return this.typeParams;
      }

      public Type[] getParameterTypes() {
         return this.params;
      }

      public Type getReturnType() {
         return this.retType;
      }

      public ObjectType[] getExceptionTypes() {
         return this.exceptions;
      }

      public String toString() {
         StringBuffer sbuf = new StringBuffer();
         SignatureAttribute.TypeParameter.toString(sbuf, this.typeParams);
         sbuf.append(" (");
         SignatureAttribute.Type.toString(sbuf, this.params);
         sbuf.append(") ");
         sbuf.append(this.retType);
         if (this.exceptions.length > 0) {
            sbuf.append(" throws ");
            SignatureAttribute.Type.toString(sbuf, this.exceptions);
         }

         return sbuf.toString();
      }
   }

   public static class TypeParameter {
      String name;
      ObjectType superClass;
      ObjectType[] superInterfaces;

      TypeParameter(String sig, int nb, int ne, ObjectType sc, ObjectType[] si) {
         this.name = sig.substring(nb, ne);
         this.superClass = sc;
         this.superInterfaces = si;
      }

      public String getName() {
         return this.name;
      }

      public ObjectType getClassBound() {
         return this.superClass;
      }

      public ObjectType[] getInterfaceBound() {
         return this.superInterfaces;
      }

      public String toString() {
         StringBuffer sbuf = new StringBuffer(this.getName());
         if (this.superClass != null) {
            sbuf.append(" extends ").append(this.superClass.toString());
         }

         int len = this.superInterfaces.length;
         if (len > 0) {
            for(int i = 0; i < len; ++i) {
               if (i <= 0 && this.superClass == null) {
                  sbuf.append(" extends ");
               } else {
                  sbuf.append(" & ");
               }

               sbuf.append(this.superInterfaces[i].toString());
            }
         }

         return sbuf.toString();
      }

      static void toString(StringBuffer sbuf, TypeParameter[] tp) {
         sbuf.append('<');

         for(int i = 0; i < tp.length; ++i) {
            if (i > 0) {
               sbuf.append(", ");
            }

            sbuf.append(tp[i]);
         }

         sbuf.append('>');
      }
   }

   public static class TypeArgument {
      ObjectType arg;
      char wildcard;

      TypeArgument(ObjectType a, char w) {
         this.arg = a;
         this.wildcard = w;
      }

      public char getKind() {
         return this.wildcard;
      }

      public boolean isWildcard() {
         return this.wildcard != ' ';
      }

      public ObjectType getType() {
         return this.arg;
      }

      public String toString() {
         if (this.wildcard == '*') {
            return "?";
         } else {
            String type = this.arg.toString();
            if (this.wildcard == ' ') {
               return type;
            } else {
               return this.wildcard == '+' ? "? extends " + type : "? super " + type;
            }
         }
      }
   }

   public abstract static class Type {
      static void toString(StringBuffer sbuf, Type[] ts) {
         for(int i = 0; i < ts.length; ++i) {
            if (i > 0) {
               sbuf.append(", ");
            }

            sbuf.append(ts[i]);
         }

      }
   }

   public static class BaseType extends Type {
      char descriptor;

      BaseType(char c) {
         this.descriptor = c;
      }

      public char getDescriptor() {
         return this.descriptor;
      }

      public CtClass getCtlass() {
         return Descriptor.toPrimitiveClass(this.descriptor);
      }

      public String toString() {
         return Descriptor.toClassName(Character.toString(this.descriptor));
      }
   }

   public abstract static class ObjectType extends Type {
   }

   public static class ClassType extends ObjectType {
      String name;
      TypeArgument[] arguments;

      static ClassType make(String s, int b, int e, TypeArgument[] targs, ClassType parent) {
         return (ClassType)(parent == null ? new ClassType(s, b, e, targs) : new NestedClassType(s, b, e, targs, parent));
      }

      ClassType(String signature, int begin, int end, TypeArgument[] targs) {
         this.name = signature.substring(begin, end).replace('/', '.');
         this.arguments = targs;
      }

      public String getName() {
         return this.name;
      }

      public TypeArgument[] getTypeArguments() {
         return this.arguments;
      }

      public ClassType getDeclaringClass() {
         return null;
      }

      public String toString() {
         StringBuffer sbuf = new StringBuffer();
         ClassType parent = this.getDeclaringClass();
         if (parent != null) {
            sbuf.append(parent.toString()).append('.');
         }

         sbuf.append(this.name);
         if (this.arguments != null) {
            sbuf.append('<');
            int n = this.arguments.length;

            for(int i = 0; i < n; ++i) {
               if (i > 0) {
                  sbuf.append(", ");
               }

               sbuf.append(this.arguments[i].toString());
            }

            sbuf.append('>');
         }

         return sbuf.toString();
      }
   }

   public static class NestedClassType extends ClassType {
      ClassType parent;

      NestedClassType(String s, int b, int e, TypeArgument[] targs, ClassType p) {
         super(s, b, e, targs);
         this.parent = p;
      }

      public ClassType getDeclaringClass() {
         return this.parent;
      }
   }

   public static class ArrayType extends ObjectType {
      int dim;
      Type componentType;

      public ArrayType(int d, Type comp) {
         this.dim = d;
         this.componentType = comp;
      }

      public int getDimension() {
         return this.dim;
      }

      public Type getComponentType() {
         return this.componentType;
      }

      public String toString() {
         StringBuffer sbuf = new StringBuffer(this.componentType.toString());

         for(int i = 0; i < this.dim; ++i) {
            sbuf.append("[]");
         }

         return sbuf.toString();
      }
   }

   public static class TypeVariable extends ObjectType {
      String name;

      TypeVariable(String sig, int begin, int end) {
         this.name = sig.substring(begin, end);
      }

      public String getName() {
         return this.name;
      }

      public String toString() {
         return this.name;
      }
   }
}
