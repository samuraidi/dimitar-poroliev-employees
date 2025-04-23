package com.example.employee_analyzer.service;

import com.example.employee_analyzer.model.entity.AssignmentEntity;
import com.example.employee_analyzer.model.entity.EmployeeEntity;
import com.example.employee_analyzer.model.entity.ProjectEntity;
import com.example.employee_analyzer.repository.AssignmentRepository;
import com.example.employee_analyzer.repository.EmployeeRepository;
import com.example.employee_analyzer.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EmployeePairServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @InjectMocks
    private EmployeePairService service;

    private MockMultipartFile validFile;
    private MockMultipartFile invalidFile;

    @BeforeEach
    void setup(){
        String validCsv = "143, 12, 2013-11-01, 2014-01-05\n218,12,2013-12-01,2014-01-05";
        validFile = new MockMultipartFile("file", "valid.csv", "text/csv", validCsv.getBytes(StandardCharsets.UTF_8));

        String invalidCsv = "bad,data,line\ninvalid,line";
        invalidFile = new MockMultipartFile("file", "invalid.csv", "text/csv", invalidCsv.getBytes(StandardCharsets.UTF_8));

    }
    @Test
    void testProcessValidFile_shouldSucced(){
        assertDoesNotThrow(() -> service.processFile(validFile));

        verify(employeeRepository, atLeastOnce()).save(any(EmployeeEntity.class));
        verify(projectRepository, atLeastOnce()).save(any(ProjectEntity.class));
        verify(assignmentRepository, atLeastOnce()).save(any(AssignmentEntity.class));
    }

    void testProcessInvalidFile_shouldNotThrowExceptionButSkipInvalidLines(){
        assertDoesNotThrow(() -> service.processFile(invalidFile));

        verify(employeeRepository, never()).save(any());
        verify(projectRepository, never()).save(any());
        verify(assignmentRepository, never()).save(any());
    }

    void testProcessEmptyFile_shouldNotCrash() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        assertDoesNotThrow(() -> service.processFile(empty));
    }

}
