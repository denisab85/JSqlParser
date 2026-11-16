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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionScopeLadderTest {

    @Test
    void subqueryLadderInnerToOuter() {
        String sql = "SELECT * FROM T WHERE ID IN (SELECT X FROM U WHERE Z = 1)";
        int caret = sql.indexOf("Z = 1") + 2;
        List<ExecutionScope> ladder =
                ExecutionScopeLadder.inStatement(sql, caret, ScopeAnchor.SELECT_OR_STATEMENT);
        assertTrue(ladder.size() >= 2, () -> ladder.toString());
        assertTrue(ladder.get(0).getSql().contains("SELECT X FROM U"));
        assertEquals("SELECT * FROM T WHERE ID IN (SELECT X FROM U WHERE Z = 1)",
                ladder.get(ladder.size() - 1).getSql());
    }

    @Test
    void scriptPicksSecondStatement() {
        String script = "SELECT 1;\nSELECT 2 FROM SYSIBM.SYSDUMMY1";
        int caret = script.lastIndexOf('2');
        List<ExecutionScope> ladder =
                ExecutionScopeLadder.inScript(script, caret, ScopeAnchor.SELECT_OR_STATEMENT);
        assertEquals(1, ladder.size());
        assertEquals("SELECT 2 FROM SYSIBM.SYSDUMMY1", ladder.get(0).getSql());
        assertTrue(ladder.get(0).getRange().start() > 0);
    }

    @Test
    void semicolonInsideStringDoesNotSplit() {
        String sql = "SELECT 'a;b' FROM T";
        List<SourceRange> parts = SqlScriptScanner.topLevelStatements(sql);
        assertEquals(1, parts.size());
        assertEquals(sql, parts.get(0).slice(sql));
    }

    @Test
    void parenthesedSelectIsSingleScope() {
        String sql = "(SELECT A FROM T)";
        int caret = sql.indexOf("A FROM");
        List<ExecutionScope> ladder =
                ExecutionScopeLadder.inStatement(sql, caret, ScopeAnchor.SELECT_OR_STATEMENT);
        assertEquals(1, ladder.size());
        assertEquals("(SELECT A FROM T)", ladder.get(0).getSql());
        assertEquals("ParenthesedSelect", ladder.get(0).getKind());
    }

    @Test
    void unionBranchDoesNotExposeUnionOperatorOrSiblingSelects() {
        String sql = "SELECT A FROM T UNION SELECT B FROM U";
        int caret = sql.indexOf("B FROM");
        List<ExecutionScope> ladder =
                ExecutionScopeLadder.inStatement(sql, caret, ScopeAnchor.SELECT_OR_STATEMENT);
        assertEquals(1, ladder.size());
        assertEquals("SELECT B FROM U", ladder.get(0).getSql());
    }

    @Test
    void parenthesedFromSubqueryOmitsTopLevelAlias() {
        String sql = "select * from (select * from table) as s where n > 0";
        int caret = sql.indexOf("from table") + 4;
        List<ExecutionScope> ladder =
                ExecutionScopeLadder.inStatement(sql, caret, ScopeAnchor.SELECT_OR_STATEMENT);
        assertEquals(2, ladder.size());
        assertEquals("select * from table", ladder.get(0).getSql());
    }

    @Test
    void subqueryInParensIsSingleInnerScope() {
        String sql = "SELECT * FROM T WHERE ID = (SELECT X FROM U WHERE Z = 1)";
        int caret = sql.indexOf("Z = 1");
        List<ExecutionScope> ladder =
                ExecutionScopeLadder.inStatement(sql, caret, ScopeAnchor.SELECT_OR_STATEMENT);
        assertEquals(2, ladder.size());
        assertEquals("(SELECT X FROM U WHERE Z = 1)", ladder.get(0).getSql());
        assertTrue(ladder.get(1).getSql().startsWith("SELECT * FROM T"));
    }
}
