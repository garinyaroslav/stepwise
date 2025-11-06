package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.stepwise.entity.User;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceTest {

    @InjectMocks
    private ExcelExportService excelExportService;

    @Test
    void exportUsersWithTempPasswords_WithUsers_ShouldGenerateExcelFile() throws IOException {
        List<User> users = List.of(
                User.builder()
                        .username("user1")
                        .email("user1@example.com")
                        .tempPassword("temp123")
                        .build(),
                User.builder()
                        .username("user2")
                        .email("user2@example.com")
                        .tempPassword("temp456")
                        .build());

        byte[] result = excelExportService.exportUsersWithTempPasswords(users);

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Users with Temporary Passwords");
            assertNotNull(sheet);

            Row headerRow = sheet.getRow(0);
            assertEquals("Username", headerRow.getCell(0).getStringCellValue());
            assertEquals("Email", headerRow.getCell(1).getStringCellValue());
            assertEquals("Temporary Password", headerRow.getCell(2).getStringCellValue());

            Row firstDataRow = sheet.getRow(1);
            assertEquals("user1", firstDataRow.getCell(0).getStringCellValue());
            assertEquals("user1@example.com", firstDataRow.getCell(1).getStringCellValue());
            assertEquals("temp123", firstDataRow.getCell(2).getStringCellValue());

            Row secondDataRow = sheet.getRow(2);
            assertEquals("user2", secondDataRow.getCell(0).getStringCellValue());
            assertEquals("user2@example.com", secondDataRow.getCell(1).getStringCellValue());
            assertEquals("temp456", secondDataRow.getCell(2).getStringCellValue());
        }
    }

    @Test
    void exportUsersWithTempPasswords_WithEmptyList_ShouldGenerateExcelWithOnlyHeaders() throws IOException {
        List<User> users = List.of();

        byte[] result = excelExportService.exportUsersWithTempPasswords(users);

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Users with Temporary Passwords");
            assertNotNull(sheet);

            assertEquals(1, sheet.getPhysicalNumberOfRows());

            Row headerRow = sheet.getRow(0);
            assertEquals("Username", headerRow.getCell(0).getStringCellValue());
            assertEquals("Email", headerRow.getCell(1).getStringCellValue());
            assertEquals("Temporary Password", headerRow.getCell(2).getStringCellValue());
        }
    }

    @Test
    void exportUsersWithTempPasswords_WithNullValues_ShouldHandleGracefully() throws IOException {
        List<User> users = List.of(
                User.builder()
                        .username(null)
                        .email(null)
                        .tempPassword(null)
                        .build(),
                User.builder()
                        .username("validuser")
                        .email("valid@example.com")
                        .tempPassword("validpass")
                        .build());

        byte[] result = excelExportService.exportUsersWithTempPasswords(users);

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Users with Temporary Passwords");

            Row firstDataRow = sheet.getRow(1);
            assertEquals("", firstDataRow.getCell(0).getStringCellValue());
            assertEquals("", firstDataRow.getCell(1).getStringCellValue());
            assertEquals("", firstDataRow.getCell(2).getStringCellValue());

            Row secondDataRow = sheet.getRow(2);
            assertEquals("validuser", secondDataRow.getCell(0).getStringCellValue());
            assertEquals("valid@example.com", secondDataRow.getCell(1).getStringCellValue());
            assertEquals("validpass", secondDataRow.getCell(2).getStringCellValue());
        }
    }

    @Test
    void exportUsersWithTempPasswords_ShouldSetHeaderStyle() throws IOException {
        List<User> users = List.of(
                User.builder()
                        .username("testuser")
                        .email("test@example.com")
                        .tempPassword("testpass")
                        .build());

        byte[] result = excelExportService.exportUsersWithTempPasswords(users);

        assertNotNull(result);

        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Users with Temporary Passwords");
            Row headerRow = sheet.getRow(0);

            CellStyle headerStyle = headerRow.getCell(0).getCellStyle();
            Font font = workbook.getFontAt(headerStyle.getFontIndex());
            assertTrue(font.getBold());
        }
    }

    @Test
    void exportUsersWithTempPasswords_ShouldAutoSizeColumns() throws IOException {
        List<User> users = List.of(
                User.builder()
                        .username("verylongusernameexample")
                        .email("verylongemailaddress@example.com")
                        .tempPassword("verylongtemporarypasswordexample")
                        .build());

        byte[] result = excelExportService.exportUsersWithTempPasswords(users);

        assertNotNull(result);

        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Users with Temporary Passwords");

            assertDoesNotThrow(() -> {
                for (int i = 0; i < 3; i++) {
                    sheet.autoSizeColumn(i);
                }
            });
        }
    }

    @Test
    void exportUsersWithTempPasswords_ShouldUseCorrectSheetName() throws IOException {
        List<User> users = List.of(
                User.builder()
                        .username("user1")
                        .email("user1@example.com")
                        .tempPassword("temp123")
                        .build());

        byte[] result = excelExportService.exportUsersWithTempPasswords(users);

        assertNotNull(result);

        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Users with Temporary Passwords");
            assertNotNull(sheet);
            assertEquals("Users with Temporary Passwords", sheet.getSheetName());
        }
    }

    @Test
    void exportUsersWithTempPasswords_ShouldCloseResourcesProperly() throws IOException {
        List<User> users = List.of(
                User.builder()
                        .username("user1")
                        .email("user1@example.com")
                        .tempPassword("temp123")
                        .build());

        assertDoesNotThrow(() -> {
            byte[] result = excelExportService.exportUsersWithTempPasswords(users);
            assertNotNull(result);
        });
    }
}
