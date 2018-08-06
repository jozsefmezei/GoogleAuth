package hu.ponte.mobile.twoaf.exception;

public class TwoafException extends RuntimeException {

    public enum TwoafReason { NO_DATE_FORMAT, EMPTY_SECRET }

    private TwoafReason reason;

    public TwoafException(Throwable e, TwoafReason reason){
        initCause(e);
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        if (reason == TwoafReason.NO_DATE_FORMAT) return "Incompatible Date parser pattern.";
        if (reason == TwoafReason.EMPTY_SECRET) return "Secret is empty.";
        return super.getMessage();
    }
}
