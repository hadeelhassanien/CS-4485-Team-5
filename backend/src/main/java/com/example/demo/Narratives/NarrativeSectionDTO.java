package com.example.demo.Narratives;

import java.util.List;

public class NarrativeSectionDTO {
    private String title;
    private List<String> items;

    public NarrativeSectionDTO() {}

    public NarrativeSectionDTO(String title, List<String> items) {
        this.title = title;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getItems() {
        return items;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }
}