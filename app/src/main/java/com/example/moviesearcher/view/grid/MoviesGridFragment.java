package com.example.moviesearcher.view.grid;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.moviesearcher.R;
import com.example.moviesearcher.viewmodel.MovieGridViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoviesGridFragment extends Fragment {

    private MovieGridViewModel viewModel;
    private MovieGridAdapter gridAdapter = new MovieGridAdapter();

    @BindView(R.id.movie_grid_view) RecyclerView recyclerView;
    @BindView(R.id.loading_error_text_view) TextView errorTextView;
    @BindView(R.id.progress_bar_loading_movie_list) ProgressBar progressBar;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.grid_movies_fragment) LinearLayout fragmentLayout;

    private boolean isDown = true;

    private StaggeredGridLayoutManager layoutManager;
    private Parcelable state;

    public MoviesGridFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies_grid, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        viewModel = ViewModelProviders.of(this).get(MovieGridViewModel.class);
        viewModel.initFetch(getActivity(), getArguments());

        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(gridAdapter);
        if (state != null){

            layoutManager.onRestoreInstanceState(state);
        }



        observeViewModel();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && isDown) {
                    isDown = false;
                    viewModel.fetch();
                    isDown = true;
                }
            }
        });

        refreshLayout.setOnRefreshListener(() -> {
            viewModel.refresh();
            refreshLayout.setRefreshing(false);
        });
    }

    private void observeViewModel(){
        viewModel.getMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null){
                gridAdapter.updateMovieList(movies);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getMovieLoadError().observe(getViewLifecycleOwner(), isError -> {
            if (isError != null){
                errorTextView.setVisibility(isError ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null){
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading){
                    errorTextView.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        state = layoutManager.onSaveInstanceState();
    }
}