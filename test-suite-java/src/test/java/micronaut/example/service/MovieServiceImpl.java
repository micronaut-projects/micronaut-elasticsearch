package micronaut.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import micronaut.example.controller.SearchMovieResponse;
import micronaut.example.exception.MovieServiceException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MovieServiceImpl implements MovieService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieServiceImpl.class);

    @Value("${elasticsearch.indexes.movies}")
    String moviesIndex;

    private final RestHighLevelClient client;
    private final MovieMapper movieMapper;
    private final ObjectMapper objectMapper;

    public MovieServiceImpl(RestHighLevelClient client,
                            MovieMapper movieMapper,
                            ObjectMapper objectMapper) {
        this.client = client;
        this.movieMapper = movieMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public String saveMovie(Movie movie) {
        try {
            String movieAsJsonString = objectMapper.writeValueAsString(movie);

            IndexRequest indexRequest = new IndexRequest(moviesIndex).source(movieAsJsonString, XContentType.JSON);

            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            String id = indexResponse.getId();

            LOG.info("Document for '{}' {} successfully in ES. The id is: {}", movie, indexResponse.getResult(), id);
            return id;
        } catch (Exception e) {
            String errorMessage = String.format("An exception occurred while indexing '%s'", movie);
            LOG.error(errorMessage);
            throw new MovieServiceException(errorMessage, e);
        }
    }

    @Override
    public SearchMovieResponse searchMovies(String title) {
        try {
            SearchRequest searchRequest = new SearchRequest(moviesIndex);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.termQuery("title", title));
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            LOG.info("Searching for '{}' took {} and found {}", title, searchResponse.getTook(), searchResponse.getHits().getTotalHits());

            return movieMapper.toSearchMovieDto(searchResponse.getHits(), searchResponse.getTook());

        } catch (Exception e) {
            String errorMessage = String.format("An exception occurred while searching for title '%s'", title);
            LOG.error(errorMessage);
            throw new MovieServiceException(errorMessage, e);
        }
    }
}
