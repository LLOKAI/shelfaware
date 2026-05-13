export type Role = 'USER' | 'ADMIN';

export type ReadingStatus = 'WANT_TO_READ' | 'READING' | 'FINISHED' | 'FAVORITE';

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
};

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};
