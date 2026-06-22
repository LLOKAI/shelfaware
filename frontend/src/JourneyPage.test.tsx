import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { JourneyPage } from './JourneyPage';
import { api } from './api/client';

const notify = vi.fn();

vi.mock('./state/AuthContext', () => ({
  useAuth: () => ({ token: 'test-token', user: { displayName: 'Liam' } })
}));

vi.mock('./state/ToastContext', () => ({
  useToast: () => ({ notify })
}));

vi.mock('./api/client', async (importOriginal) => {
  const original = await importOriginal<typeof import('./api/client')>();
  return {
    ...original,
    api: {
      ...original.api,
      getJourney: vi.fn(),
      saveReadingGoal: vi.fn(),
      updateProgress: vi.fn(),
      undoReadingSession: vi.fn()
    }
  };
});

const emptyJourney = {
  year: new Date().getFullYear(),
  goal: {
    year: new Date().getFullYear(), targetBooks: null, targetPages: null,
    completedBooks: 0, pagesRead: 0, booksProgress: 0, pagesProgress: 0,
    projectedBooks: 0, projectedPages: 0
  },
  streak: { currentDays: 0, longestDays: 0 },
  activeBooks: [],
  recentSessions: [],
  pagesRead: 0,
  completedBooks: 0
};

function renderJourney() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter><JourneyPage /></MemoryRouter>
    </QueryClientProvider>
  );
}

describe('JourneyPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.getJourney).mockResolvedValue(emptyJourney);
  });

  it('shows useful empty states for a new reader', async () => {
    renderJourney();
    expect(await screen.findByText('Welcome back, Liam.')).toBeInTheDocument();
    expect(screen.getByText('Nothing in progress')).toBeInTheDocument();
    expect(screen.getByText('Your history starts here')).toBeInTheDocument();
  });

  it('creates an annual goal', async () => {
    vi.mocked(api.saveReadingGoal).mockResolvedValue({ ...emptyJourney.goal, targetBooks: 12, targetPages: 5000 });
    renderJourney();
    fireEvent.click(await screen.findByText(`Set a gentle goal for ${new Date().getFullYear()}`));
    fireEvent.change(screen.getByLabelText('Books'), { target: { value: '12' } });
    fireEvent.change(screen.getByLabelText('Pages'), { target: { value: '5000' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save goals' }));
    await waitFor(() => expect(api.saveReadingGoal).toHaveBeenCalledWith('test-token', new Date().getFullYear(), 12, 5000));
    expect(notify).toHaveBeenCalledWith(`${new Date().getFullYear()} reading goal saved.`);
  });

  it('opens progress entry and reports mutation failures', async () => {
    vi.mocked(api.getJourney).mockResolvedValue({
      ...emptyJourney,
      activeBooks: [{
        id: 1,
        book: { id: 4, title: 'Effective Java', authors: 'Joshua Bloch', pageCount: 416, coverImageUrl: null },
        status: 'READING', currentPage: 40, favorite: false, startedOn: null, finishedOn: null,
        privateNotes: null, createdAt: '', updatedAt: ''
      } as never]
    });
    vi.mocked(api.updateProgress).mockRejectedValue(new Error('Could not log progress'));
    renderJourney();
    fireEvent.click(await screen.findByRole('button', { name: 'Update' }));
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByLabelText('New current page')).toHaveValue(41);
    fireEvent.click(screen.getByRole('button', { name: 'Log progress' }));
    await waitFor(() => expect(notify).toHaveBeenCalledWith('Could not log progress', 'error'));
  });
});
