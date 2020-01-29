package com.example.moviesearcher.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviesearcher.R;
import com.example.moviesearcher.model.data.Movie;
import com.example.moviesearcher.model.util.ConverterUtil;
import com.example.moviesearcher.model.util.JsonUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.ViewHolder>{

    private Activity activity;
    private final List<Movie> movieList;

    public MovieGridAdapter(Activity activity, List<Movie> movieList) {
        this.activity = activity;
        this.movieList = movieList;
    }

    void updateMovieList(List<Movie> movieList){
        this.movieList.clear();
        this.movieList.addAll(movieList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.titleView.setText(movieList.get(position).getTitle());
        holder.releaseDateView.setText(movieList.get(position).getReleaseDate());
        holder.IMDbScoreView.setText(movieList.get(position).getScore());

        holder.posterView.setImageBitmap(ConverterUtil.HttpPathToBitmap(JsonUtil.getInstance().getPosterImageUrl(movieList.get(position).getPosterImageUrl())));

        StringBuilder genreString = null;
        for (String genre : movieList.get(position).getGenres()){
            if (genreString != null){
                genreString.append(", ").append(genre);
            } else {
                genreString = genre == null ? null : new StringBuilder(genre);
            }
        }
        holder.genresView.setText(genreString == null ? null : genreString.toString());
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        private ImageView posterView;
        private TextView titleView;
        private TextView releaseDateView;
        private TextView IMDbScoreView;
        private TextView genresView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            posterView = itemView.findViewById(R.id.movie_poster_view);
            titleView = itemView.findViewById(R.id.movie_title_view);
            releaseDateView = itemView.findViewById(R.id.movie_release_date_view);
            IMDbScoreView = itemView.findViewById(R.id.movie_score_view);
            genresView = itemView.findViewById(R.id.movie_genre_view);

            this.itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                int movieId = movieList.get(position).getId();
                NavDirections action = MoviesGridFragmentDirections.actionMovieDetails(movieId);
                Navigation.findNavController(itemView).navigate(action);
            });
        }
    }
}
