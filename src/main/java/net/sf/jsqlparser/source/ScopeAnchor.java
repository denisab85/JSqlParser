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
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.function.Predicate;

/**
 * Filters for which AST nodes begin an execution-scope ladder (DataGrip-style).
 */
public enum ScopeAnchor implements Predicate<ASTNodeAccess> {

    /** Innermost {@link Select} or {@link Statement} around the caret. */
    SELECT_OR_STATEMENT {
        @Override
        public boolean test(ASTNodeAccess access) {
            return access instanceof Select || access instanceof Statement;
        }
    },

    /** Any model object that carries a source range. */
    ANY {
        @Override
        public boolean test(ASTNodeAccess access) {
            return true;
        }
    };
}
