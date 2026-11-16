package com.dai.microservice.rag.infrastructure.adapter.passive.excel;

import com.dai.microservice.rag.application.port.out.MeasurementReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Component;

@Component
public class ZipMeasurementReader implements MeasurementReader {
    private static final long MAX_ENTRY_BYTES = 128L * 1024 * 1024;
    private static final long MAX_TOTAL_BYTES = 1024L * 1024 * 1024;
    private final StreamingExcelParser parser = new StreamingExcelParser();

    @Override
    public void read(InputStream input, String archiveName, MeasurementHandler handler) throws Exception {
        long total = 0;
        int workbooks = 0;
        int entries = 0;
        try (var zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (++entries > 10_000) throw new IOException("ZIP contains more than 10,000 entries");
                String name = entry.getName();
                String lower = name.toLowerCase(Locale.ROOT);
                boolean workbook = !entry.isDirectory()
                        && (lower.endsWith(".xlsx") || lower.endsWith(".xlsm"));
                if (workbook) {
                    Path temp = Files.createTempFile("windfarm-", ".xlsx");
                    try {
                        long written = copyBounded(zip, temp, MAX_ENTRY_BYTES);
                        total = ensureTotal(total, written);
                        String entryName = name.replace('\\', '/');
                        parser.parse(temp, archiveName + "!" + entryName.substring(entryName.lastIndexOf('/') + 1), handler);
                        workbooks++;
                    } finally {
                        Files.deleteIfExists(temp);
                    }
                } else {
                    total = ensureTotal(total, drainBounded(zip, MAX_TOTAL_BYTES - total));
                }
                zip.closeEntry();
            }
        }
        if (workbooks == 0) throw new IllegalArgumentException("ZIP must contain at least one .xlsx or .xlsm workbook");
    }

    private static long copyBounded(InputStream input, Path output, long limit) throws IOException {
        long total = 0;
        byte[] buffer = new byte[64 * 1024];
        try (var target = Files.newOutputStream(output)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > limit) throw new IOException("Workbook expands beyond the 128 MiB limit");
                target.write(buffer, 0, read);
            }
        }
        return total;
    }

    private static long drainBounded(InputStream input, long limit) throws IOException {
        long total = 0;
        byte[] buffer = new byte[64 * 1024];
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > limit) throw new IOException("ZIP expands beyond the 1 GiB limit");
        }
        return total;
    }

    private static long ensureTotal(long current, long added) throws IOException {
        if (added > MAX_TOTAL_BYTES - current) throw new IOException("ZIP expands beyond the 1 GiB limit");
        return current + added;
    }
}
