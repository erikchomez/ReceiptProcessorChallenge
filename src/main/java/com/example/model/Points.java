package com.example.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Points {
    private final int points;

    public Points(int points) {
        this.points = points;
    }
}
