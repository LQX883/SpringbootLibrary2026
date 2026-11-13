package edu.chenjin.library.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(@NotNull(message = "请选择图书") Long bookId) {
}
