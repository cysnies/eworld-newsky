package org.hibernate.hql.internal.antlr;

public interface SqlStatementParserTokenTypes {
   int EOF = 1;
   int NULL_TREE_LOOKAHEAD = 3;
   int NOT_STMT_END = 4;
   int QUOTED_STRING = 5;
   int STMT_END = 6;
   int ESCqs = 7;
   int LINE_COMMENT = 8;
   int MULTILINE_COMMENT = 9;
}
