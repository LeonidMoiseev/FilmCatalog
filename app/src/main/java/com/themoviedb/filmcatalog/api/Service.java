package com.themoviedb.filmcatalog.api;

import com.themoviedb.filmcatalog.model.FilmsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Service {
    @GET("discover/movie")
    Call<FilmsResponse> getPopularMovies(@Query("api_key") String api_key,
                                         @Query("language") String language,
                                         @Query("with_original_language") String original_language);
}
