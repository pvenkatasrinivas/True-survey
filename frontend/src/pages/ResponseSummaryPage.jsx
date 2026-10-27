import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import { getResponses, getSummary } from '../services/surveyService';
import { QUESTION_TYPES, QUESTION_TYPE_LABELS } from '../utils/constants';
import { formatDateTime, pluralize } from '../utils/format';

// Results of a survey: totals, per-question counts, and the list of individual responses.
// The data is fetched every time the page is opened, so new submissions always show up.
function ResponseSummaryPage() {
  const { id } = useParams();
  const [summary, setSummary] = useState(null);
  const [responses, setResponses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadData() {
    setLoading(true);
    setError('');
    try {
      // Run both requests at the same time
      const [summaryData, responseData] = await Promise.all([getSummary(id), getResponses(id)]);
      setSummary(summaryData);
      setResponses(responseData || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [id]);

  if (loading) {
    return <Loading message="Loading results..." />;
  }
  if (error) {
    return (
      <div>
        <ErrorMessage message={error} onRetry={loadData} />
        <Link to="/">Back to surveys</Link>
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <p className="muted small">Results for</p>
          <h1>{summary.title}</h1>
        </div>
        <div className="button-row">
          <button type="button" className="btn btn-secondary" onClick={loadData}>
            Refresh
          </button>
          <Link to={`/surveys/${id}`} className="btn btn-secondary">
            Survey details
          </Link>
          <Link to={`/surveys/${id}/take`} className="btn btn-primary">
            Take survey
          </Link>
        </div>
      </div>

      <div className="card stat-card">
        <span className="stat-number">{summary.totalResponses}</span>
        <span className="stat-label">Total responses</span>
      </div>

      {summary.totalResponses === 0 ? (
        <EmptyState title="No responses yet" message="Results will appear here once people answer this survey.">
          <Link to={`/surveys/${id}/take`} className="btn btn-primary">
            Be the first to answer
          </Link>
        </EmptyState>
      ) : (
        <>
          <h2 className="section-title">Question results</h2>
          {summary.questions.map((question, index) => (
            <QuestionSummary key={question.questionId} question={question} number={index + 1} />
          ))}

          <h2 className="section-title">Individual responses</h2>
          <ResponseTable responses={responses} />
        </>
      )}
    </div>
  );
}

// Results for one question: bars for choice questions, a list for text questions.
function QuestionSummary({ question, number }) {
  const isText = question.type === QUESTION_TYPES.SHORT_TEXT;

  return (
    <div className="card">
      <h3>
        {number}. {question.text}
      </h3>
      <p className="muted small">
        {QUESTION_TYPE_LABELS[question.type]} · {pluralize(question.answeredCount, 'answer')}
      </p>

      {isText ? (
        <TextAnswers answers={question.textAnswers} />
      ) : (
        <>
          <div className="result-list">
            {question.options.map((option) => (
              <div className="result-row" key={option.optionId}>
                <div className="result-row-label">
                  <span>{option.text}</span>
                  <span className="muted">
                    {option.count} ({option.percentage}%)
                  </span>
                </div>
                <div className="bar-track">
                  {/* Width can't go above 100% even if the number is bigger */}
                  <div className="bar-fill" style={{ width: `${Math.min(option.percentage, 100)}%` }} />
                </div>
              </div>
            ))}
          </div>
          {question.type === QUESTION_TYPES.MULTIPLE_CHOICE && (
            <p className="muted small note">
              People could pick more than one option, so the percentages can add up to more than 100%.
            </p>
          )}
        </>
      )}
    </div>
  );
}

function TextAnswers({ answers }) {
  if (!answers || answers.length === 0) {
    return <p className="muted">No text answers yet.</p>;
  }
  return (
    <>
      <ul className="text-answers">
        {answers.map((answer, index) => (
          <li key={index}>{answer}</li>
        ))}
      </ul>
      <p className="muted small note">Showing the latest {answers.length} answers.</p>
    </>
  );
}

// Table of every response: when it was submitted and what was answered.
function ResponseTable({ responses }) {
  if (responses.length === 0) {
    return <p className="muted">No individual responses to show.</p>;
  }
  return (
    <div className="card table-wrapper">
      <table className="response-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Submitted</th>
            <th>Answers</th>
          </tr>
        </thead>
        <tbody>
          {responses.map((response, index) => (
            <tr key={response.id}>
              <td>{responses.length - index}</td>
              <td className="nowrap">{formatDateTime(response.submittedAt)}</td>
              <td>
                {response.answers.length === 0 ? (
                  <span className="muted">No answers</span>
                ) : (
                  <dl className="answer-list">
                    {response.answers.map((answer) => (
                      <div key={answer.questionId}>
                        <dt>{answer.questionText}</dt>
                        <dd>{answer.answer}</dd>
                      </div>
                    ))}
                  </dl>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default ResponseSummaryPage;
