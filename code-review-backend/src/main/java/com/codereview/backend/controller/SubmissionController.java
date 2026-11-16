package com.codereview.backend.controller;

import com.codereview.backend.dto.SubmissionHistoryDto;
import com.codereview.backend.dto.SubmissionRequestDto;
import com.codereview.backend.service.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<SubmissionService.SubmissionResponse> submitSolution(@Valid @RequestBody SubmissionRequestDto request) {
        SubmissionService.SubmissionResponse response = submissionService.submitSolution(
                request.getProblemId(), request.getCode(), request.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<SubmissionHistoryDto>> getSubmissionsByProblem(
            @PathVariable Long problemId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(submissionService.getSubmissionHistoryByProblem(problemId, userId));
    }
}
