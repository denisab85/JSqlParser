# `net.sf.jsqlparser.source` (ca.denisab85 fork)

Public API for editor integration: map caret positions to parse-tree execution scopes.

## Types

| Class | Role |
|-------|------|
| `SourceRange` | 0-based half-open `[start, end)` in parsed SQL |
| `SourceSpans` | `SourceRange` from `ASTNodeAccess` / token `absoluteBegin`−1 |
| `SqlScriptScanner` | Lexical top-level `;` split (comments/strings aware) |
| `ExecutionScopeLadder` | Inner → outer scopes via AST parent chain |
| `ScopeAnchor` | `SELECT_OR_STATEMENT` (default) or `ANY` |
| `AstNavigation` | Null-safe parent walk on `ASTNodeAccessImpl` |

## Example

```java
String script = "SELECT 1; SELECT * FROM T WHERE ID IN (SELECT X FROM U WHERE Z = 1)";
int caret = script.indexOf("Z = 1") + 2;
List<ExecutionScope> ladder =
        ExecutionScopeLadder.inScript(script, caret, ScopeAnchor.SELECT_OR_STATEMENT);
// ladder.get(0).getSql() → inner SELECT …
// ladder.get(n).getSql() → full outer statement
```

Token offsets are already tracked by JavaCC (`TRACK_TOKENS`); this package does not change the grammar.

## Also in 5.4.164

- `ASTNodeAccessImpl.getParent()` returns `null` at the root instead of throwing.