package micronaut.example;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.elasticsearch.ElasticSearch;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import micronaut.example.service.Movie;
import micronaut.example.service.MovieService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import java.util.Map;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@DisabledInNativeImage
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ElasticSearchTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return ElasticSearch.getProperties();
    }

    @Test
    void testElasticSearch(MovieService movieService) {
        String title = "Die Hard";
        movieService.saveMovie(new Movie("KJFDOD", title));
        await().atMost(10, SECONDS).until(() ->
            movieService.searchMovies(title) != null
        );
        Movie result = movieService.searchMovies(title);
        Assertions.assertNotNull(result);
    }
}
