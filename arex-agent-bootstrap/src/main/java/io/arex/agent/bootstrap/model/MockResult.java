package io.arex.agent.bootstrap.model;

public class MockResult {
    private final boolean ignoreMockResult;
    private final Object result;
    private final Throwable throwable;

    private MockResult(boolean ignoreMockResult, Object mockResult, Throwable throwable) {
        this.ignoreMockResult = ignoreMockResult;
        this.result = mockResult;
        this.throwable = throwable;
    }

    public boolean isIgnoreMockResult() {
        return ignoreMockResult;
    }

    public boolean notIgnoreMockResult() {
        return !isIgnoreMockResult();
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Object getResult() {
        return result;
    }

    public static MockResult success(boolean ignoreMockResult, Object mockResult) {
        if (mockResult instanceof Throwable) {
            return new MockResult(ignoreMockResult, null, (Throwable) mockResult);
        }
        return new MockResult(ignoreMockResult, mockResult, null);
    }

    public static MockResult success(Object mockResult) {
        return success(false, mockResult);
    }
}
