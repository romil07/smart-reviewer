package com.example.aries.service.internal;

import com.example.aries.common.Sentiment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    private String summary;
    private Sentiment sentiment;
}
