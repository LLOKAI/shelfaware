export type Role = 'USER' | 'ADMIN';

export type ReadingStatus = 'WANT_TO_READ' | 'READING' | 'FINISHED';

export type User = {
  id: number;
  displayName: string;
  email: string;
  username: string;
  roles: Role[];
};

export type AuthResponse = {
  tokenType: 'Bearer';
  accessToken: string;
  expiresAt: string;
  user: User;
};

export type Book = {
  id: number;
  title: string;
  authors: string;
  isbn: string | null;
  description: string | null;
  coverImageUrl: string | null;
  publisher: string | null;
  categories: string | null;
  externalSource: string | null;
  externalId: string | null;
  publishedDate: string | null;
  pageCount: number | null;
  createdAt: string;
  updatedAt: string;
};

export type ExternalBook = {
  externalSource: string;
  externalId: string;
  title: string;
  authors: string;
  isbn: string | null;
  description: string | null;
  coverImageUrl: string | null;
  publisher: string | null;
  categories: string | null;
  publishedDate: string | null;
  pageCount: number | null;
};

export type ExternalBookSearchResponse = {
  query: string;
  totalResults: number;
  books: ExternalBook[];
};

export type ShelfItem = {
  id: number;
  book: Book;
  status: ReadingStatus;
  startedOn: string | null;
  finishedOn: string | null;
  currentPage: number;
  favorite: boolean;
  privateNotes: string | null;
  createdAt: string;
  updatedAt: string;
};

export type Review = {
  id: number;
  bookId: number;
  bookTitle: string;
  userId: number;
  username: string;
  rating: number;
  body: string;
  publicReview: boolean;
  createdAt: string;
  updatedAt: string;
};

export type ReadingInsights = {
  totalShelfItems: number;
  wantToReadCount: number;
  readingCount: number;
  finishedCount: number;
  favoriteCount: number;
  reviewCount: number;
  averageRating: number;
  ratingsDistribution: Record<string, number>;
  pagesRead: number;
  currentStreak: number;
  averagePagesPerSession: number;
  monthlyPages: Record<string, number>;
  monthlyBooks: Record<string, number>;
};

export type ReadingSession = {
  id: number;
  bookId: number;
  bookTitle: string;
  coverImageUrl: string | null;
  readOn: string;
  startPage: number;
  endPage: number;
  pagesRead: number;
  completedBook: boolean;
  createdAt: string;
};

export type ReadingGoal = {
  year: number;
  targetBooks: number | null;
  targetPages: number | null;
  completedBooks: number;
  pagesRead: number;
  booksProgress: number;
  pagesProgress: number;
  projectedBooks: number;
  projectedPages: number;
};

export type Journey = {
  year: number;
  goal: ReadingGoal;
  streak: { currentDays: number; longestDays: number };
  activeBooks: ShelfItem[];
  recentSessions: ReadingSession[];
  pagesRead: number;
  completedBooks: number;
};

export type ProgressUpdate = {
  shelfItem: ShelfItem;
  session: ReadingSession;
  milestone: string | null;
};

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};
