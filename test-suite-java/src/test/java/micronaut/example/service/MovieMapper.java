package micronaut.example.service;

import micronaut.example.controller.CreateMovieRequest;
import micronaut.example.controller.SearchMovieResponse;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.search.SearchHits;

public interface MovieMapper {

    Movie toMovie(CreateMovieRequest createMovieRequest);

    SearchMovieResponse toSearchMovieDto(SearchHits searchHits, TimeValue took);
}
