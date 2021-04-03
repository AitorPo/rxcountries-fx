package com.svalero.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Bloc {
    private String acronym;

    @Override
    public String toString() {
        return acronym;
    }
}
