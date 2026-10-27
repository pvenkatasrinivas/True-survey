package com.truesurvey.repository;

import com.truesurvey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** Spring Data JPA generates the implementation of this interface at runtime. */
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    List<Survey> findAllByOrderByCreatedAtDescIdDesc();

    List<Survey> findByTitleContainingIgnoreCaseOrderByCreatedAtDescIdDesc(String title);

    /** Rows of [surveyId, numberOfQuestions] - one query instead of one per survey. */
    @Query("SELECT q.survey.id, COUNT(q) FROM Question q GROUP BY q.survey.id")
    List<Object[]> countQuestionsPerSurvey();
}
