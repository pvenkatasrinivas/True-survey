import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import SurveyForm from '../components/SurveyForm';

describe('SurveyForm', () => {
  it('shows an error and does not save when the title is empty', () => {
    const onSubmit = vi.fn();
    render(<SurveyForm onSubmit={onSubmit} />);

    fireEvent.click(screen.getByRole('button', { name: 'Save survey' }));

    expect(screen.getByText('Title is required')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('sends the survey request when the form is valid', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<SurveyForm onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/title/i), { target: { value: 'Canteen Feedback' } });
    fireEvent.change(screen.getByLabelText('Question text'), { target: { value: 'Your name' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save survey' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1));
    expect(onSubmit).toHaveBeenCalledWith({
      title: 'Canteen Feedback',
      description: '',
      questions: [{ text: 'Your name', type: 'SHORT_TEXT', required: false, options: [] }],
    });
  });

  it('shows server field errors under the matching field', async () => {
    const serverError = new Error('Validation failed');
    serverError.fieldErrors = { 'questions[0].text': 'Question text is too short' };
    const onSubmit = vi.fn().mockRejectedValue(serverError);
    render(<SurveyForm onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/title/i), { target: { value: 'Canteen Feedback' } });
    fireEvent.change(screen.getByLabelText('Question text'), { target: { value: 'Your name' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save survey' }));

    expect(await screen.findByText('Question text is too short')).toBeInTheDocument();
    expect(screen.getByText('Validation failed')).toBeInTheDocument();
  });

  it('shows questions read-only when questionsLocked is true', () => {
    const initialValues = {
      title: 'Canteen Feedback',
      description: '',
      questions: [{ key: 'a', text: 'How was the food?', type: 'SINGLE_CHOICE', required: true, options: ['Good', 'Poor'] }],
    };
    render(<SurveyForm initialValues={initialValues} questionsLocked onSubmit={vi.fn()} />);

    expect(screen.getByText("This survey already has responses, so questions can't be changed.")).toBeInTheDocument();
    expect(screen.getByText('How was the food?')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /add question/i })).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Question text')).not.toBeInTheDocument();
  });
});
