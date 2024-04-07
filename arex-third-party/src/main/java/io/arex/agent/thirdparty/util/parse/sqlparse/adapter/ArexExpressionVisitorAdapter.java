package io.arex.agent.thirdparty.util.parse.sqlparse.adapter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.thirdparty.util.parse.sqlparse.util.ExpressionExtractor;
import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.CollateExpression;
import net.sf.jsqlparser.expression.ConnectByRootOperator;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonAggregateFunction;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NextValExpression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.RowGetExpression;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.TryCastExpression;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.ValueListExpression;
import net.sf.jsqlparser.expression.VariableAssignment;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.XMLSerializeExpr;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsDistinctExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class ArexExpressionVisitorAdapter implements ExpressionVisitor {
    private ObjectNode sqlObj;
    private ArrayNode andOrObj;
    private ObjectNode columnsObj;

    public ArexExpressionVisitorAdapter(ObjectNode object) {
        sqlObj = object;
        andOrObj = (ArrayNode) object.get(DbParseConstants.AND_OR);
        columnsObj = (ObjectNode) object.get(DbParseConstants.COLUMNS);
    }

    // scores >> 4
    @Override
    public void visit(BitwiseRightShift aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    // scores << 4
    @Override
    public void visit(BitwiseLeftShift aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(NullValue nullValue) {
        columnsObj.put(ExpressionExtractor.extract(nullValue),
                DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Function function) {
        columnsObj.put(ExpressionExtractor.extract(function), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        columnsObj.put(ExpressionExtractor.extract(signedExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        columnsObj.put(ExpressionExtractor.extract(jdbcParameter), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        columnsObj.put(ExpressionExtractor.extract(jdbcNamedParameter), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        columnsObj.put(ExpressionExtractor.extract(doubleValue), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(LongValue longValue) {
        columnsObj.put(ExpressionExtractor.extract(longValue), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(HexValue hexValue) {
        columnsObj.put(ExpressionExtractor.extract(hexValue), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(DateValue dateValue) {
        columnsObj.put(ExpressionExtractor.extract(dateValue), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TimeValue timeValue) {
        columnsObj.put(ExpressionExtractor.extract(timeValue), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        columnsObj.put(ExpressionExtractor.extract(timestampValue), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        columnsObj.put(ExpressionExtractor.extract(parenthesis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(StringValue stringValue) {
        columnsObj.put(ExpressionExtractor.extract(stringValue), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Addition addition) {
        columnsObj.put(ExpressionExtractor.extract(addition), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Division division) {
        columnsObj.put(ExpressionExtractor.extract(division), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IntegerDivision division) {
        columnsObj.put(ExpressionExtractor.extract(division), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Multiplication multiplication) {
        columnsObj.put(ExpressionExtractor.extract(multiplication), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Subtraction subtraction) {
        columnsObj.put(ExpressionExtractor.extract(subtraction), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AndExpression andExpression) {
        andOrObj.add(DbParseConstants.AND);
        ArexExpressionVisitorAdapter arexExpressionVisitorAdapter = new ArexExpressionVisitorAdapter(
                sqlObj);
        Expression leftExpression = andExpression.getLeftExpression();
        Expression rightExpression = andExpression.getRightExpression();
        leftExpression.accept(arexExpressionVisitorAdapter);
        rightExpression.accept(arexExpressionVisitorAdapter);
    }

    @Override
    public void visit(OrExpression orExpression) {
        andOrObj.add(DbParseConstants.OR);
        ArexExpressionVisitorAdapter arexExpressionVisitorAdapter = new ArexExpressionVisitorAdapter(
                sqlObj);
        Expression leftExpression = orExpression.getLeftExpression();
        Expression rightExpression = orExpression.getRightExpression();
        leftExpression.accept(arexExpressionVisitorAdapter);
        rightExpression.accept(arexExpressionVisitorAdapter);
    }

    @Override
    public void visit(XorExpression orExpression) {
        andOrObj.add(DbParseConstants.XOR);
        ArexExpressionVisitorAdapter arexExpressionVisitorAdapter = new ArexExpressionVisitorAdapter(
                sqlObj);
        Expression leftExpression = orExpression.getLeftExpression();
        Expression rightExpression = orExpression.getRightExpression();
        leftExpression.accept(arexExpressionVisitorAdapter);
        rightExpression.accept(arexExpressionVisitorAdapter);
    }

    @Override
    public void visit(Between between) {
        columnsObj.put(ExpressionExtractor.extract(between), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        String leftExpression = ExpressionExtractor.extract(equalsTo.getLeftExpression());
        String rightExpression = ExpressionExtractor.extract(equalsTo.getRightExpression());
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, equalsTo.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        String leftExpression = ExpressionExtractor.extract(greaterThan.getLeftExpression());
        String rightExpression = ExpressionExtractor.extract(greaterThan.getRightExpression());
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, greaterThan.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        String leftExpression = ExpressionExtractor.extract(greaterThanEquals.getLeftExpression());
        String rightExpression = ExpressionExtractor.extract(greaterThanEquals.getRightExpression());
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, greaterThanEquals.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(InExpression inExpression) {
        columnsObj.put(ExpressionExtractor.extract(inExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        columnsObj.put(ExpressionExtractor.extract(fullTextSearch), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        columnsObj.put(ExpressionExtractor.extract(isNullExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        columnsObj.put(ExpressionExtractor.extract(isBooleanExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        columnsObj.put(ExpressionExtractor.extract(likeExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(MinorThan minorThan) {
        String leftExpression = ExpressionExtractor.extract(minorThan.getLeftExpression());
        String rightExpression = ExpressionExtractor.extract(minorThan.getRightExpression());
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, minorThan.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        String leftExpression = ExpressionExtractor.extract(minorThanEquals.getLeftExpression());
        String rightExpression = ExpressionExtractor.extract(minorThanEquals.getRightExpression());
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, minorThanEquals.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        String leftExpression = ExpressionExtractor.extract(notEqualsTo.getLeftExpression());
        String rightExpression = ExpressionExtractor.extract(notEqualsTo.getRightExpression());
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, notEqualsTo.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(Column tableColumn) {
        columnsObj.put(ExpressionExtractor.extract(tableColumn), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(SubSelect subSelect) {
        columnsObj.put(ExpressionExtractor.extract(subSelect), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        columnsObj.put(ExpressionExtractor.extract(caseExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(WhenClause whenClause) {
        columnsObj.put(ExpressionExtractor.extract(whenClause), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        columnsObj.put(ExpressionExtractor.extract(existsExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        columnsObj.put(ExpressionExtractor.extract(anyComparisonExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Concat concat) {
        columnsObj.put(ExpressionExtractor.extract(concat), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Matches matches) {
        columnsObj.put(ExpressionExtractor.extract(matches), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        columnsObj.put(ExpressionExtractor.extract(bitwiseAnd), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        columnsObj.put(ExpressionExtractor.extract(bitwiseOr), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        columnsObj.put(ExpressionExtractor.extract(bitwiseXor), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(CastExpression cast) {
        columnsObj.put(ExpressionExtractor.extract(cast), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TryCastExpression cast) {
        columnsObj.put(ExpressionExtractor.extract(cast), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Modulo modulo) {
        columnsObj.put(ExpressionExtractor.extract(modulo), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        columnsObj.put(ExpressionExtractor.extract(aexpr), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        columnsObj.put(ExpressionExtractor.extract(eexpr), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        columnsObj.put(ExpressionExtractor.extract(iexpr), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        columnsObj.put(ExpressionExtractor.extract(oexpr), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        columnsObj.put(ExpressionExtractor.extract(rexpr), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        columnsObj.put(ExpressionExtractor.extract(jsonExpr), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        columnsObj.put(ExpressionExtractor.extract(jsonExpr), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        columnsObj.put(ExpressionExtractor.extract(regExpMySQLOperator), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(UserVariable var) {
        columnsObj.put(ExpressionExtractor.extract(var), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(NumericBind bind) {
        columnsObj.put(ExpressionExtractor.extract(bind), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(KeepExpression aexpr) {
        columnsObj.put(ExpressionExtractor.extract(aexpr), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        columnsObj.put(ExpressionExtractor.extract(groupConcat), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ValueListExpression valueList) {
        columnsObj.put(ExpressionExtractor.extract(valueList), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        columnsObj.put(ExpressionExtractor.extract(rowConstructor), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {
        columnsObj.put(ExpressionExtractor.extract(rowGetExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(OracleHint hint) {
        columnsObj.put(ExpressionExtractor.extract(hint), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        columnsObj.put(ExpressionExtractor.extract(timeKeyExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        columnsObj.put(ExpressionExtractor.extract(literal), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(NotExpression aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(NextValExpression aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(CollateExpression aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(SimilarToExpression aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ArrayExpression aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ArrayConstructor aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(VariableAssignment aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(XMLSerializeExpr aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TimezoneExpression aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JsonAggregateFunction aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JsonFunction aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ConnectByRootOperator aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(OracleNamedFunctionParameter aThis) {
        columnsObj.put(ExpressionExtractor.extract(aThis), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AllColumns allColumns) {
        columnsObj.put(ExpressionExtractor.extract(allColumns), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        columnsObj.put(ExpressionExtractor.extract(allTableColumns), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AllValue allValue) {
        columnsObj.put(ExpressionExtractor.extract(allValue), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IsDistinctExpression isDistinctExpression) {
        columnsObj.put(ExpressionExtractor.extract(isDistinctExpression), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(GeometryDistance geometryDistance) {
        columnsObj.put(ExpressionExtractor.extract(geometryDistance), DbParseConstants.EMPTY);
    }
}
