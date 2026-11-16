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
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.parser.Token;

/**
 * Maps {@link ASTNodeAccess} model objects to source ranges using parser token positions.
 * <p>
 * Token {@link Token#absoluteBegin} and {@link Token#absoluteEnd} are 1-based in the parse buffer;
 * this API normalizes to 0-based half-open ranges for editors.
 */
public final class SourceSpans {

    private SourceSpans() {}

    public static SourceRange range(ASTNodeAccess access) {
        if (access == null) {
            return SourceRange.EMPTY;
        }
        Node node = access.getASTNode();
        if (node == null || node.jjtGetFirstToken() == null || node.jjtGetLastToken() == null) {
            return SourceRange.EMPTY;
        }
        int start = node.jjtGetFirstToken().absoluteBegin - 1;
        int end = node.jjtGetLastToken().absoluteEnd - 1;
        if (start < 0 || end <= start) {
            return SourceRange.EMPTY;
        }
        return new SourceRange(start, end);
    }

    /**
     * Like {@link #range(ASTNodeAccess)} but returns {@code null} when the model has no span.
     */
    public static SourceRange rangeOrNull(ASTNodeAccess access, String sql) {
        SourceRange range = range(access);
        if (range.isEmpty()) {
            return null;
        }
        if (sql != null && range.end() > sql.length()) {
            return null;
        }
        return range;
    }
}
