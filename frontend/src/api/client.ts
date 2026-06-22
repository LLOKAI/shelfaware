import type {
  AuthResponse,
  Book,
  ExternalBook,
  ExternalBookSearchResponse,
  PageResponse,
  ReadingInsights,
  ReadingStatus,
  Journey,
  ProgressUpdate,
  ReadingGoal,
  Review,
  ShelfItem,
  User
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

type RequestOptions = {
  token?: string | null;
  body?: unknown;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
};

export class ApiError extends Error {
  status: number;
  details: string[];

  constructor(status: number, message: string, details: string[] = []) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers();
  headers.set('Accept', 'application/json');

  if (options.body !== undefined) {
    headers.set('Content-Type', 'application/json');
  }

  if (options.token) {
    headers.set('Authorization', `Bearer ${options.token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? (options.body === undefined ? 'GET' : 'POST'),
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body)
  });

  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new ApiError(response.status, payload?.message ?? 'Request failed', payload?.details ?? []);
  }

  return payload as T;
}

export const api = {
  register: (body: { displayName: string; email: string; username: string; password: string }) =>
    request<AuthResponse>('/api/auth/register', { body }),

  login: (body: { usernameOrEmail: string; password: string }) =>
    request<AuthResponse>('/api/auth/login', { body }),

  demo: () => request<AuthResponse>('/api/auth/demo', { method: 'POST' }),

  me: (token: string) => request<User>('/api/auth/me', { token }),

  searchBooks: (query: string) => {
    const params = new URLSearchParams({ size: '12', sort: 'title' });
    if (query.trim()) {
      params.set('q', query.trim());
    }
    return request<PageResponse<Book>>(`/api/books?${params.toString()}`);
  },

  getBook: (id: number) => request<Book>(`/api/books/${id}`),

  searchExternalBooks: (query: string, limit = 8) => {
    const params = new URLSearchParams({ q: query, limit: String(limit) });
    return request<ExternalBookSearchResponse>(`/api/books/external-search?${params.toString()}`);
  },

  importBook: (token: string, book: ExternalBook) =>
    request<Book>('/api/books/import', { token, body: book }),

  getShelf: (token: string, status?: ReadingStatus) => {
    const params = new URLSearchParams();
    if (status) {
      params.set('status', status);
    }
    const suffix = params.toString() ? `?${params.toString()}` : '';
    return request<ShelfItem[]>(`/api/me/shelf${suffix}`, { token });
  },

  updateShelfItem: (
    token: string,
    bookId: number,
    body: { status: ReadingStatus; privateNotes?: string | null; startedOn?: string | null; finishedOn?: string | null; favorite?: boolean }
  ) => request<ShelfItem>(`/api/me/shelf/${bookId}`, { token, method: 'PUT', body }),

  getReviews: (bookId: number) => request<Review[]>(`/api/books/${bookId}/reviews`),

  reviewBook: (token: string, bookId: number, body: { rating: number; body: string; publicReview: boolean }) =>
    request<Review>(`/api/books/${bookId}/reviews/me`, { token, method: 'PUT', body }),

  getInsights: (token: string) => request<ReadingInsights>('/api/me/insights', { token }),

  getJourney: (token: string, year = new Date().getFullYear()) =>
    request<Journey>(`/api/me/journey?year=${year}`, { token }),

  updateProgress: (token: string, bookId: number, currentPage: number, readOn: string) =>
    request<ProgressUpdate>(`/api/me/shelf/${bookId}/progress`, {
      token,
      method: 'POST',
      body: { currentPage, readOn }
    }),

  undoReadingSession: (token: string, sessionId: number) =>
    request<void>(`/api/me/reading-sessions/${sessionId}`, { token, method: 'DELETE' }),

  saveReadingGoal: (token: string, year: number, targetBooks: number | null, targetPages: number | null) =>
    request<ReadingGoal>(`/api/me/reading-goals/${year}`, {
      token,
      method: 'PUT',
      body: { targetBooks, targetPages }
    })
};
