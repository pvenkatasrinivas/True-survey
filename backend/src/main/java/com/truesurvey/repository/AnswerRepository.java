package com.truesurvey.repository;

import com.truesurvey.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /** Every answer given to a survey, newest response first. Used to build the summary. */
    @Query("SELECT a FROM Answer a JOIN FETCH a.response r "
            + "WHERE r.survey.id = :surveyId ORDER BY r.submittedAt DESC, r.id DESC, a.id ASC")
    List<Answer> findAllBySurveyId(@Param("surveyId") Long surveyId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Answer a WHERE a.response.id IN "
            + "(SELECT r.id FROM SurveyResponse r WHERE r.survey.id = :surveyId)")
    int deleteBySurveyId(@Param("surveyId") Long surveyId);
}
