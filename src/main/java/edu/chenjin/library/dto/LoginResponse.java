package edu.chenjin.library.dto;

public record LoginResponse(String token, long expiresIn, UserView user) {
}
