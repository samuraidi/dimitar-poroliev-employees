package com.example.employee_analyzer.model.dto;

public class PairWorkSummary {
    private int empId1;
    private int empId2;
    private int projectId;
    private long daysWorked;

    public PairWorkSummary(int empId1, int empId2, int projectId, long daysWorked) {
        this.empId1 = empId1;
        this.empId2 = empId2;
        this.projectId = projectId;
        this.daysWorked = daysWorked;
    }

    public int getEmpId1() {
        return empId1;
    }

    public int getEmpId2() {
        return empId2;
    }

    public int getProjectId() {
        return projectId;
    }

    public long getDaysWorked() {
        return daysWorked;
    }
}
