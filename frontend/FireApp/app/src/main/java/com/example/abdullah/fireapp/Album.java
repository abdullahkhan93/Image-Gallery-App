package com.example.abdullah.fireapp;

import java.util.List;

/**
 * Created by jharjuma on 12/9/17.
 */

public class Album {

    private LocalGroup local_group;
    private List<LocalImage> local_images;

    public Album(LocalGroup local_group, List<LocalImage> local_images) {
        this.local_group = local_group;
        this.local_images = local_images;
    }

    public LocalGroup getLocalGroup() {
        return this.local_group;
    }

    public List<LocalImage> getLocalImages() {
        return this.local_images;
    }
}