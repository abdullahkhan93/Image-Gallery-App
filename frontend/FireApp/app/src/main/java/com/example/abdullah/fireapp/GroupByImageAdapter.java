package com.example.abdullah.fireapp;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jharjuma on 12/10/17.
 */

class GroupByImageAdapter extends RecyclerView.Adapter<GroupByImageAdapter.ViewHolder>  {

    private List<LocalImage> image_list;
    private Context context;
    private ImageGridActivity.GroupBySelection group_by_selection;

    public GroupByImageAdapter(Context context, List<LocalImage> image_list, ImageGridActivity.GroupBySelection group_by_selection)  {
        this.image_list = image_list;
        this.context = context;
        this.group_by_selection = group_by_selection;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        RecyclerView recyclerView;
        public ViewHolder(View view) {
            super(view);
            title = (TextView)view.findViewById(R.id.album_group_name);
            recyclerView = (RecyclerView) view.findViewById(R.id.image_gallery_view);
        }
    }

    @Override
    public GroupByImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_layout, parent, false);
        return new GroupByImageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupByImageAdapter.ViewHolder holder, int position) {

        if(group_by_selection.getSelected() == ImageGridActivity.GroupBySelection.CATEGORY) {

            if(position == 0) {
                holder.title.setText("People");
            }
            else {
                holder.title.setText("No people");
            }


        }
        else if(group_by_selection.getSelected() == ImageGridActivity.GroupBySelection.AUTHOR) {
            holder.title.setText(getAuthors().get(position));
        }

        holder.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context,3);
        holder.recyclerView.setLayoutManager(layoutManager);

        ArrayList<LocalImage> group_by_list = new ArrayList<LocalImage>();

        if(group_by_selection.getSelected() == ImageGridActivity.GroupBySelection.CATEGORY) {

        }
        else if(group_by_selection.getSelected() == ImageGridActivity.GroupBySelection.AUTHOR) {
            List<String> authors = this.getAuthors();
            for(LocalImage local_image : this.image_list) {
                if(local_image.getAuthor().equals(authors.get(position))) {
                    group_by_list.add(local_image);
                }
            }
        }

        GridImageAdapter adapter = new GridImageAdapter(context, group_by_list);
        holder.recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private List<String> getAuthors() {
        HashSet<String> authors = new HashSet<String>();
        for(LocalImage local_image : this.image_list) {
            authors.add(local_image.getAuthor());
        }
        ArrayList<String> authors_list = new ArrayList<String>();
        authors_list.addAll(authors);
        Collections.sort(authors_list);
        return authors_list;
    }

    @Override
    public int getItemCount() {
        if(group_by_selection.getSelected() == ImageGridActivity.GroupBySelection.CATEGORY) {
            return 2;
        }
        else if(group_by_selection.getSelected() == ImageGridActivity.GroupBySelection.AUTHOR) {

            return this.getAuthors().size();
        }
        return 0;
    }
}

