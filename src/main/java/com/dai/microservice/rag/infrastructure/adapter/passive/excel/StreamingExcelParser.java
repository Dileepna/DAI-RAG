package com.dai.microservice.rag.infrastructure.adapter.passive.excel;

import com.dai.microservice.rag.domain.Measurement;
import com.dai.microservice.rag.application.port.out.MeasurementReader.MeasurementHandler;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.util.XMLHelper;
import org.xml.sax.XMLReader;

public class StreamingExcelParser {
    public void parse(Path file, String source, MeasurementHandler handler) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(file.toFile(), PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            var strings = new ReadOnlySharedStringsTable(pkg);
            Iterator<InputStream> sheets = reader.getSheetsData();
            boolean foundSheet = false;
            while (sheets.hasNext()) {
                foundSheet = true;
                try (InputStream sheet = sheets.next()) {
                    var rows = new RowHandler(source, handler);
                    XMLReader xml = XMLHelper.newXMLReader();
                    xml.setContentHandler(new XSSFSheetXMLHandler(styles, null, strings, rows,
                            new DataFormatter(Locale.ROOT), false));
                    try {
                        xml.parse(new org.xml.sax.InputSource(sheet));
                    } catch (HandlerFailure failure) {
                        throw failure.getCause();
                    }
                }
            }
            if (!foundSheet) throw new IllegalArgumentException("Workbook contains no worksheets");
        }
    }

    private static final class RowHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final String source;
        private final MeasurementHandler handler;
        private List<String> row = new ArrayList<>();
        private Map<String, Integer> columns;

        private RowHandler(String source, MeasurementHandler handler) {
            this.source = source;
            this.handler = handler;
        }

        @Override public void startRow(int rowNum) { row = new ArrayList<>(); }

        @Override
        public void endRow(int rowNum) {
            if (columns == null) {
                columns = new HashMap<>();
                for (int i = 0; i < row.size(); i++) columns.put(normalize(row.get(i)), i);
                requireColumn("uid", "deviceuid", "deviceid", "turbineid");
                requireColumn("timestamp", "datetime", "dateandtime", "time");
                requireColumn("measurement", "parameter", "signal", "tag", "name");
                requireColumn("value", "reading", "measurementvalue");
                return;
            }
            String uid = value("uid", "deviceuid", "deviceid", "turbineid");
            if (uid.isBlank()) return;
            String timestamp = value("timestamp", "datetime", "dateandtime", "time");
            try {
                handler.accept(new Measurement(uid, parseTimestamp(timestamp),
                        value("measurement", "parameter", "signal", "tag", "name"),
                        value("value", "reading", "measurementvalue"),
                        value("unit", "uom"), source));
            } catch (Exception exception) {
                throw new HandlerFailure(exception);
            }
        }

        @Override
        public void cell(String ref, String formattedValue, XSSFComment comment) {
            int index = new CellReference(ref).getCol();
            while (row.size() <= index) row.add("");
            row.set(index, formattedValue == null ? "" : formattedValue.trim());
        }

        @Override public void headerFooter(String text, boolean isHeader, String tagName) { }

        private String value(String... aliases) {
            for (String alias : aliases) {
                Integer index = columns.get(alias);
                if (index != null && index < row.size()) return row.get(index);
            }
            return "";
        }

        private void requireColumn(String... aliases) {
            for (String alias : aliases) if (columns.containsKey(alias)) return;
            throw new IllegalArgumentException("Worksheet is missing required column: " + aliases[0]);
        }

        private static String normalize(String value) {
            return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        }

        private static Instant parseTimestamp(String value) {
            try { return Instant.parse(value); } catch (Exception ignored) { }
            try { return OffsetDateTime.parse(value).toInstant(); } catch (Exception ignored) { }
            try { return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC); }
            catch (Exception ignored) { }
            for (String pattern : List.of("yyyy-MM-dd HH:mm:ss", "M/d/uuuu H:mm:ss", "M/d/uuuu H:mm")) {
                try {
                    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern, Locale.ROOT))
                            .toInstant(ZoneOffset.UTC);
                } catch (Exception ignored) { }
            }
            throw new IllegalArgumentException("Invalid measurement timestamp: " + value);
        }
    }

    private static final class HandlerFailure extends RuntimeException {
        private HandlerFailure(Exception cause) { super(cause); }
        @Override public synchronized Exception getCause() { return (Exception) super.getCause(); }
    }
}
