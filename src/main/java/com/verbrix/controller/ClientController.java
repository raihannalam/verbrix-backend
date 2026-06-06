package com.verbrix.controller;

import com.verbrix.payload.publiccontroller.response.InterpreterAuthenticatedResponse;
import com.verbrix.payload.publiccontroller.response.InterpreterPublicResponse;
import com.verbrix.service.InterpreterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/clients")
@RestController
@PreAuthorize( "hasRole('CLIENT')")
@RequiredArgsConstructor
public class ClientController {

    private final InterpreterService interpreterService;

    @GetMapping("/interpreters")
    public ResponseEntity<Page<InterpreterPublicResponse>> getInterpreters() {
        return ResponseEntity.ok(interpreterService.getVerifiedAvailableInterpreters(Pageable.unpaged()));
    }

    @GetMapping("/interpreters/{id}")
    public ResponseEntity<InterpreterAuthenticatedResponse> getInterpreterById(@PathVariable Long id) {
        return ResponseEntity.ok(interpreterService.getAuthenticatedInterpreterById(id));
    }

}