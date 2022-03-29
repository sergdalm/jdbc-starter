package com.srgdalm.jdbc.starter.dto;

public record TicketFilter(int limit,
                           int offset,
                           String passengerName,
                           String seatNo) {
}
