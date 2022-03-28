package com.srgdalm.jdbc.starter;

import com.srgdalm.jdbc.starter.util.ConnectionManager;
import org.postgresql.Driver;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcRunner {
    public static void main(String[] args) throws SQLException {
//        Long flight_id = 2L;
//        var result = getTicketsByFlightId(flight_id);
//        System.out.println(result);
//        var result = getFlightsBetween(LocalDate.of(2020,1,1).atStartOfDay(), LocalDateTime.now());
//        System.out.println(result);
        checkMetaData();

    }

    private static void checkMetaData()  throws SQLException{
        try(var connection = ConnectionManager.open()) {
            var metaData = connection.getMetaData();
            var catalogs = metaData.getCatalogs();
            while(catalogs.next()) {
                var catalog = catalogs.getString(1);
                var schemas = metaData.getSchemas();
                while(schemas.next()) {
                    var scheme = schemas.getString("TABLE_SCHEM");
                    var tables = metaData.getTables(catalog, scheme, "%", new String[] {"TABLE"});
                    if(scheme.equals("public")) {
                        while(tables.next()) {
                            System.out.println(tables.getString("TABLE_NAME"));
                        }
                    }
                }
            }
        }
    }

    private static List<Long> getFlightsBetween(LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT id
                FROM flight
                WHERE  departure_date BETWEEN ? AND ?
                """;

        List<Long> result = new ArrayList<>();

        try(var connection = ConnectionManager.open();
            var prepareStatement = connection.prepareStatement(sql)) {
            prepareStatement.setFetchSize(50);
            prepareStatement.setQueryTimeout(10);
            prepareStatement.setMaxRows(100);
            System.out.println(prepareStatement);
            prepareStatement.setTimestamp(1, Timestamp.valueOf(start));
            System.out.println(prepareStatement);
            prepareStatement.setTimestamp(2, Timestamp.valueOf(end));
            System.out.println(prepareStatement);
            var resultSet = prepareStatement.executeQuery();
            while(resultSet.next()) {
                result.add((Long)resultSet.getObject("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private static List<Long> getTicketsByFlightId(Long flightId) {
        String sql = """
                SELECT id
                FROM ticket
                WHERE flight_id =  ?
             
                """;
        List<Long> result = new ArrayList<>();
        try (var connection = ConnectionManager.open();
             var prepareStatement = connection.prepareStatement(sql)) {
            prepareStatement.setLong(1, flightId);
            var resultSet = prepareStatement.executeQuery();
            while(resultSet.next()) {
                result.add(resultSet.getObject("id", Long.class)); // null-safe
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
