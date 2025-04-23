package com.example.employee_analyzer.model.dto;

public class LongestPairDto {
    private int empId1;
    private int empId2;
    private long totalDays;

    public LongestPairDto(int empId1, int empId2, long totalDays) {
        this.empId1 = empId1;
        this.empId2 = empId2;
        this.totalDays = totalDays;
    }

    public int getEmpId1() {
        return empId1;
    }

    public int getEmpId2() {
        return empId2;
    }

    public long getTotalDays() {
        return totalDays;
    }
}
