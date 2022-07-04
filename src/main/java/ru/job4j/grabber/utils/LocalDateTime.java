package ru.job4j.grabber.utils;

import java.text.ParseException;

public interface LocalDateTime {
    LocalDateTime parse(String parse) throws ParseException;
}
