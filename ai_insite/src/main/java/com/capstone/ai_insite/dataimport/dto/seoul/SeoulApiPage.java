package com.capstone.ai_insite.dataimport.dto.seoul;

import java.util.List;

public record SeoulApiPage<T>(
    int totalCount,
    List<SeoulApiRow<T>> rows
) {
}
