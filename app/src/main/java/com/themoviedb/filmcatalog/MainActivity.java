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
import android.widget.Button;
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
    private ArrayList<? extends Film> films;
    private ProgressBar progressBar;
    private ProgressBar progressBarHorizontal;
    private String request;
    private TextView requestTV;
    private LinearLayout checkEmptyIssue;
    private RelativeLayout error;
    private RelativeLayout parentLayout;
    private Button refresh;
    private boolean firstLoad = false;
    private boolean errorLoad = false;

    private static final String STATE_LIST = "StateAdapterData";
    private static final String STATE_FIRST_LOAD = "StateFirstLoad";
    private static final String STATE_REQUEST = "StateRequest";
    private static final String STATE_ERROR_LOAD = "StateErrorLoad";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        if (savedInstanceState != null) {
            /*
             * errorLoad - принимает значение в зависимости от того, была ли ошибка загрузки данных
             * при первом открытии приложения. Если ошибка была и пользователь повернул экран,
             * на активити будет показано тоже самое, что и перед поворотом экрана.
             * */
            errorLoad = savedInstanceState.getBoolean(STATE_ERROR_LOAD);
            if (!errorLoad) {
                films = savedInstanceState.getParcelableArrayList(STATE_LIST);
                firstLoad = savedInstanceState.getBoolean(STATE_FIRST_LOAD);
                request = savedInstanceState.getString(STATE_REQUEST);
                initSearchEditText();
                initRecyclerView((List<Film>) films);
                initPullToRefresh();
            } else {
                buttonRefreshErrorLoad();
                initSearchEditText();
            }
        } else {
            loadJSON();
            initSearchEditText();
        }
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
                    films = (ArrayList<? extends Film>) response.body().getResults();

                    initRecyclerView((List<Film>) films);
                    /*
                     * Если список успешно загрузился в первый раз, инициализируется pull-to-refresh
                     * */
                    if (!firstLoad) {
                        initPullToRefresh();
                        firstLoad = true;
                        errorLoad = false;
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

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void initSearchEditText() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    request = etSearch.getText().toString().trim();
                    filmsAdapter.getFilter().filter(s.toString());
                    recyclerView.setAdapter(filmsAdapter);
                } catch (NullPointerException ex) {
                    Log.d("error", "changeOrientationError");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if (getCurrentFocus() == etSearch) {
                    try {
                        if (filmsAdapter.getSizeList() == 0 && !request.isEmpty()) {

                            showErrorEmptyIssue();

                        } else {
                            checkEmptyIssue.setVisibility(View.INVISIBLE);
                        }
                    } catch (NullPointerException ex) {
                        Log.d("error", "changeOrientationError");
                    }
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showErrorEmptyIssue() {
        checkEmptyIssue.setVisibility(View.VISIBLE);
        requestTV.setText(getString(R.string.empty_issue_1) + request
                + getString(R.string.empty_issue_2));
    }

    private void initView() {
        requestTV = findViewById(R.id.request);
        checkEmptyIssue = findViewById(R.id.empty_issue);
        error = findViewById(R.id.error);
        etSearch = findViewById(R.id.etSearch);
        parentLayout = findViewById(R.id.parentLayout);
        refresh = findViewById(R.id.refresh);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        progressBarHorizontal = findViewById(R.id.progressBarHorizontal);
        progressBarHorizontal.setVisibility(View.INVISIBLE);

        swipeRefreshLayout = findViewById(R.id.main_content);
        /*
         * Удаляется Progress Spinner из функциональности SwipeRefreshLayout
         * */
        try {
            Field f = swipeRefreshLayout.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView img = (ImageView) f.get(swipeRefreshLayout);
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
        error.setVisibility(View.INVISIBLE);
    }

    private void initPullToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                progressBarHorizontal.setVisibility(ProgressBar.VISIBLE);
                loadJSON();
            }
        });
    }

    private void errorLoad() {
        /*
         * Если при первой загрузки контента возникает ошибка,
         * на экране показывается сообщение об ошибке и кнопка с возможностью повторить запрос
         * */
        if (!firstLoad) {
            errorLoad = true;
            buttonRefreshErrorLoad();
        }
        Snackbar.make(parentLayout, getString(R.string.error_internet), Snackbar.LENGTH_LONG).show();
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        progressBarHorizontal.setVisibility(ProgressBar.INVISIBLE);
    }

    private void buttonRefreshErrorLoad() {
        error.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                error.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                loadJSON();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_LIST, films);
        outState.putBoolean(STATE_FIRST_LOAD, firstLoad);
        outState.putString(STATE_REQUEST, request);
        outState.putBoolean(STATE_ERROR_LOAD, errorLoad);
        super.onSaveInstanceState(outState);
    }
}