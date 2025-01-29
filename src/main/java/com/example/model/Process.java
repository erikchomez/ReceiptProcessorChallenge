package com.example.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Process {
    private final String id;

    public Process(String id) {
        this.id = id;
    }
}
