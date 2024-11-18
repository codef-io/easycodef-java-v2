package io.codef.api.error;

import java.io.Serial;

public class CodefException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String LOG_WITH_CAUSE_FORMAT = "%s\n→ %s\n\n";

    private final CodefError codefError;

    private CodefException(CodefError codefError, Exception exception) {
        super(String.format(LOG_WITH_CAUSE_FORMAT, codefError.getMessage(), exception.getMessage()), exception);
        this.codefError = codefError;
    }

    private CodefException(CodefError codefError, String extraMessage) {
        super(codefError.getMessage() + '\n' + extraMessage);
        this.codefError = codefError;
    }

    private CodefException(CodefError codefError) {
        super(codefError.getMessage() + '\n');
        this.codefError = codefError;
    }

    public static CodefException from(CodefError codefError) {
        return new CodefException(codefError);
    }

    public static CodefException of(
            CodefError codefError,
            Exception exception
    ) {
        return new CodefException(codefError, exception);
    }

    public static CodefException of(
            CodefError codefError,
            String extraMessage
    ) {
        return new CodefException(codefError, extraMessage);
    }

    public CodefError getError() {
        return codefError;
    }
}
