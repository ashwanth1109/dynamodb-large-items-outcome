package com.hackathon.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Name of the counter model (i.e. 'S' - Split, 'C' - Compress, 'B' - Bucket)
@AllArgsConstructor
public enum ItemTypeInitial {
    Split("S"),
    Compress("C"),
    Bucket("B");

    @Getter
    private final String initial;
}
