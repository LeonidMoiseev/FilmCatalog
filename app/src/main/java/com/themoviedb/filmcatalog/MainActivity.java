package com.themoviedb.filmcatalog;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.themoviedb.filmcatalog.adapter.FilmsAdapter;
import com.themoviedb.filmcatalog.api.Client;
import com.themoviedb.filmcatalog.api.Service;
import com.themoviedb.filmcatalog.model.Film;
import com.themoviedb.filmcatalog.model.FilmsResponse;

import java.lang.reflect.Field;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FilmsAdapter filmsAdapter;
    private EditText etSearch;
    private List<Film> films;
    private ProgressBar progressBar;
    private ProgressBar progressBarHorizontal;
    private String request;
    private TextView requestTV;
    private LinearLayout checkEmptyIssue;
    private LinearLayout error;
    private RelativeLayout parentLayout;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                progressBarHorizontal.setVisibility(ProgressBar.VISIBLE);
                loadJSON();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    request = etSearch.getText().toString().trim();
                    filmsAdapter.getFilter().filter(s.toString());
                    recyclerView.setAdapter(filmsAdapter);
                    if (filmsAdapter.getSizeList() == 0 && !request.isEmpty()) {
                        checkEmptyIssue.setVisibility(View.VISIBLE);
                        requestTV.setText(getString(R.string.empty_issue_1) + request + getString(R.string.empty_issue_2));
                    } else {
                        checkEmptyIssue.setVisibility(View.INVISIBLE);
                    }
                }catch (NullPointerException ex) {
                    Log.d("error", "changeOrientationError");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

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
                    films = response.body().getResults();

                    initRecyclerView(films);

                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FilmsResponse> call, @NonNull Throwable t) {
                    errorLoad();
                }
            });
        } catch (Exception e) {
            errorLoad();
        }
    }

    private void initView() {
        requestTV = findViewById(R.id.request);
        checkEmptyIssue = findViewById(R.id.empty_issue);
        error = findViewById(R.id.error);
        etSearch = findViewById(R.id.etSearch);
        parentLayout = findViewById(R.id.parentLayout);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        progressBarHorizontal = findViewById(R.id.progressBarHorizontal);
        progressBarHorizontal.setVisibility(View.INVISIBLE);

        swipeRefreshLayout = findViewById(R.id.main_content);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark);
        try {
            Field f = swipeRefreshLayout.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView img = (ImageView)f.get(swipeRefreshLayout);
            img.setAlpha(0.0f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void initRecyclerView(List<Film> film) {
        filmsAdapter = new FilmsAdapter(getApplicationContext(), film);
        filmsAdapter.notifyDataSetChanged();

        recyclerView.setAdapter(filmsAdapter);
        recyclerView.smoothScrollToPosition(0);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        checkEmptyIssue.setVisibility(View.INVISIBLE);
        progressBarHorizontal.setVisibility(ProgressBar.INVISIBLE);
    }

    private void errorLoad() {
        error.setVisibility(View.VISIBLE);
        Snackbar.make(parentLayout, getString(R.string.error_internet), Snackbar.LENGTH_LONG)
                .show();
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        progressBarHorizontal.setVisibility(ProgressBar.INVISIBLE);
    }
}
