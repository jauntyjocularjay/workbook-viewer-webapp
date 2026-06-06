package com.example.manifest.controller;

import com.example.manifest.model.ManifestData;
import com.example.manifest.service.ExcelParserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ManifestController {

    private final ExcelParserService excelParserService;

    public ManifestController(ExcelParserService excelParserService) {
        this.excelParserService = excelParserService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/";
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            redirectAttributes.addFlashAttribute("error", "Only .xlsx files are supported.");
            return "redirect:/";
        }

        try {
            ManifestData data = excelParserService.parse(file.getInputStream());
            model.addAttribute("data", data);
            model.addAttribute("filename", filename);
            return "results";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to parse file: " + e.getMessage());
            return "redirect:/";
        }
    }
}
