package com.example.aries.service;

import com.example.aries.service.internal.AnalysisResult;

public interface AnalysisService {

    AnalysisResult analyze(String title, String content, String url);
}
