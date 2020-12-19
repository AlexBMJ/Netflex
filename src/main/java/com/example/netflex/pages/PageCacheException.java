package com.example.netflex.pages;

public class PageCacheException extends Exception {
    private String message;
    public PageCacheException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
