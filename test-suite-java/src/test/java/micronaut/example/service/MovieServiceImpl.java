package micronaut.example.service;

import java.util.Iterator;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import micronaut.example.exception.MovieServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MovieServiceImpl implements MovieService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieServiceImpl.class);

    @Value("${elasticsearch.indexes.movies}")
    String moviesIndex;

    private final ElasticsearchClient client;

    public MovieServiceImpl(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public String saveMovie(Movie movie) {
        try {
            IndexRequest<Movie> indexRequest = new IndexRequest.Builder<Movie>()
                .index(moviesIndex)
                .document(movie)
                .build();

            IndexResponse indexResponse = client.index(indexRequest);
            String id = indexResponse.id();

            LOG.info("Document for '{}' {} successfully in ES. The id is: {}", movie, indexResponse.result(), id);
            return id;
        } catch (Exception e) {
            String errorMessage = String.format("An exception occurred while indexing '%s'", movie);
            LOG.error(errorMessage);
            throw new MovieServiceException(errorMessage, e);
        }
    }

    @Override
    public Movie searchMovies(String title) {
        try {
            SearchResponse<Movie> searchResponse = client.search((s) ->
                s.index(moviesIndex)
                    .query(q -> q.match(m ->
                        m.field("title")
                         .query(title)
                    )), Movie.class
            );
            LOG.info("Searching for '{}' took {} and found {}", title, searchResponse.took(), searchResponse.hits().total().value());

            Iterator<Hit<Movie>> hits = searchResponse.hits().hits().iterator();
            if (hits.hasNext()) {
                return hits.next().source();
            }
            return null;

        } catch (Exception e) {
            String errorMessage = String.format("An exception occurred while searching for title '%s'", title);
            LOG.error(errorMessage);
            throw new MovieServiceException(errorMessage, e);
        }
    }
}
