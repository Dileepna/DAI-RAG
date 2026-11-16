package com.dai.microservice.rag.infrastructure.adapter.passive.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;

import com.dai.microservice.rag.domain.Measurement;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class StreamingExcelParserTest {
    @Test
    void streamsRowsIntoMeasurements() throws Exception {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("readings");
        var header = sheet.createRow(0);
        for (String value : new String[] {"Device UID", "Timestamp", "Measurement", "Value", "Unit"}) {
            header.createCell(java.util.Arrays.asList("Device UID", "Timestamp", "Measurement", "Value", "Unit")
                    .indexOf(value)).setCellValue(value);
        }
        var data = sheet.createRow(1);
        data.createCell(0).setCellValue("T-01");
        data.createCell(1).setCellValue("2026-01-02T03:04:05Z");
        data.createCell(2).setCellValue("wind_speed");
        data.createCell(3).setCellValue(12.5);
        data.createCell(4).setCellValue("m/s");
        var path = Files.createTempFile("windfarm-parser-test-", ".xlsx");
        try {
            try (var output = Files.newOutputStream(path)) { workbook.write(output); }
            var measurements = new ArrayList<Measurement>();
            new StreamingExcelParser().parse(path, "test.xlsx", measurements::add);
            assertEquals(1, measurements.size());
            assertEquals("T-01", measurements.getFirst().uid());
            assertEquals(Instant.parse("2026-01-02T03:04:05Z"), measurements.getFirst().timestamp());
            assertEquals("12.5", measurements.getFirst().value());
        } finally {
            workbook.close();
            Files.deleteIfExists(path);
        }
    }
}
