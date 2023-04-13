package micronaut.example;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
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
        Movie result = movieService.searchMovies(title);
        Assertions.assertNotNull(result);
    }
}
