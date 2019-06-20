package com.themoviedb.filmcatalog.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.themoviedb.filmcatalog.R;
import com.themoviedb.filmcatalog.model.Film;

import java.util.ArrayList;
import java.util.List;

public class FilmsAdapter extends RecyclerView.Adapter<FilmsAdapter.MyViewHolder> implements Filterable {

    private Context mContext;
    private List<Film> mListFilms;
    private List<Film> mOriginalValues;
    private List<Film> filteredArrList;


    public FilmsAdapter(Context mContext, List<Film> mListFilms) {
        this.mContext = mContext;
        this.mListFilms = mListFilms;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.film_card, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilmsAdapter.MyViewHolder viewHolder, int i) {
        viewHolder.titleFilm.setText(mListFilms.get(i).getTitle());
        viewHolder.descriptionFilm.setText(mListFilms.get(i).getOverview());
        viewHolder.releaseDateFilm.setText(mListFilms.get(i).getReleaseDate());

        Glide.with(mContext)
                .load(mListFilms.get(i).getPosterPath())
                .into(viewHolder.imageFilm);
    }

    @Override
    public int getItemCount() {
        return mListFilms.size();
    }

    public int getSizeList() {
        return filteredArrList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titleFilm;
        TextView descriptionFilm;
        ImageView imageFilm;
        TextView releaseDateFilm;

        MyViewHolder(View view) {
            super(view);
            titleFilm = view.findViewById(R.id.film_title);
            descriptionFilm = view.findViewById(R.id.film_description);
            imageFilm = view.findViewById(R.id.film_image);
            releaseDateFilm = view.findViewById(R.id.film_release_date);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Film click = mListFilms.get(position);
                    Snackbar.make(v, click.getTitle(), Snackbar.LENGTH_LONG)
                            .show();
                }
            });
        }
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {
                mListFilms = (List<Film>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                filteredArrList = new ArrayList<>();


                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<>();
                    mOriginalValues = mListFilms;
                }

                if (constraint == null || constraint.length() == 0) {

                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String data = mOriginalValues.get(i).getTitle();
                        if (data.toLowerCase().contains(constraint.toString())) {
                            filteredArrList.add(mOriginalValues.get(i));
                        }
                    }

                    results.count = filteredArrList.size();
                    results.values = filteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}
