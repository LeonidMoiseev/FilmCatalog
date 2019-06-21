package com.themoviedb.filmcatalog.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FilmsResponse {
    @SerializedName("results")
    private List<Film> results;

    public List<Film> getResults() {
        return results;
    }

    public void setResults(List<Film> results) {
        this.results = results;
    }
}
