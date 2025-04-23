package com.example.employee_analyzer.controller;

import com.example.employee_analyzer.model.dto.ResponseDto;
import com.example.employee_analyzer.service.EmployeePairService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UploadController {

    private final EmployeePairService employeePairService;

    public UploadController(EmployeePairService employeePairService) {
        this.employeePairService = employeePairService;
    }

    /**
     * Accepts a CSV file, delegates parsing and processing to the service layer,
     * and returns the longest-working employee pair along with project breakdowns.
     *
     * @param file CSV file uploaded as multipart/form-data
     * @return ResponseDto with longest pair and project-level summaries
     */
    @PostMapping("/upload")
    public ResponseEntity<ResponseDto> uploadFile(@RequestParam("file")MultipartFile file) {
        ResponseDto response = employeePairService.processFile(file);
        return ResponseEntity.ok(response);
    }
}
