package com.codereview.backend.service;

import com.codereview.backend.entity.HintEntity;
import com.codereview.backend.entity.ProblemEntity;
import com.codereview.backend.entity.SubmissionEntity;
import com.codereview.backend.entity.TestCaseEntity;
import com.codereview.backend.entity.UserEntity;
import com.codereview.backend.repository.ProblemRepository;
import com.codereview.backend.repository.SubmissionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final ReviewService reviewService;
    private final DsaProblemSeeder dsaProblemSeeder;

    public List<ProblemEntity> getAllProblems(Long userId) {
        List<ProblemEntity> problems = problemRepository.findAll();
        enrichWithUserStatus(problems, userId);
        return problems;
    }

    public Optional<ProblemEntity> getProblemById(Long id, Long userId) {
        Optional<ProblemEntity> problem = problemRepository.findById(id);
        problem.ifPresent(p -> enrichWithUserStatus(Collections.singletonList(p), userId));
        return problem;
    }

    public Optional<ProblemEntity> getProblemBySlug(String slug, Long userId) {
        Optional<ProblemEntity> problem = problemRepository.findBySlug(slug);
        problem.ifPresent(p -> enrichWithUserStatus(Collections.singletonList(p), userId));
        return problem;
    }

    public List<ProblemEntity> getProblemsByDifficulty(String difficulty, Long userId) {
        List<ProblemEntity> problems = problemRepository.findByDifficulty(difficulty);
        enrichWithUserStatus(problems, userId);
        return problems;
    }

    public List<ProblemEntity> getProblemsByCategory(String category, Long userId) {
        List<ProblemEntity> problems = problemRepository.findByCategory(category);
        enrichWithUserStatus(problems, userId);
        return problems;
    }

    private void enrichWithUserStatus(List<ProblemEntity> problems, Long userId) {
        UserEntity user = reviewService.resolveUser(userId);
        List<SubmissionEntity> userSubmissions = submissionRepository.findByUserId(user.getId());

        Map<Long, String> statusMap = new HashMap<>();
        for (SubmissionEntity sub : userSubmissions) {
            if (sub.getProblem() == null) continue;
            Long pId = sub.getProblem().getId();
            String currentStatus = statusMap.get(pId);

            if ("SOLVED".equals(currentStatus)) {
                continue;
            }

            boolean isPassed = "PASSED".equalsIgnoreCase(sub.getStatus())
                    || "SOLVED".equalsIgnoreCase(sub.getStatus())
                    || "ACCEPTED".equalsIgnoreCase(sub.getStatus());

            if (isPassed) {
                statusMap.put(pId, "SOLVED");
            } else {
                statusMap.put(pId, "ATTEMPTED");
            }
        }

        for (ProblemEntity problem : problems) {
            String status = statusMap.getOrDefault(problem.getId(), "NOT_ATTEMPTED");
            problem.setUserStatus(status);
        }
    }

    @PostConstruct
    @Transactional
    public void seedInitialProblems() {
        dsaProblemSeeder.seedProblems();
    }
}
