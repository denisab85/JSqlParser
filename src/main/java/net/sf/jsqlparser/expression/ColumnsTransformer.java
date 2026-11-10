/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * A ClickHouse-style transformer following {@code *} or {@code COLUMNS(...)}.
 */
public class ColumnsTransformer extends ASTNodeAccessImpl {

    public enum ColumnsTransformerType {
        APPLY, EXCEPT, EXCLUDE, REPLACE, RENAME
    }

    private ColumnsTransformerType type;
    private Expression applyExpression;
    private ParenthesedExpressionList<Column> exceptColumns;
    private StringValue exceptPattern;
    private List<SelectItem<?>> replaceItems;
    private List<SelectItem<?>> renameItems;
    private boolean parenthesized = true;
    private boolean strict;

    public ColumnsTransformer(ColumnsTransformerType type) {
        this.type = type;
    }

    public ColumnsTransformerType getType() {
        return type;
    }

    public ColumnsTransformer setType(ColumnsTransformerType type) {
        this.type = type;
        return this;
    }

    public Expression getApplyExpression() {
        return applyExpression;
    }

    public ColumnsTransformer setApplyExpression(Expression applyExpression) {
        this.applyExpression = applyExpression;
        return this;
    }

    public ParenthesedExpressionList<Column> getExceptColumns() {
        return exceptColumns;
    }

    public ColumnsTransformer setExceptColumns(ParenthesedExpressionList<Column> exceptColumns) {
        this.exceptColumns = exceptColumns;
        if (exceptColumns != null) {
            this.exceptPattern = null;
        }
        return this;
    }

    public StringValue getExceptPattern() {
        return exceptPattern;
    }

    public ColumnsTransformer setExceptPattern(StringValue exceptPattern) {
        this.exceptPattern = exceptPattern;
        if (exceptPattern != null) {
            this.exceptColumns = null;
        }
        return this;
    }

    public boolean isStrict() {
        return strict;
    }

    public ColumnsTransformer setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public List<SelectItem<?>> getReplaceItems() {
        return replaceItems;
    }

    public ColumnsTransformer setReplaceItems(List<SelectItem<?>> replaceItems) {
        this.replaceItems = replaceItems;
        return this;
    }

    public List<SelectItem<?>> getRenameItems() {
        return renameItems;
    }

    public ColumnsTransformer setRenameItems(List<SelectItem<?>> renameItems) {
        this.renameItems = renameItems;
        return this;
    }

    public boolean isParenthesized() {
        return parenthesized;
    }

    public ColumnsTransformer setParenthesized(boolean parenthesized) {
        this.parenthesized = parenthesized;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        switch (type) {
            case APPLY:
                builder.append("APPLY(").append(applyExpression).append(")");
                break;
            case EXCEPT:
                builder.append("EXCEPT").append(strict ? " STRICT" : "").append(" ")
                        .append(exceptPattern != null ? exceptPattern : exceptColumns);
                break;
            case EXCLUDE:
                builder.append("EXCLUDE ").append(parenthesized || exceptColumns.size() != 1
                        ? exceptColumns
                        : exceptColumns.get(0));
                break;
            case REPLACE:
                boolean replaceParenthesized = parenthesized || replaceItems.size() != 1;
                builder.append("REPLACE").append(strict ? " STRICT" : "")
                        .append(replaceParenthesized ? "(" : " ")
                        .append(Select.getStringList(replaceItems))
                        .append(replaceParenthesized ? ")" : "");
                break;
            case RENAME:
                boolean renameParenthesized = parenthesized || renameItems.size() != 1;
                builder.append("RENAME ").append(renameParenthesized ? "(" : "")
                        .append(Select.getStringList(renameItems))
                        .append(renameParenthesized ? ")" : "");
                break;
            default:
                throw new IllegalStateException("Unhandled ColumnsTransformerType: " + type);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
