package edu.chenjin.library.dto;

public record StatsOverview(
        long totalBooks,
        long totalCopies,
        long availableCopies,
        long totalUsers,
        long totalLoans,
        long activeLoans,
        long overdueLoans,
        long waitingReservations
) {
}
