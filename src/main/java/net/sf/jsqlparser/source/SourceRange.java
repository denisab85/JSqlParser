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

import java.util.Objects;

/**
 * Half-open character range {@code [start, end)} in the SQL text that was parsed. Offsets are
 * 0-based for editor and {@link String#substring(int, int)} use.
 */
public final class SourceRange {

    public static final SourceRange EMPTY = new SourceRange(0, 0);

    private final int start;
    private final int end;

    public SourceRange(int start, int end) {
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("invalid range: " + start + ".." + end);
        }
        this.start = start;
        this.end = end;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public int length() {
        return end - start;
    }

    public boolean isEmpty() {
        return end <= start;
    }

    public boolean contains(int offset) {
        return offset >= start && offset < end;
    }

    public String slice(String sql) {
        Objects.requireNonNull(sql, "sql");
        if (isEmpty()) {
            return "";
        }
        if (end > sql.length()) {
            throw new IndexOutOfBoundsException(
                    "range end " + end + " beyond sql length " + sql.length());
        }
        return sql.substring(start, end);
    }

    public SourceRange shift(int delta) {
        return delta == 0 ? this : new SourceRange(start + delta, end + delta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SourceRange)) {
            return false;
        }
        SourceRange other = (SourceRange) o;
        return start == other.start && end == other.end;
    }

    @Override
    public int hashCode() {
        return 31 * start + end;
    }

    @Override
    public String toString() {
        return "[" + start + ".." + end + ")";
    }
}
