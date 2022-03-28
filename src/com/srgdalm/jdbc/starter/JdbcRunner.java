package com.srgdalm.jdbc.starter;

import com.srgdalm.jdbc.starter.util.ConnectionManager;
import org.postgresql.Driver;

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class JdbcRunner {
    public static void main(String[] args) throws SQLException {


    }

    private static List<Long> getTicketsByFlightId(String flightId) {
        String sql = """
                SELECt id
                FROM ticket
                WHERE flight_id =  %s
             
                """.formatted(flightId);
        try (var connection = ConnectionManager.open();
             var statement = connection.createStatement()) {

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
