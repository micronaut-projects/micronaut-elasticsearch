package micronaut.example.controller;

import io.micronaut.core.annotation.Introspected;

import java.util.ArrayList;
import java.util.List;

@Introspected
public class SearchMovieResponse {

    private List<Hit> hits = new ArrayList<>();
    private String took;

    public SearchMovieResponse() {
    }

    public List<Hit> getHits() {
        return hits;
    }

    public void setHits(List<Hit> hits) {
        this.hits = hits;
    }

    public String getTook() {
        return took;
    }

    public void setTook(String took) {
        this.took = took;
    }

    @Introspected
    public static class Hit {
        private String index;
        private String id;
        private Float score;
        private String source;

        public Hit(String index, String id, Float score, String source) {
            this.index = index;
            this.id = id;
            this.score = score;
            this.source = source;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

}
