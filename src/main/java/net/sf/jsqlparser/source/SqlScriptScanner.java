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

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a script on statement terminators outside of literals and comments. Does not model
 * procedural {@code BEGIN…END} blocks.
 */
public final class SqlScriptScanner {

    private SqlScriptScanner() {}

    public static List<SourceRange> topLevelStatements(String text) {
        List<SourceRange> out = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return out;
        }
        int statementStart = 0;
        int i = 0;
        while (i < text.length()) {
            int skip = skipCommentOrString(text, i);
            if (skip > i) {
                i = skip;
                continue;
            }
            if (text.charAt(i) == ';') {
                int end = trimEnd(text, statementStart, i);
                if (end > statementStart) {
                    out.add(new SourceRange(statementStart, end));
                }
                statementStart = skipLeadingWhitespace(text, i + 1);
            }
            i++;
        }
        int tailEnd = trimEnd(text, statementStart, text.length());
        if (tailEnd > statementStart) {
            out.add(new SourceRange(statementStart, tailEnd));
        }
        return out;
    }

    public static SourceRange statementContaining(String text, int caret) {
        if (text == null || text.isEmpty()) {
            return SourceRange.EMPTY;
        }
        int offset = Math.max(0, Math.min(caret, text.length()));
        for (SourceRange range : topLevelStatements(text)) {
            if (range.contains(offset)) {
                return range;
            }
        }
        return new SourceRange(0, text.length());
    }

    private static int skipLeadingWhitespace(String text, int from) {
        int i = from;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i;
    }

    private static int trimEnd(String text, int start, int end) {
        int trimmedEnd = end;
        while (trimmedEnd > start && Character.isWhitespace(text.charAt(trimmedEnd - 1))) {
            trimmedEnd--;
        }
        return trimmedEnd;
    }

    public static int skipCommentOrString(String text, int index) {
        if (index >= text.length()) {
            return index;
        }
        char c = text.charAt(index);
        if (c == '-' && index + 1 < text.length() && text.charAt(index + 1) == '-') {
            int i = index + 2;
            while (i < text.length() && text.charAt(i) != '\n') {
                i++;
            }
            return i;
        }
        if (c == '/' && index + 1 < text.length() && text.charAt(index + 1) == '*') {
            int i = index + 2;
            while (i + 1 < text.length() && !(text.charAt(i) == '*' && text.charAt(i + 1) == '/')) {
                i++;
            }
            return Math.min(i + 2, text.length());
        }
        if (c == '\'') {
            return endOfQuoted(text, index, '\'');
        }
        if (c == '"') {
            return endOfQuoted(text, index, '"');
        }
        return index;
    }

    private static int endOfQuoted(String text, int start, char quote) {
        int i = start + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == quote) {
                if (i + 1 < text.length() && text.charAt(i + 1) == quote) {
                    i += 2;
                    continue;
                }
                return i + 1;
            }
            i++;
        }
        return text.length();
    }
}
