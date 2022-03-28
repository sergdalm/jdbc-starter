package com.srgdalm.jdbc.starter.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {
    public static final String URL_KEY = "db.url";
    public static final String USERNAME_KEY = "db.username";
    public static final String PASSWORD_KEY = "db.password";

    private ConnectionManager() {
    }

    static {
        open();
    }

    public static Connection open()  {
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // До Java 1.8 были проблемы с подгрузкой драйверов, которые мы подключали
    // через дополнительные библиотеки, они автоматически сами не находились в classpath,
    // их необходимо было загружать.
    // С помощью Class.forName() мы загружали класс, который мы передали с помощью строки
    // в память JVM. После Java 1.8 эта память называется meta space
    // Таким образом будет код, который будет работать даже в старых версиях Java
    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // в данном случае обязательно надо пробрасывать исключение
            // чтобы программа остановилась в случе если класс не подгрузится
            throw new RuntimeException();
        }
    }
}
