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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds execution scopes from innermost {@link ScopeAnchor} anchor outward along parse parents.
 */
public final class ExecutionScopeLadder {

    private ExecutionScopeLadder() {}

    /**
     * Scopes inside one statement SQL string. {@code caretOffset} is 0-based in
     * {@code statementSql}.
     */
    public static List<ExecutionScope> inStatement(String statementSql, int caretOffset,
            ScopeAnchor anchor) {
        try {
            Statement statement = CCJSqlParserUtil.parse(statementSql);
            return inParsedStatement(statement, statementSql, caretOffset, 0, anchor);
        } catch (JSQLParserException ex) {
            return List.of();
        }
    }

    /**
     * Locates the top-level statement around {@code caretOffset} in a script, parses it, then
     * {@link #inStatement}.
     */
    public static List<ExecutionScope> inScript(String script, int caretOffset,
            ScopeAnchor anchor) {
        SourceRange statementSlice = SqlScriptScanner.statementContaining(script, caretOffset);
        if (statementSlice.isEmpty()) {
            return List.of();
        }
        String sql = statementSlice.slice(script);
        int caretInStatement = caretOffset - statementSlice.start();
        List<ExecutionScope> scopes = inStatement(sql, caretInStatement, anchor);
        List<ExecutionScope> shifted = new ArrayList<>(scopes.size());
        for (ExecutionScope scope : scopes) {
            shifted.add(new ExecutionScope(
                    scope.getKind(),
                    scope.getRange().shift(statementSlice.start()),
                    scope.getSql(),
                    scope.getModel()));
        }
        return shifted;
    }

    static List<ExecutionScope> inParsedStatement(
            Statement statement, String statementSql, int caretOffset, int baseOffset,
            ScopeAnchor anchor) {
        if (!(statement instanceof ASTNodeAccess)) {
            return List.of();
        }
        ASTNodeAccess access = (ASTNodeAccess) statement;
        if (access.getASTNode() == null) {
            return List.of();
        }
        List<NodeHit> hits = new ArrayList<>();
        collectHits(access.getASTNode(), statementSql, caretOffset, hits);
        if (hits.isEmpty()) {
            return List.of();
        }
        ASTNodeAccessImpl start = hits.stream()
                .filter(h -> anchor.test(h.getAccess()))
                .min(Comparator.comparingInt(h -> h.getRange().length()))
                .map(h -> AstNavigation.asImpl(h.getAccess()))
                .orElse(null);
        if (start == null) {
            return List.of();
        }
        List<ExecutionScope> ladder = new ArrayList<>();
        Set<SourceRange> seen = new LinkedHashSet<>();
        for (ASTNodeAccessImpl node = start; node != null; node = AstNavigation.parent(node)) {
            if (!anchor.test(node)) {
                continue;
            }
            if (skipSetOperationParent(node, start)) {
                continue;
            }
            SourceRange range = executionRange(node, statementSql);
            if (range == null || !seen.add(range)) {
                continue;
            }
            String sql = range.slice(statementSql).trim();
            String kind = scopeKind(node);
            ladder.add(new ExecutionScope(kind, range.shift(baseOffset), sql, node));
        }
        return collapseParenthesedDuplicates(ladder);
    }

    /**
     * A {@link SetOperationList} spans every union branch; skip it when the ladder starts on one
     * branch only.
     */
    private static boolean skipSetOperationParent(ASTNodeAccess access, ASTNodeAccessImpl start) {
        if (!(access instanceof SetOperationList)) {
            return false;
        }
        List<Select> selects = ((SetOperationList) access).getSelects();
        return selects != null && selects.stream().anyMatch(branch -> branch == start);
    }

    /**
     * Drop the inner {@link Select} when the next rung is a {@link ParenthesedSelect} for the same
     * model object (one executable scope including parentheses, not inner SQL and wrappers
     * separately).
     */
    private static List<ExecutionScope> collapseParenthesedDuplicates(List<ExecutionScope> ladder) {
        if (ladder.size() < 2) {
            return ladder;
        }
        List<ExecutionScope> out = new ArrayList<>(ladder.size());
        for (int i = 0; i < ladder.size(); i++) {
            ExecutionScope scope = ladder.get(i);
            if (i + 1 < ladder.size()) {
                Object outerModel = ladder.get(i + 1).getModel();
                if (outerModel instanceof ParenthesedSelect
                        && ((ParenthesedSelect) outerModel).getSelect() == scope.getModel()) {
                    continue;
                }
            }
            out.add(scope);
        }
        return out;
    }

    private static String scopeKind(ASTNodeAccess model) {
        return model.getClass().getSimpleName();
    }

    /**
     * For a parenthesized subquery used as a {@code FROM} item, the model span often includes
     * {@code AS alias}; execution scope is the inner {@link Select} only.
     */
    private static SourceRange executionRange(ASTNodeAccessImpl node, String statementSql) {
        SourceRange range = SourceSpans.rangeOrNull(node, statementSql);
        if (range == null) {
            return null;
        }
        if (node instanceof ParenthesedSelect) {
            ParenthesedSelect parenthesed = (ParenthesedSelect) node;
            if (parenthesed.getAlias() != null) {
                Select inner = parenthesed.getSelect();
                if (inner != null) {
                    SourceRange innerRange = SourceSpans.rangeOrNull(inner, statementSql);
                    if (innerRange != null) {
                        return innerRange;
                    }
                }
            }
        }
        return range;
    }

    private static void collectHits(Node treeNode, String sql, int caret, List<NodeHit> hits) {
        if (treeNode == null) {
            return;
        }
        Object value = treeNode.jjtGetValue();
        if (value instanceof ASTNodeAccess) {
            ASTNodeAccess access = (ASTNodeAccess) value;
            SourceRange range = SourceSpans.rangeOrNull(access, sql);
            if (range != null && range.contains(caret)) {
                hits.add(new NodeHit(access, range));
            }
        }
        for (int i = 0; i < treeNode.jjtGetNumChildren(); i++) {
            collectHits(treeNode.jjtGetChild(i), sql, caret, hits);
        }
    }

    private static final class NodeHit {
        private final ASTNodeAccess access;
        private final SourceRange range;

        NodeHit(ASTNodeAccess access, SourceRange range) {
            this.access = access;
            this.range = range;
        }

        ASTNodeAccess getAccess() {
            return access;
        }

        SourceRange getRange() {
            return range;
        }
    }
}
