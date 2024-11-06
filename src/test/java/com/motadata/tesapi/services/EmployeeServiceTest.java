package com.motadata.tesapi.services;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.motadata.tesapi.entities.Employee;
import com.motadata.tesapi.repos.EmployeeRepo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(WireMockExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepo employeeRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setupWireMockServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("Keyur Chotaliya");
        employee.setEmail("john.doe@example.com");
        employee.setDepartment("Engineering");
        employee.setSalary(100000.0);

        MockitoAnnotations.openMocks(this);
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/employees/1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody("{ \"id\": 1, \"name\": \"Keyur Chotaliya\", \"email\": \"john.doe@example.com\" }")
                        .withHeader("Content-Type", "application/json")));
    }

    @Test
    @DisplayName("Get employee by ID - Success")
    void testGetEmployeeById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee fetchedEmployee = employeeService.getEmployee(1L);

        assertNotNull(fetchedEmployee);
        assertEquals("Keyur Chotaliya", fetchedEmployee.getName());
        assertEquals(1L, fetchedEmployee.getId());
        verify(employeeRepository, times(1)).findById(1L);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/api/employees/1")));
    }

    @Test
    @DisplayName("Get employee by ID - Employee Not Found")
    void testGetEmployeeById_EmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Employee fetchedEmployee = employeeService.getEmployee(1L);

        assertNull(fetchedEmployee);
        verify(employeeRepository, times(1)).findById(1L);
        verify(kafkaTemplate, never()).send(anyString(), anyString());

        WireMock.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo("/api/employees/1")));
    }

    @Test
    @DisplayName("Get employee by ID - External Service Failure (HTTP 500)")
    void testGetEmployeeById_ExternalServiceFailure() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/employees/1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("{ \"error\": \"Internal Server Error\" }")
                        .withHeader("Content-Type", "application/json")));

        Employee fetchedEmployee = employeeService.getEmployee(1L);

        assertNull(fetchedEmployee);

        verify(employeeRepository, times(1)).findById(1L);
        verify(kafkaTemplate, never()).send(anyString(), anyString());

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/api/employees/1")));
    }

    @Test
    @DisplayName("Get employee by ID - Repository Failure (Exception)")
    void testGetEmployeeById_RepositoryFailure() {
        when(employeeRepository.findById(1L)).thenThrow(new RuntimeException("Database connection error"));

        Exception exception = assertThrows(RuntimeException.class, () -> employeeService.getEmployee(1L));

        assertEquals("Database connection error", exception.getMessage());

        verify(employeeRepository, times(1)).findById(1L);
        verify(kafkaTemplate, never()).send(anyString(), anyString());

        WireMock.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo("/api/employees/1")));
    }

    @Test
    @DisplayName("create test")
    void testSaveEmployee_create() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.saveEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals("Keyur Chotaliya", savedEmployee.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }

    @Test
    @DisplayName("delete test")
    void testDeleteEmployee() {
        doNothing().when(employeeRepository).deleteById(1L);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).deleteById(1L);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }

    @Test
    @DisplayName("get all test")
    void testGetAllEmployees() {
        List<Employee> employeeList = Arrays.asList(employee);
        when(employeeRepository.findAll()).thenReturn(employeeList);

        List<Employee> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("Keyur Chotaliya", employees.get(0).getName());
        verify(employeeRepository, times(1)).findAll();
    }

    @AfterAll
    static void tearDownWireMockServer() {
        wireMockServer.stop();
    }
}
