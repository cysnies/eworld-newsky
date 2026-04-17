package javassist.bytecode.stackmap;

public interface TypeTag {
   TypeData TOP = null;
   TypeData INTEGER = new TypeData.BasicType("int", 1);
   TypeData FLOAT = new TypeData.BasicType("float", 2);
   TypeData DOUBLE = new TypeData.BasicType("double", 3);
   TypeData LONG = new TypeData.BasicType("long", 4);
}
