package io.codef.api.error;

import java.io.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodefException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(CodefException.class);
    private static final String LOG_WITH_CAUSE_FORMAT = "%s\n→ %s\n\n";

    private final CodefError codefError;

    private CodefException(CodefError codefError, Exception exception) {
        super(
            String.format(
                LOG_WITH_CAUSE_FORMAT,
                codefError.getMessage(),
                exception.getMessage()
            ),
            exception
        );
        this.codefError = codefError;

        log.error(codefError.getMessage());
        log.error(exception.getMessage());
    }

    private CodefException(
        CodefError codefError,
        String extraMessage
    ) {
        super(codefError.getMessage() + '\n' + extraMessage);
        log.error(codefError.getMessage());
        log.error(extraMessage);
        this.codefError = codefError;
    }

    private CodefException(CodefError codefError) {
        super(codefError.getMessage() + '\n');

        log.error("{}", codefError.getRawMessage());
        log.error("{}", codefError.getReferenceUrl().getRawUrl());

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
