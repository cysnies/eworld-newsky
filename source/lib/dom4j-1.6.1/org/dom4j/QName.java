package org.dom4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.dom4j.tree.QNameCache;
import org.dom4j.util.SingletonStrategy;

public class QName implements Serializable {
   private static SingletonStrategy singleton = null;
   private String name;
   private String qualifiedName;
   private transient Namespace namespace;
   private int hashCode;
   private DocumentFactory documentFactory;
   // $FF: synthetic field
   static Class class$org$dom4j$tree$QNameCache;

   public QName(String name) {
      this(name, Namespace.NO_NAMESPACE);
   }

   public QName(String name, Namespace namespace) {
      this.name = name == null ? "" : name;
      this.namespace = namespace == null ? Namespace.NO_NAMESPACE : namespace;
   }

   public QName(String name, Namespace namespace, String qualifiedName) {
      this.name = name == null ? "" : name;
      this.qualifiedName = qualifiedName;
      this.namespace = namespace == null ? Namespace.NO_NAMESPACE : namespace;
   }

   public static QName get(String name) {
      return getCache().get(name);
   }

   public static QName get(String name, Namespace namespace) {
      return getCache().get(name, namespace);
   }

   public static QName get(String name, String prefix, String uri) {
      if ((prefix == null || prefix.length() == 0) && uri == null) {
         return get(name);
      } else if (prefix != null && prefix.length() != 0) {
         return uri == null ? get(name) : getCache().get(name, Namespace.get(prefix, uri));
      } else {
         return getCache().get(name, Namespace.get(uri));
      }
   }

   public static QName get(String qualifiedName, String uri) {
      return uri == null ? getCache().get(qualifiedName) : getCache().get(qualifiedName, uri);
   }

   public static QName get(String localName, Namespace namespace, String qualifiedName) {
      return getCache().get(localName, namespace, qualifiedName);
   }

   public String getName() {
      return this.name;
   }

   public String getQualifiedName() {
      if (this.qualifiedName == null) {
         String prefix = this.getNamespacePrefix();
         if (prefix != null && prefix.length() > 0) {
            this.qualifiedName = prefix + ":" + this.name;
         } else {
            this.qualifiedName = this.name;
         }
      }

      return this.qualifiedName;
   }

   public Namespace getNamespace() {
      return this.namespace;
   }

   public String getNamespacePrefix() {
      return this.namespace == null ? "" : this.namespace.getPrefix();
   }

   public String getNamespaceURI() {
      return this.namespace == null ? "" : this.namespace.getURI();
   }

   public int hashCode() {
      if (this.hashCode == 0) {
         this.hashCode = this.getName().hashCode() ^ this.getNamespaceURI().hashCode();
         if (this.hashCode == 0) {
            this.hashCode = 47806;
         }
      }

      return this.hashCode;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof QName) {
            QName that = (QName)object;
            if (this.hashCode() == that.hashCode()) {
               return this.getName().equals(that.getName()) && this.getNamespaceURI().equals(that.getNamespaceURI());
            }
         }

         return false;
      }
   }

   public String toString() {
      return super.toString() + " [name: " + this.getName() + " namespace: \"" + this.getNamespace() + "\"]";
   }

   public DocumentFactory getDocumentFactory() {
      return this.documentFactory;
   }

   public void setDocumentFactory(DocumentFactory documentFactory) {
      this.documentFactory = documentFactory;
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeObject(this.namespace.getPrefix());
      out.writeObject(this.namespace.getURI());
      out.defaultWriteObject();
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      String prefix = (String)in.readObject();
      String uri = (String)in.readObject();
      in.defaultReadObject();
      this.namespace = Namespace.get(prefix, uri);
   }

   private static QNameCache getCache() {
      QNameCache cache = (QNameCache)singleton.instance();
      return cache;
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      try {
         String defaultSingletonClass = "org.dom4j.util.SimpleSingleton";
         Class clazz = null;

         try {
            String singletonClass = System.getProperty("org.dom4j.QName.singleton.strategy", defaultSingletonClass);
            clazz = Class.forName(singletonClass);
         } catch (Exception var5) {
            try {
               clazz = Class.forName(defaultSingletonClass);
            } catch (Exception var4) {
            }
         }

         singleton = (SingletonStrategy)clazz.newInstance();
         singleton.setSingletonClassName((class$org$dom4j$tree$QNameCache == null ? (class$org$dom4j$tree$QNameCache = class$("org.dom4j.tree.QNameCache")) : class$org$dom4j$tree$QNameCache).getName());
      } catch (Exception var6) {
      }

   }
}
