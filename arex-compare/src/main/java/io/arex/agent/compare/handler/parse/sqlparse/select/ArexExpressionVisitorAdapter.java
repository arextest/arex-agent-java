package io.arex.agent.compare.handler.parse.sqlparse.select;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.utils.JacksonHelperUtil;
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
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    // scores << 4
    @Override
    public void visit(BitwiseLeftShift aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(NullValue nullValue) {
        columnsObj.put(nullValue.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Function function) {
        columnsObj.put(function.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        columnsObj.put(signedExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        columnsObj.put(jdbcParameter.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        columnsObj.put(jdbcNamedParameter.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        columnsObj.put(doubleValue.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(LongValue longValue) {
        columnsObj.put(longValue.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(HexValue hexValue) {
        columnsObj.put(hexValue.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(DateValue dateValue) {
        columnsObj.put(dateValue.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TimeValue timeValue) {
        columnsObj.put(timeValue.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        columnsObj.put(timestampValue.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        columnsObj.put(parenthesis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(StringValue stringValue) {
        columnsObj.put(stringValue.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Addition addition) {
        columnsObj.put(addition.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Division division) {
        columnsObj.put(division.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IntegerDivision division) {
        columnsObj.put(division.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Multiplication multiplication) {
        columnsObj.put(multiplication.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Subtraction subtraction) {
        columnsObj.put(subtraction.toString(), DbParseConstants.EMPTY);
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
        columnsObj.put(between.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        String leftExpression = equalsTo.getLeftExpression().toString();
        String rightExpression = equalsTo.getRightExpression().toString();
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, equalsTo.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        String leftExpression = greaterThan.getLeftExpression().toString();
        String rightExpression = greaterThan.getRightExpression().toString();
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, greaterThan.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        String leftExpression = greaterThanEquals.getLeftExpression().toString();
        String rightExpression = greaterThanEquals.getRightExpression().toString();
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, greaterThanEquals.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(InExpression inExpression) {
        columnsObj.put(inExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        columnsObj.put(fullTextSearch.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        columnsObj.put(isNullExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        columnsObj.put(isBooleanExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        columnsObj.put(likeExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(MinorThan minorThan) {
        String leftExpression = minorThan.getLeftExpression().toString();
        String rightExpression = minorThan.getRightExpression().toString();
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, minorThan.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        String leftExpression = minorThanEquals.getLeftExpression().toString();
        String rightExpression = minorThanEquals.getRightExpression().toString();
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, minorThanEquals.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        String leftExpression = notEqualsTo.getLeftExpression().toString();
        String rightExpression = notEqualsTo.getRightExpression().toString();
        ObjectNode fieldObj = JacksonHelperUtil.getObjectNode();
        fieldObj.put(DbParseConstants.OPERATOR, notEqualsTo.getStringExpression());
        fieldObj.put(DbParseConstants.RIGHT, rightExpression);
        columnsObj.set(leftExpression, fieldObj);
    }

    @Override
    public void visit(Column tableColumn) {
        columnsObj.put(tableColumn.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(SubSelect subSelect) {
        columnsObj.put(subSelect.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        columnsObj.put(caseExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(WhenClause whenClause) {
        columnsObj.put(whenClause.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        columnsObj.put(existsExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        columnsObj.put(anyComparisonExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Concat concat) {
        columnsObj.put(concat.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Matches matches) {
        columnsObj.put(matches.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        columnsObj.put(bitwiseAnd.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        columnsObj.put(bitwiseOr.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        columnsObj.put(bitwiseXor.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(CastExpression cast) {
        columnsObj.put(cast.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TryCastExpression cast) {
        columnsObj.put(cast.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(Modulo modulo) {
        columnsObj.put(modulo.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        columnsObj.put(aexpr.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        columnsObj.put(eexpr.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        columnsObj.put(iexpr.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        columnsObj.put(oexpr.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        columnsObj.put(rexpr.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        columnsObj.put(jsonExpr.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        columnsObj.put(jsonExpr.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        columnsObj.put(regExpMySQLOperator.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(UserVariable var) {
        columnsObj.put(var.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(NumericBind bind) {
        columnsObj.put(bind.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(KeepExpression aexpr) {
        columnsObj.put(aexpr.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        columnsObj.put(groupConcat.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ValueListExpression valueList) {
        columnsObj.put(valueList.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        columnsObj.put(rowConstructor.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {
        columnsObj.put(rowGetExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(OracleHint hint) {
        columnsObj.put(hint.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        columnsObj.put(timeKeyExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        columnsObj.put(literal.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(NotExpression aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(NextValExpression aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(CollateExpression aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(SimilarToExpression aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ArrayExpression aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ArrayConstructor aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(VariableAssignment aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(XMLSerializeExpr aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(TimezoneExpression aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JsonAggregateFunction aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(JsonFunction aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(ConnectByRootOperator aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(OracleNamedFunctionParameter aThis) {
        columnsObj.put(aThis.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AllColumns allColumns) {
        columnsObj.put(allColumns.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        columnsObj.put(allTableColumns.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AllValue allValue) {
        columnsObj.put(allValue.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(IsDistinctExpression isDistinctExpression) {
        columnsObj.put(isDistinctExpression.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(GeometryDistance geometryDistance) {
        columnsObj.put(geometryDistance.toString(), DbParseConstants.EMPTY);
    }
}
