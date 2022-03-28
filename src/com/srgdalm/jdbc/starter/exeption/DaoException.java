package com.srgdalm.jdbc.starter.exeption;

public class DaoException extends RuntimeException {
    public DaoException(Throwable throwable) {
        super(throwable);
    }
}
