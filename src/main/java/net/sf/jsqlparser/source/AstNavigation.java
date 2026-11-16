/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.source;

import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.parser.Node;

/**
 * Null-safe traversal of the parse tree beyond {@link ASTNodeAccessImpl#getParent()}.
 */
public final class AstNavigation {

    private AstNavigation() {}

    /**
     * Nearest enclosing model with an AST node, or {@code null} at the root.
     */
    public static ASTNodeAccessImpl parent(ASTNodeAccess access) {
        if (!(access instanceof ASTNodeAccessImpl)) {
            return null;
        }
        ASTNodeAccessImpl current = (ASTNodeAccessImpl) access;
        Node tree = current.getASTNode();
        if (tree == null) {
            return null;
        }
        Node parent = (Node) tree.jjtGetParent();
        while (parent != null && !(parent.jjtGetValue() instanceof ASTNodeAccessImpl)) {
            parent = (Node) parent.jjtGetParent();
        }
        return parent == null ? null : (ASTNodeAccessImpl) parent.jjtGetValue();
    }

    public static ASTNodeAccessImpl asImpl(ASTNodeAccess access) {
        return access instanceof ASTNodeAccessImpl ? (ASTNodeAccessImpl) access : null;
    }
}
