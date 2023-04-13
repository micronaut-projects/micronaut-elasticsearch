package micronaut.example.service;


public interface MovieService {

    String saveMovie(Movie movie);

    Movie searchMovies(String title);

}
