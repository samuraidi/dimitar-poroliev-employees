package com.example.employee_analyzer.service;

import com.example.employee_analyzer.model.dto.LongestPairDto;
import com.example.employee_analyzer.model.dto.PairWorkSummary;
import com.example.employee_analyzer.model.dto.ResponseDto;
import com.example.employee_analyzer.model.entity.AssignmentEntity;
import com.example.employee_analyzer.model.entity.EmployeeEntity;
import com.example.employee_analyzer.model.entity.ProjectEntity;
import com.example.employee_analyzer.repository.AssignmentRepository;
import com.example.employee_analyzer.repository.EmployeeRepository;
import com.example.employee_analyzer.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeePairService {

    private final EmployeeRepository employeeRepo;
    private final ProjectRepository projectRepo;
    private final AssignmentRepository assignmentRepo;

    public EmployeePairService(EmployeeRepository employeeRepo,
                               ProjectRepository projectRepo,
                               AssignmentRepository assignmentRepo) {
        this.employeeRepo = employeeRepo;
        this.projectRepo = projectRepo;
        this.assignmentRepo = assignmentRepo;
    }

    public ResponseDto processFile(MultipartFile file) {
        Map<Integer, List<AssignmentEntity>> projectAssignments = new HashMap<>();
        List<PairWorkSummary> summaryList = new ArrayList<>();
        Map<String, Long> totalDaysTogether = new HashMap<>();

        // Optional in-memory cache to reduce DB hits
        Map<Integer, EmployeeEntity> employeeCache = new HashMap<>();
        Map<Integer, ProjectEntity> projectCache = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",\\s*");
                if (parts.length < 4) continue;

                int empId = Integer.parseInt(parts[0]);
                int projectId = Integer.parseInt(parts[1]);
                LocalDate from = parseDate(parts[2]);
                LocalDate to = parseDate(parts[3]);

                if (from == null || to == null || from.isAfter(to)) continue;

                // Fetch or save Employee
                EmployeeEntity employee = employeeCache.computeIfAbsent(empId, id ->
                        employeeRepo.findById(id)
                                .orElseGet(() -> employeeRepo.save(new EmployeeEntity(id, "Employee #" + id)))
                );

                // Fetch or save Project
                ProjectEntity project = projectCache.computeIfAbsent(projectId, id ->
                        projectRepo.findById(id)
                                .orElseGet(() -> projectRepo.save(new ProjectEntity(id, "Project #" + id)))
                );

                // Persist assignment
                AssignmentEntity assignment = new AssignmentEntity(employee, project, from, to);
                assignmentRepo.save(assignment);

                projectAssignments
                        .computeIfAbsent(projectId, k -> new ArrayList<>())
                        .add(assignment);
            }

            // Calculate overlaps
            for (List<AssignmentEntity> assignments : projectAssignments.values()) {
                for (int i = 0; i < assignments.size(); i++) {
                    for (int j = i + 1; j < assignments.size(); j++) {
                        AssignmentEntity a1 = assignments.get(i);
                        AssignmentEntity a2 = assignments.get(j);

                        LocalDate overlapStart = a1.getDateFrom().isAfter(a2.getDateFrom()) ? a1.getDateFrom() : a2.getDateFrom();
                        LocalDate overlapEnd = a1.getDateTo().isBefore(a2.getDateTo()) ? a1.getDateTo() : a2.getDateTo();

                        if (!overlapStart.isAfter(overlapEnd)) {
                            long days = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;

                            int id1 = a1.getEmployee().getId();
                            int id2 = a2.getEmployee().getId();
                            int projId = a1.getProject().getId();

                            String key = id1 < id2 ? id1 + "-" + id2 : id2 + "-" + id1;

                            summaryList.add(new PairWorkSummary(id1, id2, projId, days));
                            totalDaysTogether.put(key, totalDaysTogether.getOrDefault(key, 0L) + days);
                        }
                    }
                }
            }

            // Determine longest pair
            String maxPairKey = totalDaysTogether.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("0-0");

            long maxDays = totalDaysTogether.getOrDefault(maxPairKey, 0L);
            String[] empIds = maxPairKey.split("-");

            LongestPairDto longest = new LongestPairDto(
                    Integer.parseInt(empIds[0]),
                    Integer.parseInt(empIds[1]),
                    maxDays
            );

            return new ResponseDto(longest, summaryList);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process CSV file", e);
        }
    }


    private Optional<AssignmentEntity> parseAssignmentLine(String line) {
        try {
            String[] parts = line.split(",\\s*");
            if (parts.length < 4) return Optional.empty();

            int empId = Integer.parseInt(parts[0]);
            int projectId = Integer.parseInt(parts[1]);
            LocalDate from = parseDate(parts[2]);
            LocalDate to = parseDate(parts[3]);

            if (from != null && to != null && !from.isAfter(to)) {
                EmployeeEntity emp = new EmployeeEntity(empId, "Employee #" + empId);
                ProjectEntity proj = new ProjectEntity(projectId, "Project #" + projectId);
                return Optional.of(new AssignmentEntity(emp, proj, from, to));
            }
        } catch (Exception ignored) {}

        return Optional.empty();
    }

    private void calculatePairOverlaps(Map<Integer, List<AssignmentEntity>> assignmentsByProject,
                                       List<PairWorkSummary> summaryList,
                                       Map<String, Long> totalDaysTogether) {

        for (List<AssignmentEntity> assignments : assignmentsByProject.values()) {
            for (int i = 0; i < assignments.size(); i++) {
                for (int j = i + 1; j < assignments.size(); j++) {
                    AssignmentEntity a1 = assignments.get(i);
                    AssignmentEntity a2 = assignments.get(j);

                    LocalDate overlapStart = a1.getDateFrom().isAfter(a2.getDateFrom()) ? a1.getDateFrom() : a2.getDateFrom();
                    LocalDate overlapEnd = a1.getDateTo().isBefore(a2.getDateTo()) ? a1.getDateTo() : a2.getDateTo();

                    if (!overlapStart.isAfter(overlapEnd)) {
                        long days = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;

                        int id1 = a1.getEmployee().getId();
                        int id2 = a2.getEmployee().getId();
                        int projectId = a1.getProject().getId();

                        String key = id1 < id2 ? id1 + "-" + id2 : id2 + "-" + id1;

                        summaryList.add(new PairWorkSummary(id1, id2, projectId, days));
                        totalDaysTogether.put(key, totalDaysTogether.getOrDefault(key, 0L) + days);
                    }
                }
            }
        }
    }

    private LocalDate parseDate(String input) {
        if (input == null || input.equalsIgnoreCase("null")) return LocalDate.now();

        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
        );

        for (DateTimeFormatter fmt : formats) {
            try {
                return LocalDate.parse(input, fmt);
            } catch (Exception ignored) {}
        }

        return null;
    }
}



