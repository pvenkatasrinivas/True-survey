import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import QuestionField from '../components/QuestionField';

const options = [
  { id: 21, text: 'Good' },
  { id: 22, text: 'Poor' },
];

describe('QuestionField', () => {
  it('renders a text box for SHORT_TEXT and sends the typed text', () => {
    const onChange = vi.fn();
    const question = { id: 10, text: 'Your name', type: 'SHORT_TEXT', required: false, options: [] };
    render(<QuestionField question={question} number={1} value="" onChange={onChange} />);

    const textbox = screen.getByRole('textbox', { name: /your name/i });
    expect(textbox).toBeInTheDocument();
    expect(screen.queryByRole('radio')).not.toBeInTheDocument();
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument();

    fireEvent.change(textbox, { target: { value: 'Ravi' } });
    expect(onChange).toHaveBeenCalledWith('Ravi');
  });

  it('renders radio buttons for SINGLE_CHOICE and sends one option id', () => {
    const onChange = vi.fn();
    const question = { id: 11, text: 'How was the food?', type: 'SINGLE_CHOICE', required: true, options };
    render(<QuestionField question={question} number={2} value={[]} onChange={onChange} />);

    expect(screen.getAllByRole('radio')).toHaveLength(2);
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument();

    fireEvent.click(screen.getByLabelText('Poor'));
    expect(onChange).toHaveBeenCalledWith([22]);
  });

  it('renders checkboxes for MULTIPLE_CHOICE and adds to the selection', () => {
    const onChange = vi.fn();
    const question = { id: 12, text: 'Which meals?', type: 'MULTIPLE_CHOICE', required: true, options };
    render(<QuestionField question={question} number={3} value={[21]} onChange={onChange} />);

    const checkboxes = screen.getAllByRole('checkbox');
    expect(checkboxes).toHaveLength(2);
    expect(screen.getByLabelText('Good')).toBeChecked();
    expect(screen.queryByRole('radio')).not.toBeInTheDocument();

    fireEvent.click(screen.getByLabelText('Poor'));
    expect(onChange).toHaveBeenCalledWith([21, 22]);
  });

  it('shows the required marker and the error text', () => {
    const question = { id: 13, text: 'Comments', type: 'SHORT_TEXT', required: true, options: [] };
    render(<QuestionField question={question} value="" error="This question is required" onChange={() => {}} />);

    expect(screen.getByTitle('Required')).toBeInTheDocument();
    expect(screen.getByText('This question is required')).toBeInTheDocument();
  });
});
