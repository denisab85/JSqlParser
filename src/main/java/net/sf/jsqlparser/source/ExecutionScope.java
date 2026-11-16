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

import java.util.Objects;

/**
 * A contiguous SQL fragment that could be executed via JDBC, from the parse tree.
 */
public final class ExecutionScope {

    private final String kind;
    private final SourceRange range;
    private final String sql;
    private final ASTNodeAccess model;

    public ExecutionScope(String kind, SourceRange range, String sql, ASTNodeAccess model) {
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("kind");
        }
        this.kind = kind;
        this.range = Objects.requireNonNull(range, "range");
        this.sql = Objects.requireNonNull(sql, "sql");
        this.model = model;
    }

    public String getKind() {
        return kind;
    }

    public SourceRange getRange() {
        return range;
    }

    public String getSql() {
        return sql;
    }

    public ASTNodeAccess getModel() {
        return model;
    }

    @Override
    public String toString() {
        return kind + " " + range + " " + sql;
    }
}
