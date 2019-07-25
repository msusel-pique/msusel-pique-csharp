package qatch.csharp;

/**
 * Enum of FxCop MessageLevel (priority) values used for string -> int translation
 */
public enum FxcopPriority {
    CriticalError(1),
    Error(2),
    CriticalWarning(3),
    Warning(4),
    Information(5);

    private int code;

    FxcopPriority(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}