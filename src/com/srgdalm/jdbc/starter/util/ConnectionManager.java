package com.srgdalm.jdbc.starter.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {
    public static final String URL_KEY = "db.url";
    public static final String USERNAME_KEY = "db.username";
    public static final String PASSWORD_KEY = "db.password";
    public static final String PULL_SIZE_KEY = "db.pool.size";
    public static final Integer DEFAULT_POOL_SIZE = 10;
    public static BlockingQueue<Connection> pool;
    public static List<Connection> sourceConnections;

    private ConnectionManager() {
    }

    private static void initConnectionPool() {
        var poolSize = PropertiesUtil.get(PULL_SIZE_KEY);
        var size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(size);
        sourceConnections = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            var connection = open();
            var proxyConnection = (Connection) Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(), new Class[]{Connection.class},
                (proxy, method, args) -> method.getName().equals("close")
                        ? pool.add((Connection) proxy)
                        : method.invoke(connection, args));
            pool.add(proxyConnection);
            sourceConnections.add(connection);
        }
    }

    public static Connection get() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        loadDriver();
        initConnectionPool();
    }

    private static Connection open()  {
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

    public static void closePool() {
        try {
            for (Connection sourceConnection : sourceConnections) {
                sourceConnection.close();
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
