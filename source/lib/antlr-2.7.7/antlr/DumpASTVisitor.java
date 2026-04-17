package antlr;

import antlr.collections.AST;

public class DumpASTVisitor implements ASTVisitor {
   protected int level = 0;

   private void tabs() {
      for(int var1 = 0; var1 < this.level; ++var1) {
         System.out.print("   ");
      }

   }

   public void visit(AST var1) {
      boolean var2 = false;

      for(AST var3 = var1; var3 != null; var3 = var3.getNextSibling()) {
         if (var3.getFirstChild() != null) {
            var2 = false;
            break;
         }
      }

      for(AST var4 = var1; var4 != null; var4 = var4.getNextSibling()) {
         if (!var2 || var4 == var1) {
            this.tabs();
         }

         if (var4.getText() == null) {
            System.out.print("nil");
         } else {
            System.out.print(var4.getText());
         }

         System.out.print(" [" + var4.getType() + "] ");
         if (var2) {
            System.out.print(" ");
         } else {
            System.out.println("");
         }

         if (var4.getFirstChild() != null) {
            ++this.level;
            this.visit(var4.getFirstChild());
            --this.level;
         }
      }

      if (var2) {
         System.out.println("");
      }

   }
}
