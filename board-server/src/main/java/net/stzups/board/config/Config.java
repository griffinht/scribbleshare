package net.stzups.board.config;

public interface Config<T> {
    T get(String key);
}
