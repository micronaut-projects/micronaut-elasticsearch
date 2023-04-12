package micronaut.example.controller;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;


@Introspected
public class CreateMovieRequest {

    @NotBlank
    private String imdb;

    @NotBlank
    private String title;

    public CreateMovieRequest(@NotBlank String imdb,
                              @NotBlank String title) {
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
}
