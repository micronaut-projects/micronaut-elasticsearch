package micronaut.example.service;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;

@Introspected
@ReflectiveAccess
public class Movie {

    private String imdb;
    private String title;

    public Movie(String imdb, String title) {
        this.imdb = imdb;
        this.title = title;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "imdb='" + imdb + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
