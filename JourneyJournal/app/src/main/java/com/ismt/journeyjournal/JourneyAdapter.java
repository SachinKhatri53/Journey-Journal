package com.ismt.journeyjournal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.ismt.journeyjournal.model.JourneyModel;

public class JourneyAdapter extends FirebaseRecyclerAdapter<JourneyModel, JourneyAdapter.JourneyViewHolder> {

    public JourneyAdapter(@NonNull FirebaseRecyclerOptions<JourneyModel> options) {
        super(options);

    }

    @Override
    protected void onBindViewHolder(@NonNull JourneyViewHolder holder, int position, @NonNull JourneyModel model) {
        holder.title.setText(model.getTitle().substring(0, 1).toUpperCase() + model.getTitle().substring(1));
        holder.date.setText(model.getDate());
        Glide.with(holder.picture.getContext()).load(model.getImage()).into(holder.picture);
        holder.location.setText(model.getLocation());

        holder.layoutJourney.setOnClickListener(view -> {

            view.getContext().startActivity(new Intent(view.getContext(), JourneyDetailActivity.class)
            .putExtra("title", model.getTitle().substring(0, 1).toUpperCase() + model.getTitle().substring(1))
            .putExtra("description", model.getDescription())
            .putExtra("image", model.getImage())
            .putExtra("date", model.getDate())
            .putExtra("location", model.getLocation())
                    .putExtra("findLatitude", model.getFindLatitude())
                    .putExtra("findLongitude", model.getFindLongitude())
            .putExtra("position", getRef(position).getKey())

            );
        });

    }

    @NonNull
    @Override
    public JourneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_page_layout,parent,false);
        return new JourneyViewHolder(view);
    }

    public class JourneyViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView picture;
        AppCompatTextView title, date, location;
        LinearLayoutCompat layoutJourney;

        public JourneyViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            layoutJourney = itemView.findViewById(R.id.layoutJourney);
            location = itemView.findViewById(R.id.location);
        }

    }
}
