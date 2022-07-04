package ru.job4j.grabber.utils;

import java.time.LocalDate;
import java.time.LocalTime;

public class HabrCareerDateTimeParser implements LocalDateTime {
    private LocalDate localDate;
    private LocalTime localTime;

    public HabrCareerDateTimeParser() {
    }

    public HabrCareerDateTimeParser(LocalDate localDate, LocalTime localTime) {
        this.localDate = localDate;
        this.localTime = localTime;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    @Override
    public LocalDateTime parse(String parse) {
        LocalDate localDate = LocalDate.parse(parse.substring(0, parse.indexOf('T')));
        LocalTime localTime = LocalTime.parse(parse.substring(parse.indexOf('T') + 1, parse.indexOf('+')));
        return new HabrCareerDateTimeParser(localDate, localTime);
    }

    @Override
    public String toString() {
        return localDate + " " + localTime;
    }
}


