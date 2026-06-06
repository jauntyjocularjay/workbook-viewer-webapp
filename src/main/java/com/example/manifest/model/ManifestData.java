package com.example.manifest.model;

import java.util.List;

public record ManifestData(List<String> headers, List<List<String>> rows) {}
