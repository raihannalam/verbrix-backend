package com.verbrix.controller;

import com.verbrix.model.enums.ProficiencyLevel;
import com.verbrix.repository.LanguageRepository;
import com.verbrix.repository.SpecializationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/metadata")
@RequiredArgsConstructor
public class MetaDataController {

    private final SpecializationsRepository specializationRepository;
    private final LanguageRepository languageRepository;

    @GetMapping("/specializations")
    public ResponseEntity<List<Map<String, String>>> getSpecializations() {
        return ResponseEntity.ok(
                specializationRepository.findAll().stream()
                        .map(spec -> Map.of(
                                "id", spec.getName().name(),
                                "label", spec.getName().getLabel()
                        ))
                        .toList()
        );
    }

    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getLanguageMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put(
                "languages",
                languageRepository.findAll().stream()
                        .map(lang -> Map.of(
                                "id", lang.getName().name(),
                                "label", lang.getName().getLabel()
                        ))
                        .toList()
        );

        metadata.put(
                "proficiencyLevels",
                Arrays.stream(ProficiencyLevel.values())
                        .map(level -> Map.of(
                                "id", level.name()
                        ))
                        .toList()
        );

        return ResponseEntity.ok(metadata);
    }
}
