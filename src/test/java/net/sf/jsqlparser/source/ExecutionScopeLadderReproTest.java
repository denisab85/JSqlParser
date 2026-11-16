package net.sf.jsqlparser.source;

import org.junit.jupiter.api.Test;

import java.util.List;

class ExecutionScopeLadderReproTest {

    @Test
    void parenthesizedStandaloneSelect() {
        String sql = "SELECT * FROM T WHERE ID = (SELECT X FROM U WHERE Z = 1)";
        int caret = sql.indexOf("Z = 1");
        printLadder(sql, caret);
        // TODO assert after fix
    }

    @Test
    void unionSecondBranch() {
        String sql = "SELECT A FROM T UNION SELECT B FROM U";
        int caret = sql.indexOf("B FROM");
        printLadder(sql, caret);
    }

    private static void printLadder(String sql, int caret) {
        List<ExecutionScope> ladder =
                ExecutionScopeLadder.inStatement(sql, caret, ScopeAnchor.SELECT_OR_STATEMENT);
        System.out.println("SQL: " + sql);
        System.out.println("Ladder size: " + ladder.size());
        for (ExecutionScope s : ladder) {
            System.out.printf(
                    "  %s [%d,%d) %s%n",
                    s.getKind(),
                    s.getRange().start(),
                    s.getRange().end(),
                    s.getSql().replace('\n', ' '));
        }
    }
}
