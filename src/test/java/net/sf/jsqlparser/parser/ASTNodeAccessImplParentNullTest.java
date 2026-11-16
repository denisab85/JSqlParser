/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class ASTNodeAccessImplParentNullTest {

    @Test
    void rootStatementHasNullParent() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT 1");
        assertNull(select.getParent());
    }
}
