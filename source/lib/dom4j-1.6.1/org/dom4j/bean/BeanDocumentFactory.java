package org.dom4j.bean;

import org.dom4j.Attribute;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.tree.DefaultAttribute;
import org.xml.sax.Attributes;

public class BeanDocumentFactory extends DocumentFactory {
   private static BeanDocumentFactory singleton = new BeanDocumentFactory();
   // $FF: synthetic field
   static Class class$org$dom4j$bean$BeanDocumentFactory;

   public static DocumentFactory getInstance() {
      return singleton;
   }

   public Element createElement(QName qname) {
      Object bean = this.createBean(qname);
      return bean == null ? new BeanElement(qname) : new BeanElement(qname, bean);
   }

   public Element createElement(QName qname, Attributes attributes) {
      Object bean = this.createBean(qname, attributes);
      return bean == null ? new BeanElement(qname) : new BeanElement(qname, bean);
   }

   public Attribute createAttribute(Element owner, QName qname, String value) {
      return new DefaultAttribute(qname, value);
   }

   protected Object createBean(QName qname) {
      return null;
   }

   protected Object createBean(QName qname, Attributes attributes) {
      String value = attributes.getValue("class");
      if (value != null) {
         try {
            Class beanClass = Class.forName(value, true, (class$org$dom4j$bean$BeanDocumentFactory == null ? (class$org$dom4j$bean$BeanDocumentFactory = class$("org.dom4j.bean.BeanDocumentFactory")) : class$org$dom4j$bean$BeanDocumentFactory).getClassLoader());
            return beanClass.newInstance();
         } catch (Exception e) {
            this.handleException(e);
         }
      }

      return null;
   }

   protected void handleException(Exception e) {
      System.out.println("#### Warning: couldn't create bean: " + e);
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }
}
