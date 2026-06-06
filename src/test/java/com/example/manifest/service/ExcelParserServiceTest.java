package com.example.manifest.service;

import com.example.manifest.model.ManifestData;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelParserServiceTest {

    private final ExcelParserService service = new ExcelParserService();

    @Test
    void parsesHeadersAndRows() throws Exception {
        byte[] xlsx = buildWorkbook(new String[][]{
                {"Name", "Quantity", "Price"},
                {"Widget A", "10", "9.99"},
                {"Widget B", "5", "19.99"}
        });

        ManifestData data = service.parse(new ByteArrayInputStream(xlsx));

        assertThat(data.headers()).containsExactly("Name", "Quantity", "Price");
        assertThat(data.rows()).hasSize(2);
        assertThat(data.rows().get(0)).containsExactly("Widget A", "10", "9.99");
        assertThat(data.rows().get(1)).containsExactly("Widget B", "5", "19.99");
    }

    @Test
    void parsesHeadersOnlySheet() throws Exception {
        byte[] xlsx = buildWorkbook(new String[][]{
                {"ID", "Description"}
        });

        ManifestData data = service.parse(new ByteArrayInputStream(xlsx));

        assertThat(data.headers()).containsExactly("ID", "Description");
        assertThat(data.rows()).isEmpty();
    }

    @Test
    void parsesEmptySheet() throws Exception {
        byte[] xlsx = buildWorkbook(new String[][]{});

        ManifestData data = service.parse(new ByteArrayInputStream(xlsx));

        assertThat(data.headers()).isEmpty();
        assertThat(data.rows()).isEmpty();
    }

    private byte[] buildWorkbook(String[][] rowData) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Sheet1");
            for (int r = 0; r < rowData.length; r++) {
                XSSFRow row = sheet.createRow(r);
                for (int c = 0; c < rowData[r].length; c++) {
                    row.createCell(c).setCellValue(rowData[r][c]);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }
}
