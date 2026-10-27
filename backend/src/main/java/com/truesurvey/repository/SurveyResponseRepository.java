package com.truesurvey.repository;

import com.truesurvey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    long countBySurvey_Id(Long surveyId);

    /** Rows of [surveyId, numberOfResponses]. */
    @Query("SELECT r.survey.id, COUNT(r) FROM SurveyResponse r GROUP BY r.survey.id")
    List<Object[]> countResponsesPerSurvey();

    /** All responses of a survey with their answers, newest first ("join fetch" loads answers in the same query). */
    @Query("SELECT DISTINCT r FROM SurveyResponse r LEFT JOIN FETCH r.answers "
            + "WHERE r.survey.id = :surveyId ORDER BY r.submittedAt DESC, r.id DESC")
    List<SurveyResponse> findWithAnswersBySurveyId(@Param("surveyId") Long surveyId);

    /** Used when deleting a survey: remove its responses (answers must be deleted first, see AnswerRepository). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SurveyResponse r WHERE r.survey.id = :surveyId")
    int deleteBySurveyId(@Param("surveyId") Long surveyId);
}
