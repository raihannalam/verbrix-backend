package com.verbrix.controller;

import com.verbrix.payload.publiccontroller.response.InterpreterPublicResponse;
import com.verbrix.service.InterpreterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final InterpreterService interpreterService;


    @GetMapping("get-started-available-interpreters")
    public Page<InterpreterPublicResponse> getAvailableInterpreters(
            @PageableDefault(
                    page = 0,
                    size = 4,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return interpreterService.getVerifiedAvailableInterpreters(pageable);
    }
}
