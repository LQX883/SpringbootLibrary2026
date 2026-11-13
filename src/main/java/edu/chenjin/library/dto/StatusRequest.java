package edu.chenjin.library.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusRequest(@NotBlank(message = "请选择状态") String status) {
}
