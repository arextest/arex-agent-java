package io.arex.inst.database.mybatis3;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class ExecutorInstrumentation extends TypeInstrumentation {
    private static final String METHOD_NAME_QUERY = "query";
    private static final String METHOD_NAME_UPDATE = "update";
    private static final String METHOD_NAME_BATCH_UPDATE = "doUpdate";
    public static final String METHOD_NAME_BATCH_FLUSH = "doFlushStatements";
    private static final String CLASS_NAME_BATCH_EXECUTOR = "org.apache.ibatis.executor.BatchExecutor";
    private static final String CLASS_NAME_BASE_EXECUTOR = "org.apache.ibatis.executor.BaseExecutor";

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return  named(CLASS_NAME_BASE_EXECUTOR)
                .or(named(CLASS_NAME_BATCH_EXECUTOR))
                .and(not(nameStartsWith("com.sun.proxy.$Proxy")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return asList(new MethodInstrumentation(
                        named(METHOD_NAME_QUERY).and(isPublic())
                                .and(takesArguments(6))
                                .and(takesArgument(0, named("org.apache.ibatis.mapping.MappedStatement")))
                                .and(takesArgument(1, Object.class))
                                .and(takesArgument(5, named("org.apache.ibatis.mapping.BoundSql"))),
                        QueryAdvice.class.getName()),
                new MethodInstrumentation(
                        named(METHOD_NAME_UPDATE).and(isPublic())
                                .and(takesArguments(2))
                                .and(takesArgument(0, named("org.apache.ibatis.mapping.MappedStatement")))
                                .and(takesArgument(1, Object.class)),
                        UpdateAdvice.class.getName()),
                new MethodInstrumentation(
                        named(METHOD_NAME_BATCH_UPDATE).and(isPublic())
                                .and(takesArguments(2)),
                        BatchUpdateAdvice.class.getName()
                ),
                new MethodInstrumentation(
                        named(METHOD_NAME_BATCH_FLUSH).and(isPublic()),
                        BatchFlushAdvice.class.getName()
                )
        );
    }

    @SuppressWarnings("unused")
    public static class QueryAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onMethodEnter(@Advice.Argument(0) MappedStatement var1,
                                            @Advice.Argument(1) Object var2,
                                            @Advice.Argument(5) BoundSql boundSql,
                                            @Advice.Local("extractor") DatabaseExtractor extractor,
                                            @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            if (ContextManager.needRecordOrReplay()) {
                String originalSql = boundSql != null ? boundSql.getSql() : null;
                extractor = InternalExecutor.createExtractor(var1, originalSql, var2, METHOD_NAME_QUERY);
                if (ContextManager.needReplay()) {
                    mockResult = InternalExecutor.replay(extractor, var2);
                }
                return mockResult != null && mockResult.notIgnoreMockResult();
            }
            return false;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Argument(0) MappedStatement var1,
                                  @Advice.Argument(1) Object var2,
                                  @Advice.Thrown(readOnly = false) Throwable throwable,
                                  @Advice.Return(readOnly = false) List<?> result,
                                  @Advice.Local("extractor") DatabaseExtractor extractor,
                                  @Advice.Local("mockResult") MockResult mockResult) {
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = (List<?>) mockResult.getResult();
                }
                return;
            }

            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                InternalExecutor.record(extractor, var2, result, throwable);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class UpdateAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onMethodEnter(@Advice.Argument(0) MappedStatement var1,
                                            @Advice.Argument(1) Object var2,
                                            @Advice.Local("extractor") DatabaseExtractor extractor,
                                            @Advice.Local("mockResult") MockResult mockResult,
                                            @Advice.This BaseExecutor executor) {
            if (executor instanceof BatchExecutor) {
                return false;
            }

            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }

            if (ContextManager.needRecordOrReplay()) {
                /**
                 * Generate executor in advance, because the insert operation will modify sql and parameters,
                 * resulting in inconsistent record and replay
                 */
                extractor = InternalExecutor.createExtractor(var1, null, var2, METHOD_NAME_UPDATE);
                if (ContextManager.needReplay()) {
                    mockResult = InternalExecutor.replay(extractor, var1, var2);
                }
            }
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Argument(0) MappedStatement var1,
                                  @Advice.Argument(1) Object var2,
                                  @Advice.Thrown(readOnly = false) Throwable throwable,
                                  @Advice.Return(readOnly = false) Integer result,
                                  @Advice.Local("extractor") DatabaseExtractor extractor,
                                  @Advice.Local("mockResult") MockResult mockResult,
                                  @Advice.This BaseExecutor executor) {
            if (executor instanceof BatchExecutor) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = (Integer) mockResult.getResult();
                }
                return;
            }

            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                InternalExecutor.record(extractor, var1, var2, result, throwable);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class BatchUpdateAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.FieldValue(value = "batchResultList", readOnly = false) List<BatchResult> batchResults,
                                      @Advice.FieldValue(value = "currentSql", readOnly = false) String currentSql,
                                      @Advice.FieldValue(value = "currentStatement", readOnly = false) MappedStatement currentStatement,
                                      @Advice.Argument(0) MappedStatement ms,
                                      @Advice.Argument(1) Object parameterObject) {
            if (ContextManager.needReplay()) {
                String sql = ms.getBoundSql(parameterObject).getSql();
                if (sql.equals(currentSql) && ms.equals(currentStatement)) {
                    int last = batchResults.size() - 1;
                    batchResults.get(last).addParameterObject(parameterObject);
                    return true;
                }
                currentSql = sql;
                currentStatement = ms;
                batchResults.add(new BatchResult(ms, sql, parameterObject));
                return true;
            }
            return false;
        }
    }

    @SuppressWarnings("unused")
    public static class BatchFlushAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.FieldValue(value = "batchResultList") List<BatchResult> batchResults,
                                      @Advice.Argument(0) boolean isRollback,
                                      @Advice.Local("extractorList") List<DatabaseExtractor> extractorList,
                                      @Advice.Local("mockResult") MockResult mockResult) {

            if (isRollback || batchResults.isEmpty()) {
                return false;
            }

            if (ContextManager.needRecordOrReplay()) {
                extractorList = new ArrayList<>(batchResults.size());
                List<BatchResult> response = new ArrayList<>(batchResults.size());

                for (BatchResult batchResult : batchResults) {
                    MappedStatement ms = batchResult.getMappedStatement();
                    /**
                     * Generate executor list in advance, because the insert operation will modify sql and parameters,
                     * resulting in inconsistent record and replay
                     */
                    for (Object parameterObject : batchResult.getParameterObjects()) {
                        DatabaseExtractor extractor = InternalExecutor.createExtractor(ms, null, parameterObject, METHOD_NAME_BATCH_FLUSH);
                        if (ContextManager.needRecord()) {
                            extractorList.add(extractor);
                            continue;
                        }
                        InternalExecutor.replay(extractor, ms, parameterObject);
                    }
                    response.add(batchResult);
                }

                if (ContextManager.needReplay()) {
                    mockResult = MockResult.success(response);
                    return true;
                }
            }
            return false;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.FieldValue(value = "batchResultList",readOnly = false) List<BatchResult> batchResults,
                                  @Advice.FieldValue(value = "currentSql", readOnly = false) String currentSql,
                                  @Advice.Return(readOnly = false) List<BatchResult> result,
                                  @Advice.Argument(0) boolean isRollback,
                                  @Advice.Thrown(readOnly = false) Throwable throwable,
                                  @Advice.Local("extractorList") List<DatabaseExtractor> extractorList,
                                  @Advice.Local("mockResult") MockResult mockResult) {
            if (isRollback) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                result = (List<BatchResult>) mockResult.getResult();
                currentSql = null;
                batchResults.clear();
                return;
            }

            if (extractorList == null || extractorList.isEmpty()) {
                return;
            }

            if (ContextManager.needRecord()) {
                int cnt = 0;
                for (BatchResult batchResult : result) {
                    MappedStatement ms = batchResult.getMappedStatement();
                    for (Object parameterObject : batchResult.getParameterObjects()) {
                        if (cnt >= extractorList.size()) {
                            return;
                        }
                        InternalExecutor.record(extractorList.get(cnt), ms, parameterObject, StringUtil.EMPTY, throwable);
                        cnt++;
                    }
                }
            }
        }
    }
}
