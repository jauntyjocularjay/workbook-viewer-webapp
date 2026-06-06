package com.example.manifest.service;

import com.example.manifest.model.ManifestData;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelParserService {

    public ManifestData parse(InputStream is) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            List<String> headers = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();

            boolean firstRow = true;
            for (Row row : sheet) {
                List<String> cells = new ArrayList<>();
                int lastCol = row.getLastCellNum();
                for (int i = 0; i < lastCol; i++) {
                    cells.add(formatter.formatCellValue(row.getCell(i)));
                }
                if (firstRow) {
                    headers = cells;
                    firstRow = false;
                } else {
                    rows.add(cells);
                }
            }

            return new ManifestData(headers, rows);
        }
    }
}
