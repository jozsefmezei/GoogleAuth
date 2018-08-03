package hu.ponte.mobile.twoaf.exception;

public class TwoafException extends RuntimeException {

    public enum TwoafReason { NO_DATE_FORMAT }

    private TwoafReason reason;

    public TwoafException(Throwable e, TwoafReason reason){
        initCause(e);
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        if (reason == TwoafReason.NO_DATE_FORMAT) return "Incompatible Date parser pattern.";
        return super.getMessage();
    }
}
