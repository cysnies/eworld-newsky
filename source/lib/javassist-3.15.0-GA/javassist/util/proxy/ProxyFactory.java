package javassist.util.proxy;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javassist.CannotCompileException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.StackMapTable;

public class ProxyFactory {
   private Class superClass = null;
   private Class[] interfaces = null;
   private MethodFilter methodFilter = null;
   private MethodHandler handler = null;
   private List signatureMethods = null;
   private byte[] signature = null;
   private String classname;
   private String basename;
   private String superName;
   private Class thisClass = null;
   private boolean factoryUseCache;
   private boolean factoryWriteReplace;
   public String writeDirectory = null;
   private static final Class OBJECT_TYPE;
   private static final String HOLDER = "_methods_";
   private static final String HOLDER_TYPE = "[Ljava/lang/reflect/Method;";
   private static final String FILTER_SIGNATURE_FIELD = "_filter_signature";
   private static final String FILTER_SIGNATURE_TYPE = "[B";
   private static final String HANDLER = "handler";
   private static final String NULL_INTERCEPTOR_HOLDER = "javassist.util.proxy.RuntimeSupport";
   private static final String DEFAULT_INTERCEPTOR = "default_interceptor";
   private static final String HANDLER_TYPE;
   private static final String HANDLER_SETTER = "setHandler";
   private static final String HANDLER_SETTER_TYPE;
   private static final String HANDLER_GETTER = "getHandler";
   private static final String HANDLER_GETTER_TYPE;
   private static final String SERIAL_VERSION_UID_FIELD = "serialVersionUID";
   private static final String SERIAL_VERSION_UID_TYPE = "J";
   private static final int SERIAL_VERSION_UID_VALUE = -1;
   public static volatile boolean useCache;
   public static volatile boolean useWriteReplace;
   private static WeakHashMap proxyCache;
   private static char[] hexDigits;
   public static ClassLoaderProvider classLoaderProvider;
   private static int counter;
   private static Comparator sorter;

   public boolean isUseCache() {
      return this.factoryUseCache;
   }

   public void setUseCache(boolean useCache) {
      if (this.handler != null && useCache) {
         throw new RuntimeException("caching cannot be enabled if the factory default interceptor has been set");
      } else {
         this.factoryUseCache = useCache;
      }
   }

   public boolean isUseWriteReplace() {
      return this.factoryWriteReplace;
   }

   public void setUseWriteReplace(boolean useWriteReplace) {
      this.factoryWriteReplace = useWriteReplace;
   }

   public static boolean isProxyClass(Class cl) {
      return ProxyObject.class.isAssignableFrom(cl);
   }

   public ProxyFactory() {
      this.factoryUseCache = useCache;
      this.factoryWriteReplace = useWriteReplace;
   }

   public void setSuperclass(Class clazz) {
      this.superClass = clazz;
      this.signature = null;
   }

   public Class getSuperclass() {
      return this.superClass;
   }

   public void setInterfaces(Class[] ifs) {
      this.interfaces = ifs;
      this.signature = null;
   }

   public Class[] getInterfaces() {
      return this.interfaces;
   }

   public void setFilter(MethodFilter mf) {
      this.methodFilter = mf;
      this.signature = null;
   }

   public Class createClass() {
      if (this.signature == null) {
         this.computeSignature(this.methodFilter);
      }

      return this.createClass1();
   }

   public Class createClass(MethodFilter filter) {
      this.computeSignature(filter);
      return this.createClass1();
   }

   Class createClass(byte[] signature) {
      this.installSignature(signature);
      return this.createClass1();
   }

   private Class createClass1() {
      if (this.thisClass == null) {
         ClassLoader cl = this.getClassLoader();
         synchronized(proxyCache) {
            if (this.factoryUseCache) {
               this.createClass2(cl);
            } else {
               this.createClass3(cl);
            }
         }
      }

      Class result = this.thisClass;
      this.thisClass = null;
      return result;
   }

   public String getKey(Class superClass, Class[] interfaces, byte[] signature, boolean useWriteReplace) {
      StringBuffer sbuf = new StringBuffer();
      if (superClass != null) {
         sbuf.append(superClass.getName());
      }

      sbuf.append(":");

      for(int i = 0; i < interfaces.length; ++i) {
         sbuf.append(interfaces[i].getName());
         sbuf.append(":");
      }

      for(int i = 0; i < signature.length; ++i) {
         byte b = signature[i];
         int lo = b & 15;
         int hi = b >> 4 & 15;
         sbuf.append(hexDigits[lo]);
         sbuf.append(hexDigits[hi]);
      }

      if (useWriteReplace) {
         sbuf.append(":w");
      }

      return sbuf.toString();
   }

   private void createClass2(ClassLoader cl) {
      String key = this.getKey(this.superClass, this.interfaces, this.signature, this.factoryWriteReplace);
      HashMap cacheForTheLoader = (HashMap)proxyCache.get(cl);
      if (cacheForTheLoader == null) {
         cacheForTheLoader = new HashMap();
         proxyCache.put(cl, cacheForTheLoader);
      }

      ProxyDetails details = (ProxyDetails)cacheForTheLoader.get(key);
      if (details != null) {
         WeakReference reference = details.proxyClass;
         this.thisClass = (Class)reference.get();
         if (this.thisClass != null) {
            return;
         }
      }

      this.createClass3(cl);
      details = new ProxyDetails(this.signature, this.thisClass, this.factoryWriteReplace);
      cacheForTheLoader.put(key, details);
   }

   private void createClass3(ClassLoader cl) {
      this.allocateClassName();

      try {
         ClassFile cf = this.make();
         if (this.writeDirectory != null) {
            FactoryHelper.writeFile(cf, this.writeDirectory);
         }

         this.thisClass = FactoryHelper.toClass(cf, cl, this.getDomain());
         this.setField("_filter_signature", this.signature);
         if (!this.factoryUseCache) {
            this.setField("default_interceptor", this.handler);
         }

      } catch (CannotCompileException e) {
         throw new RuntimeException(e.getMessage(), e);
      }
   }

   private void setField(String fieldName, Object value) {
      if (this.thisClass != null && value != null) {
         try {
            Field f = this.thisClass.getField(fieldName);
            SecurityActions.setAccessible(f, true);
            f.set((Object)null, value);
            SecurityActions.setAccessible(f, false);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }

   }

   static byte[] getFilterSignature(Class clazz) {
      return (byte[])getField(clazz, "_filter_signature");
   }

   private static Object getField(Class clazz, String fieldName) {
      try {
         Field f = clazz.getField(fieldName);
         f.setAccessible(true);
         Object value = f.get((Object)null);
         f.setAccessible(false);
         return value;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   protected ClassLoader getClassLoader() {
      return classLoaderProvider.get(this);
   }

   protected ClassLoader getClassLoader0() {
      ClassLoader loader = null;
      if (this.superClass != null && !this.superClass.getName().equals("java.lang.Object")) {
         loader = this.superClass.getClassLoader();
      } else if (this.interfaces != null && this.interfaces.length > 0) {
         loader = this.interfaces[0].getClassLoader();
      }

      if (loader == null) {
         loader = this.getClass().getClassLoader();
         if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
               loader = ClassLoader.getSystemClassLoader();
            }
         }
      }

      return loader;
   }

   protected ProtectionDomain getDomain() {
      Class clazz;
      if (this.superClass != null && !this.superClass.getName().equals("java.lang.Object")) {
         clazz = this.superClass;
      } else if (this.interfaces != null && this.interfaces.length > 0) {
         clazz = this.interfaces[0];
      } else {
         clazz = this.getClass();
      }

      return clazz.getProtectionDomain();
   }

   public Object create(Class[] paramTypes, Object[] args, MethodHandler mh) throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Object obj = this.create(paramTypes, args);
      ((ProxyObject)obj).setHandler(mh);
      return obj;
   }

   public Object create(Class[] paramTypes, Object[] args) throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Class c = this.createClass();
      Constructor cons = c.getConstructor(paramTypes);
      return cons.newInstance(args);
   }

   /** @deprecated */
   public void setHandler(MethodHandler mi) {
      if (this.factoryUseCache && mi != null) {
         this.factoryUseCache = false;
         this.thisClass = null;
      }

      this.handler = mi;
      this.setField("default_interceptor", this.handler);
   }

   private static synchronized String makeProxyName(String classname) {
      return classname + "_$$_javassist_" + counter++;
   }

   private ClassFile make() throws CannotCompileException {
      ClassFile cf = new ClassFile(false, this.classname, this.superName);
      cf.setAccessFlags(1);
      setInterfaces(cf, this.interfaces);
      ConstPool pool = cf.getConstPool();
      if (!this.factoryUseCache) {
         FieldInfo finfo = new FieldInfo(pool, "default_interceptor", HANDLER_TYPE);
         finfo.setAccessFlags(9);
         cf.addField(finfo);
      }

      FieldInfo finfo2 = new FieldInfo(pool, "handler", HANDLER_TYPE);
      finfo2.setAccessFlags(2);
      cf.addField(finfo2);
      FieldInfo finfo3 = new FieldInfo(pool, "_filter_signature", "[B");
      finfo3.setAccessFlags(9);
      cf.addField(finfo3);
      FieldInfo finfo4 = new FieldInfo(pool, "serialVersionUID", "J");
      finfo4.setAccessFlags(25);
      cf.addField(finfo4);
      this.makeConstructors(this.classname, cf, pool, this.classname);
      int s = this.overrideMethods(cf, pool, this.classname);
      addMethodsHolder(cf, pool, this.classname, s);
      addSetter(this.classname, cf, pool);
      addGetter(this.classname, cf, pool);
      if (this.factoryWriteReplace) {
         try {
            cf.addMethod(makeWriteReplace(pool));
         } catch (DuplicateMemberException var8) {
         }
      }

      this.thisClass = null;
      return cf;
   }

   private void checkClassAndSuperName() {
      if (this.interfaces == null) {
         this.interfaces = new Class[0];
      }

      if (this.superClass == null) {
         this.superClass = OBJECT_TYPE;
         this.superName = this.superClass.getName();
         this.basename = this.interfaces.length == 0 ? this.superName : this.interfaces[0].getName();
      } else {
         this.superName = this.superClass.getName();
         this.basename = this.superName;
      }

      if (Modifier.isFinal(this.superClass.getModifiers())) {
         throw new RuntimeException(this.superName + " is final");
      } else {
         if (this.basename.startsWith("java.")) {
            this.basename = "org.javassist.tmp." + this.basename;
         }

      }
   }

   private void allocateClassName() {
      this.classname = makeProxyName(this.basename);
   }

   private void makeSortedMethodList() {
      this.checkClassAndSuperName();
      HashMap allMethods = getMethods(this.superClass, this.interfaces);
      this.signatureMethods = new ArrayList(allMethods.entrySet());
      Collections.sort(this.signatureMethods, sorter);
   }

   private void computeSignature(MethodFilter filter) {
      this.makeSortedMethodList();
      int l = this.signatureMethods.size();
      int maxBytes = l + 7 >> 3;
      this.signature = new byte[maxBytes];

      for(int idx = 0; idx < l; ++idx) {
         Map.Entry e = (Map.Entry)this.signatureMethods.get(idx);
         Method m = (Method)e.getValue();
         int mod = m.getModifiers();
         if (!Modifier.isFinal(mod) && !Modifier.isStatic(mod) && isVisible(mod, this.basename, m) && (filter == null || filter.isHandled(m))) {
            this.setBit(this.signature, idx);
         }
      }

   }

   private void installSignature(byte[] signature) {
      this.makeSortedMethodList();
      int l = this.signatureMethods.size();
      int maxBytes = l + 7 >> 3;
      if (signature.length != maxBytes) {
         throw new RuntimeException("invalid filter signature length for deserialized proxy class");
      } else {
         this.signature = signature;
      }
   }

   private boolean testBit(byte[] signature, int idx) {
      int byteIdx = idx >> 3;
      if (byteIdx > signature.length) {
         return false;
      } else {
         int bitIdx = idx & 7;
         int mask = 1 << bitIdx;
         int sigByte = signature[byteIdx];
         return (sigByte & mask) != 0;
      }
   }

   private void setBit(byte[] signature, int idx) {
      int byteIdx = idx >> 3;
      if (byteIdx < signature.length) {
         int bitIdx = idx & 7;
         int mask = 1 << bitIdx;
         int sigByte = signature[byteIdx];
         signature[byteIdx] = (byte)(sigByte | mask);
      }

   }

   private static void setInterfaces(ClassFile cf, Class[] interfaces) {
      String setterIntf = ProxyObject.class.getName();
      String[] list;
      if (interfaces != null && interfaces.length != 0) {
         list = new String[interfaces.length + 1];

         for(int i = 0; i < interfaces.length; ++i) {
            list[i] = interfaces[i].getName();
         }

         list[interfaces.length] = setterIntf;
      } else {
         list = new String[]{setterIntf};
      }

      cf.setInterfaces(list);
   }

   private static void addMethodsHolder(ClassFile cf, ConstPool cp, String classname, int size) throws CannotCompileException {
      FieldInfo finfo = new FieldInfo(cp, "_methods_", "[Ljava/lang/reflect/Method;");
      finfo.setAccessFlags(10);
      cf.addField(finfo);
      MethodInfo minfo = new MethodInfo(cp, "<clinit>", "()V");
      minfo.setAccessFlags(8);
      Bytecode code = new Bytecode(cp, 0, 0);
      code.addIconst(size * 2);
      code.addAnewarray("java.lang.reflect.Method");
      code.addPutstatic(classname, "_methods_", "[Ljava/lang/reflect/Method;");
      code.addLconst(-1L);
      code.addPutstatic(classname, "serialVersionUID", "J");
      code.addOpcode(177);
      minfo.setCodeAttribute(code.toCodeAttribute());
      cf.addMethod(minfo);
   }

   private static void addSetter(String classname, ClassFile cf, ConstPool cp) throws CannotCompileException {
      MethodInfo minfo = new MethodInfo(cp, "setHandler", HANDLER_SETTER_TYPE);
      minfo.setAccessFlags(1);
      Bytecode code = new Bytecode(cp, 2, 2);
      code.addAload(0);
      code.addAload(1);
      code.addPutfield(classname, "handler", HANDLER_TYPE);
      code.addOpcode(177);
      minfo.setCodeAttribute(code.toCodeAttribute());
      cf.addMethod(minfo);
   }

   private static void addGetter(String classname, ClassFile cf, ConstPool cp) throws CannotCompileException {
      MethodInfo minfo = new MethodInfo(cp, "getHandler", HANDLER_GETTER_TYPE);
      minfo.setAccessFlags(1);
      Bytecode code = new Bytecode(cp, 1, 1);
      code.addAload(0);
      code.addGetfield(classname, "handler", HANDLER_TYPE);
      code.addOpcode(176);
      minfo.setCodeAttribute(code.toCodeAttribute());
      cf.addMethod(minfo);
   }

   private int overrideMethods(ClassFile cf, ConstPool cp, String className) throws CannotCompileException {
      String prefix = makeUniqueName("_d", this.signatureMethods);
      Iterator it = this.signatureMethods.iterator();

      int index;
      for(index = 0; it.hasNext(); ++index) {
         Map.Entry e = (Map.Entry)it.next();
         String key = (String)e.getKey();
         Method meth = (Method)e.getValue();
         int mod = meth.getModifiers();
         if (this.testBit(this.signature, index)) {
            this.override(className, meth, prefix, index, keyToDesc(key), cf, cp);
         }
      }

      return index;
   }

   private void override(String thisClassname, Method meth, String prefix, int index, String desc, ClassFile cf, ConstPool cp) throws CannotCompileException {
      Class declClass = meth.getDeclaringClass();
      String delegatorName = prefix + index + meth.getName();
      if (Modifier.isAbstract(meth.getModifiers())) {
         delegatorName = null;
      } else {
         MethodInfo delegator = makeDelegator(meth, desc, cp, declClass, delegatorName);
         delegator.setAccessFlags(delegator.getAccessFlags() & -65);
         cf.addMethod(delegator);
      }

      MethodInfo forwarder = makeForwarder(thisClassname, meth, desc, cp, declClass, delegatorName, index);
      cf.addMethod(forwarder);
   }

   private void makeConstructors(String thisClassName, ClassFile cf, ConstPool cp, String classname) throws CannotCompileException {
      Constructor[] cons = SecurityActions.getDeclaredConstructors(this.superClass);
      boolean doHandlerInit = !this.factoryUseCache;

      for(int i = 0; i < cons.length; ++i) {
         Constructor c = cons[i];
         int mod = c.getModifiers();
         if (!Modifier.isFinal(mod) && !Modifier.isPrivate(mod) && isVisible(mod, this.basename, c)) {
            MethodInfo m = makeConstructor(thisClassName, c, cp, this.superClass, doHandlerInit);
            cf.addMethod(m);
         }
      }

   }

   private static String makeUniqueName(String name, List sortedMethods) {
      if (makeUniqueName0(name, sortedMethods.iterator())) {
         return name;
      } else {
         for(int i = 100; i < 999; ++i) {
            String s = name + i;
            if (makeUniqueName0(s, sortedMethods.iterator())) {
               return s;
            }
         }

         throw new RuntimeException("cannot make a unique method name");
      }
   }

   private static boolean makeUniqueName0(String name, Iterator it) {
      while(true) {
         if (it.hasNext()) {
            Map.Entry e = (Map.Entry)it.next();
            String key = (String)e.getKey();
            if (!key.startsWith(name)) {
               continue;
            }

            return false;
         }

         return true;
      }
   }

   private static boolean isVisible(int mod, String from, Member meth) {
      if ((mod & 2) != 0) {
         return false;
      } else if ((mod & 5) != 0) {
         return true;
      } else {
         String p = getPackageName(from);
         String q = getPackageName(meth.getDeclaringClass().getName());
         if (p == null) {
            return q == null;
         } else {
            return p.equals(q);
         }
      }
   }

   private static String getPackageName(String name) {
      int i = name.lastIndexOf(46);
      return i < 0 ? null : name.substring(0, i);
   }

   private static HashMap getMethods(Class superClass, Class[] interfaceTypes) {
      HashMap hash = new HashMap();

      for(int i = 0; i < interfaceTypes.length; ++i) {
         getMethods(hash, interfaceTypes[i]);
      }

      getMethods(hash, superClass);
      return hash;
   }

   private static void getMethods(HashMap hash, Class clazz) {
      Class[] ifs = clazz.getInterfaces();

      for(int i = 0; i < ifs.length; ++i) {
         getMethods(hash, ifs[i]);
      }

      Class parent = clazz.getSuperclass();
      if (parent != null) {
         getMethods(hash, parent);
      }

      Method[] methods = SecurityActions.getDeclaredMethods(clazz);

      for(int i = 0; i < methods.length; ++i) {
         if (!Modifier.isPrivate(methods[i].getModifiers())) {
            Method m = methods[i];
            String key = m.getName() + ':' + RuntimeSupport.makeDescriptor(m);
            Method oldMethod = (Method)hash.put(key, methods[i]);
            if (null != oldMethod && Modifier.isPublic(oldMethod.getModifiers()) && !Modifier.isPublic(methods[i].getModifiers())) {
               hash.put(key, oldMethod);
            }
         }
      }

   }

   private static String keyToDesc(String key) {
      return key.substring(key.indexOf(58) + 1);
   }

   private static MethodInfo makeConstructor(String thisClassName, Constructor cons, ConstPool cp, Class superClass, boolean doHandlerInit) {
      String desc = RuntimeSupport.makeDescriptor(cons.getParameterTypes(), Void.TYPE);
      MethodInfo minfo = new MethodInfo(cp, "<init>", desc);
      minfo.setAccessFlags(1);
      setThrows(minfo, cp, cons.getExceptionTypes());
      Bytecode code = new Bytecode(cp, 0, 0);
      if (doHandlerInit) {
         code.addAload(0);
         code.addGetstatic(thisClassName, "default_interceptor", HANDLER_TYPE);
         code.addPutfield(thisClassName, "handler", HANDLER_TYPE);
         code.addGetstatic(thisClassName, "default_interceptor", HANDLER_TYPE);
         code.addOpcode(199);
         code.addIndex(10);
      }

      code.addAload(0);
      code.addGetstatic("javassist.util.proxy.RuntimeSupport", "default_interceptor", HANDLER_TYPE);
      code.addPutfield(thisClassName, "handler", HANDLER_TYPE);
      int pc = code.currentPc();
      code.addAload(0);
      int s = addLoadParameters(code, cons.getParameterTypes(), 1);
      code.addInvokespecial(superClass.getName(), "<init>", desc);
      code.addOpcode(177);
      code.setMaxLocals(s + 1);
      CodeAttribute ca = code.toCodeAttribute();
      minfo.setCodeAttribute(ca);
      StackMapTable.Writer writer = new StackMapTable.Writer(32);
      writer.sameFrame(pc);
      ca.setAttribute(writer.toStackMapTable(cp));
      return minfo;
   }

   private static MethodInfo makeDelegator(Method meth, String desc, ConstPool cp, Class declClass, String delegatorName) {
      MethodInfo delegator = new MethodInfo(cp, delegatorName, desc);
      delegator.setAccessFlags(17 | meth.getModifiers() & -1319);
      setThrows(delegator, cp, meth);
      Bytecode code = new Bytecode(cp, 0, 0);
      code.addAload(0);
      int s = addLoadParameters(code, meth.getParameterTypes(), 1);
      code.addInvokespecial(declClass.getName(), meth.getName(), desc);
      addReturn(code, meth.getReturnType());
      ++s;
      code.setMaxLocals(s);
      delegator.setCodeAttribute(code.toCodeAttribute());
      return delegator;
   }

   private static MethodInfo makeForwarder(String thisClassName, Method meth, String desc, ConstPool cp, Class declClass, String delegatorName, int index) {
      MethodInfo forwarder = new MethodInfo(cp, meth.getName(), desc);
      forwarder.setAccessFlags(16 | meth.getModifiers() & -1313);
      setThrows(forwarder, cp, meth);
      int args = Descriptor.paramSize(desc);
      Bytecode code = new Bytecode(cp, 0, args + 2);
      int origIndex = index * 2;
      int delIndex = index * 2 + 1;
      int arrayVar = args + 1;
      code.addGetstatic(thisClassName, "_methods_", "[Ljava/lang/reflect/Method;");
      code.addAstore(arrayVar);
      callFind2Methods(code, meth.getName(), delegatorName, origIndex, desc, arrayVar);
      code.addAload(0);
      code.addGetfield(thisClassName, "handler", HANDLER_TYPE);
      code.addAload(0);
      code.addAload(arrayVar);
      code.addIconst(origIndex);
      code.addOpcode(50);
      code.addAload(arrayVar);
      code.addIconst(delIndex);
      code.addOpcode(50);
      makeParameterList(code, meth.getParameterTypes());
      code.addInvokeinterface((String)MethodHandler.class.getName(), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", 5);
      Class retType = meth.getReturnType();
      addUnwrapper(code, retType);
      addReturn(code, retType);
      CodeAttribute ca = code.toCodeAttribute();
      forwarder.setCodeAttribute(ca);
      return forwarder;
   }

   private static void setThrows(MethodInfo minfo, ConstPool cp, Method orig) {
      Class[] exceptions = orig.getExceptionTypes();
      setThrows(minfo, cp, exceptions);
   }

   private static void setThrows(MethodInfo minfo, ConstPool cp, Class[] exceptions) {
      if (exceptions.length != 0) {
         String[] list = new String[exceptions.length];

         for(int i = 0; i < exceptions.length; ++i) {
            list[i] = exceptions[i].getName();
         }

         ExceptionsAttribute ea = new ExceptionsAttribute(cp);
         ea.setExceptions(list);
         minfo.setExceptionsAttribute(ea);
      }
   }

   private static int addLoadParameters(Bytecode code, Class[] params, int offset) {
      int stacksize = 0;
      int n = params.length;

      for(int i = 0; i < n; ++i) {
         stacksize += addLoad(code, stacksize + offset, params[i]);
      }

      return stacksize;
   }

   private static int addLoad(Bytecode code, int n, Class type) {
      if (type.isPrimitive()) {
         if (type == Long.TYPE) {
            code.addLload(n);
            return 2;
         }

         if (type == Float.TYPE) {
            code.addFload(n);
         } else {
            if (type == Double.TYPE) {
               code.addDload(n);
               return 2;
            }

            code.addIload(n);
         }
      } else {
         code.addAload(n);
      }

      return 1;
   }

   private static int addReturn(Bytecode code, Class type) {
      if (type.isPrimitive()) {
         if (type == Long.TYPE) {
            code.addOpcode(173);
            return 2;
         }

         if (type == Float.TYPE) {
            code.addOpcode(174);
         } else {
            if (type == Double.TYPE) {
               code.addOpcode(175);
               return 2;
            }

            if (type == Void.TYPE) {
               code.addOpcode(177);
               return 0;
            }

            code.addOpcode(172);
         }
      } else {
         code.addOpcode(176);
      }

      return 1;
   }

   private static void makeParameterList(Bytecode code, Class[] params) {
      int regno = 1;
      int n = params.length;
      code.addIconst(n);
      code.addAnewarray("java/lang/Object");

      for(int i = 0; i < n; ++i) {
         code.addOpcode(89);
         code.addIconst(i);
         Class type = params[i];
         if (type.isPrimitive()) {
            regno = makeWrapper(code, type, regno);
         } else {
            code.addAload(regno);
            ++regno;
         }

         code.addOpcode(83);
      }

   }

   private static int makeWrapper(Bytecode code, Class type, int regno) {
      int index = FactoryHelper.typeIndex(type);
      String wrapper = FactoryHelper.wrapperTypes[index];
      code.addNew(wrapper);
      code.addOpcode(89);
      addLoad(code, regno, type);
      code.addInvokespecial(wrapper, "<init>", FactoryHelper.wrapperDesc[index]);
      return regno + FactoryHelper.dataSize[index];
   }

   private static void callFind2Methods(Bytecode code, String superMethod, String thisMethod, int index, String desc, int arrayVar) {
      String findClass = RuntimeSupport.class.getName();
      String findDesc = "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;[Ljava/lang/reflect/Method;)V";
      code.addAload(0);
      code.addLdc(superMethod);
      if (thisMethod == null) {
         code.addOpcode(1);
      } else {
         code.addLdc(thisMethod);
      }

      code.addIconst(index);
      code.addLdc(desc);
      code.addAload(arrayVar);
      code.addInvokestatic(findClass, "find2Methods", findDesc);
   }

   private static void addUnwrapper(Bytecode code, Class type) {
      if (type.isPrimitive()) {
         if (type == Void.TYPE) {
            code.addOpcode(87);
         } else {
            int index = FactoryHelper.typeIndex(type);
            String wrapper = FactoryHelper.wrapperTypes[index];
            code.addCheckcast(wrapper);
            code.addInvokevirtual(wrapper, FactoryHelper.unwarpMethods[index], FactoryHelper.unwrapDesc[index]);
         }
      } else {
         code.addCheckcast(type.getName());
      }

   }

   private static MethodInfo makeWriteReplace(ConstPool cp) {
      MethodInfo minfo = new MethodInfo(cp, "writeReplace", "()Ljava/lang/Object;");
      String[] list = new String[1];
      list[0] = "java.io.ObjectStreamException";
      ExceptionsAttribute ea = new ExceptionsAttribute(cp);
      ea.setExceptions(list);
      minfo.setExceptionsAttribute(ea);
      Bytecode code = new Bytecode(cp, 0, 1);
      code.addAload(0);
      code.addInvokestatic("javassist.util.proxy.RuntimeSupport", "makeSerializedProxy", "(Ljava/lang/Object;)Ljavassist/util/proxy/SerializedProxy;");
      code.addOpcode(176);
      minfo.setCodeAttribute(code.toCodeAttribute());
      return minfo;
   }

   static {
      OBJECT_TYPE = Object.class;
      HANDLER_TYPE = 'L' + MethodHandler.class.getName().replace('.', '/') + ';';
      HANDLER_SETTER_TYPE = "(" + HANDLER_TYPE + ")V";
      HANDLER_GETTER_TYPE = "()" + HANDLER_TYPE;
      useCache = true;
      useWriteReplace = true;
      proxyCache = new WeakHashMap();
      hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
      classLoaderProvider = new ClassLoaderProvider() {
         public ClassLoader get(ProxyFactory pf) {
            return pf.getClassLoader0();
         }
      };
      counter = 0;
      sorter = new Comparator() {
         public int compare(Object o1, Object o2) {
            Map.Entry e1 = (Map.Entry)o1;
            Map.Entry e2 = (Map.Entry)o2;
            String key1 = (String)e1.getKey();
            String key2 = (String)e2.getKey();
            return key1.compareTo(key2);
         }
      };
   }

   static class ProxyDetails {
      byte[] signature;
      WeakReference proxyClass;
      boolean isUseWriteReplace;

      ProxyDetails(byte[] signature, Class proxyClass, boolean isUseWriteReplace) {
         this.signature = signature;
         this.proxyClass = new WeakReference(proxyClass);
         this.isUseWriteReplace = isUseWriteReplace;
      }
   }

   public interface ClassLoaderProvider {
      ClassLoader get(ProxyFactory var1);
   }
}
