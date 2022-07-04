package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.grabber.utils.LocalDateTime;

import java.io.IOException;
import java.text.ParseException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String PAGE_NUMBER = "?page=";

    private final HabrCareerDateTimeParser habrCareerDateTimeParser;

    public HabrCareerParse(HabrCareerDateTimeParser habrCareerDateTimeParser) {
        this.habrCareerDateTimeParser = habrCareerDateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + PAGE_NUMBER + i);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");

            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select("time").first();
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String dateTime = dateElement.attr("datetime");
                LocalDateTime localDateTime = new HabrCareerDateTimeParser();
                try {
                    localDateTime = localDateTime.parse(dateTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                System.out.printf("%s %s %s%n", vacancyName, link, localDateTime);
                try {
                    System.out.println(retrieveDescription(link));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        Connection description = Jsoup.connect(link);
        Document documentDescription = description.get();
        Element rows = documentDescription.selectFirst(".style-ugc");
        return rows.text();
    }
}
