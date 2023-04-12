package micronaut.example;

import java.util.List;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import micronaut.example.controller.SearchMovieResponse;
import micronaut.example.service.Movie;
import micronaut.example.service.MovieService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class ElasticSearchTest {
    @Test
    void testElasticSearch(MovieService movieService) {
        String title = "Die Hard";
        movieService.saveMovie(new Movie("KJFDOD", title));
        SearchMovieResponse result = movieService.searchMovies(title);
        List<SearchMovieResponse.Hit> hits = result.getHits();
        Assertions.assertEquals(1, hits.size());
    }
}
