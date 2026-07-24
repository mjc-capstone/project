package com.capstone.ai_insite.dataimport.dto.seoul;

public record SeoulApiRow<T>(
    T value,
    String sourceJson
) {
}
