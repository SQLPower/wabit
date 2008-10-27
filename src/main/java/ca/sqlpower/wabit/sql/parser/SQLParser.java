/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Wabit.
 *
 * Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit.sql.parser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.apache.log4j.Logger;

public class SQLParser {

	Logger logger = Logger.getLogger(SQLParser.class);

	/**
	 * Uses an ANTLR parser to parse the given text, and then returns a
	 * {@link Tree} representing the Abstract Syntax Tree resulting from the
	 * parse.
	 * 
	 * @param input
	 *            The text to parse
	 * @return A Tree represengint the Abstract Syntax Tree resulting from the
	 *         parse.
	 * @throws RecognitionException
	 */
	public Tree parse(String input) throws RecognitionException {
		
		ANTLRStringStream antlrStringStream = new ANTLRStringStream(input);
		SQLANTLRLexer lexer = new SQLANTLRLexer(antlrStringStream);
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);
		
		SQLANTLRParser parser = new SQLANTLRParser(tokens);
		SQLANTLRParser.stmtblock_return r = parser.stmtblock();

		return (Tree) r.getTree();
	}

	/**
	 * A debug method that recursively prints out the structure of a given Tree.
	 * Note that you need your Log4j level for this class set to DEBUG.
	 * 
	 * @param tree
	 *            - The Tree object to print out
	 * @param level
	 *            - The level of the given Tree node. This determines how far
	 *            the text for this node is indented.
	 */
	private void printASTree(Tree tree, int level) {
		StringBuilder out = new StringBuilder();
		if (level == 0) logger.debug("*********** Printing AST Tree ***********");
		for (int i = 0; i < level; i++) {
			out.append("  ");
		}
		out.append(tree.getText()).append(" CharPositionInLine:" + tree.getCharPositionInLine()).append(" Line:" + tree.getLine());
		logger.debug(out.toString());
		for (int i = 0; i < tree.getChildCount(); i++) {
			printASTree(tree.getChild(i), level + 1);
		}
		if (level == 0) logger.debug("*********** Done Printing AST Tree ***********");
	}
}
