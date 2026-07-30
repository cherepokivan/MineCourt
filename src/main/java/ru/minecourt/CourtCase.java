package ru.minecourt;

import java.util.UUID;

public record CourtCase(UUID plaintiffId, String plaintiffName, UUID defendantId, String defendantName,
                        String reason, long createdAt) {
}
