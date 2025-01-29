package com.example.validation;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ValidationResponse {
    private boolean isValid;
    private List<String> errors;

    public void addErrors(List<String> errors) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.addAll(errors);
    }

    public void addErrors(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        errors.add(error);
    }
}
