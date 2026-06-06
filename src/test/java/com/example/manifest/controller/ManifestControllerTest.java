package com.example.manifest.controller;

import com.example.manifest.model.ManifestData;
import com.example.manifest.service.ExcelParserService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ManifestControllerTest {

    @Mock
    ExcelParserService excelParserService;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ManifestController(excelParserService))
                .build();
    }

    @Test
    void getIndexReturnsUploadForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void uploadEmptyFileRedirectsWithError() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        mockMvc.perform(multipart("/upload").file(empty))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Please select a file to upload."));
    }

    @Test
    void uploadNonXlsxRedirectsWithError() throws Exception {
        MockMultipartFile csv = new MockMultipartFile("file", "data.csv",
                "text/csv", "a,b\n1,2".getBytes());

        mockMvc.perform(multipart("/upload").file(csv))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Only .xlsx files are supported."));
    }

    @Test
    void uploadValidXlsxRendersResults() throws Exception {
        ManifestData stubData = new ManifestData(
                List.of("Name", "Qty"),
                List.of(List.of("Widget", "5"))
        );
        when(excelParserService.parse(any())).thenReturn(stubData);

        MockMultipartFile file = new MockMultipartFile("file", "manifest.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildMinimalXlsx());

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("results"))
                .andExpect(model().attribute("filename", "manifest.xlsx"))
                .andExpect(model().attribute("data", stubData));
    }

    @Test
    void uploadPassesAllRowsToModel() throws Exception {
        ManifestData stubData = new ManifestData(
                List.of("SKU", "Description", "Qty"),
                List.of(
                        List.of("A001", "Widget A", "10"),
                        List.of("B002", "Widget B", "5"),
                        List.of("A003", "Gadget A", "3")
                )
        );
        when(excelParserService.parse(any())).thenReturn(stubData);

        MockMultipartFile file = new MockMultipartFile("file", "manifest.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildMinimalXlsx());

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("results"))
                .andExpect(model().attribute("data", stubData));
    }

    private byte[] buildMinimalXlsx() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            wb.createSheet("Sheet1").createRow(0).createCell(0).setCellValue("Name");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }
}
