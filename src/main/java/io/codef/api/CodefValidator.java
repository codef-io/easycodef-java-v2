package io.codef.api;

import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;

import java.util.Optional;
import java.util.UUID;

public final class CodefValidator {

    private CodefValidator() {
    }

    public static <T> T requireNonNullElseThrow(T object, CodefError codefError) {
        return Optional.ofNullable(object)
                .orElseThrow(() -> CodefException.from(codefError));
    }

    public static void requireValidUUIDPattern(String uuid, CodefError codefError) {
        final String UUID_REGULAR_EXPRESSION = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

        Optional.ofNullable(uuid)
                .filter(uuids -> uuids.matches(UUID_REGULAR_EXPRESSION))
                .map(UUID::fromString)
                .orElseThrow(() -> CodefException.from(codefError));
    }
}
