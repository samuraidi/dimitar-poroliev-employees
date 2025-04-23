package com.example.employee_analyzer.model.dto;

import java.util.List;

public class ResponseDto {
    private LongestPairDto longestPair;
    private List<PairWorkSummary> details;

    public ResponseDto(LongestPairDto longestPair, List<PairWorkSummary> details) {
        this.longestPair = longestPair;
        this.details = details;
    }

    public LongestPairDto getLongestPair() {
        return longestPair;
    }

    public List<PairWorkSummary> getDetails(){
        return details;
    }
}
