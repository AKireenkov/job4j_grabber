package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String PAGE_NUMBER = "?page=";

    public static final int PAGES = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> posts = new ArrayList<>(parse.list(PAGE_LINK + PAGE_NUMBER));
        posts.forEach(System.out::println);
    }

    private static String getDescription(String link) throws IOException {
        Connection description = Jsoup.connect(link);
        Document documentDescription = description.get();
        Element rows = documentDescription.selectFirst(".style-ugc");
        return rows.text();
    }

    private Post getPost(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dateElement = element.select("time").first();

        String vacancyName = titleElement.text();
        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String dateTime = dateElement.attr("datetime");
        String description;
        LocalDateTime created;
        try {
            created = dateTimeParser.parse(dateTime);
            description = getDescription(link);
        } catch (ParseException | IOException e) {
            throw new IllegalArgumentException();
        }
        return new Post(vacancyName, link, description, created);
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();

        for (int i = 1; i <= PAGES; i++) {
            try {
                Connection connection = Jsoup.connect(link + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> posts.add(getPost(row)));
            } catch (IOException e) {
                throw new IllegalArgumentException();
            }
        }
        return posts;
    }
}
