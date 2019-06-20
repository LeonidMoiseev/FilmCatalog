package com.themoviedb.filmcatalog;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.themoviedb.filmcatalog.adapter.FilmsAdapter;
import com.themoviedb.filmcatalog.api.Client;
import com.themoviedb.filmcatalog.api.Service;
import com.themoviedb.filmcatalog.model.Film;
import com.themoviedb.filmcatalog.model.FilmsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FilmsAdapter filmsAdapter;
    private EditText etSearch;
    //public static final String LOG_TAG = FilmsAdapter.class.getName();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        swipeRefreshLayout = findViewById(R.id.main_content);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initView();
            }
        });

        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
                filmsAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        List<Film> filmList = new ArrayList<>();
        filmsAdapter = new FilmsAdapter(this, filmList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(filmsAdapter);
        filmsAdapter.notifyDataSetChanged();

        loadJSON();
    }

    private void loadJSON() {

        try {
            Service apiService = Client.getClient().create(Service.class);
            Call<FilmsResponse> call = apiService
                    .getPopularMovies(BuildConfig.THE_MOVIE_DATABASE_API_TOKEN, "ru-RU", "en");
            call.enqueue(new Callback<FilmsResponse>() {
                @Override
                public void onResponse(@NonNull Call<FilmsResponse> call, @NonNull Response<FilmsResponse> response) {
                    assert response.body() != null;
                    List<Film> films = response.body().getResults();
                    recyclerView.setAdapter(new FilmsAdapter(getApplicationContext(), films));
                    recyclerView.smoothScrollToPosition(0);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                    recyclerView.setLayoutManager(layoutManager);
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FilmsResponse> call, @NonNull Throwable t) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
