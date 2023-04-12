package micronaut.example.service;

import micronaut.example.controller.SearchMovieResponse;

public interface MovieService {

    String saveMovie(Movie movie);

    SearchMovieResponse searchMovies(String title);

}
