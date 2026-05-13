import { FormEvent, useMemo, useState } from 'react';
import { Link, Navigate, NavLink, Route, Routes, useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  BarChart3,
  BookOpen,
  Check,
  Compass,
  Library,
  Loader2,
  LogOut,
  Plus,
  Search,
  ShieldCheck,
  Sparkles,
  Star,
  UserRound
} from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from 'recharts';
import { api, ApiError } from './api/client';
import { useAuth } from './state/AuthContext';
import type { Book, ExternalBook, ReadingStatus, Review, ShelfItem } from './types';

const STATUS_OPTIONS: Array<{ value: ReadingStatus; label: string }> = [
  { value: 'WANT_TO_READ', label: 'Want to read' },
  { value: 'READING', label: 'Reading' },
  { value: 'FINISHED', label: 'Finished' },
  { value: 'FAVORITE', label: 'Favorite' }
];

export function App() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <AuthPage />;
  }

  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<Navigate to="/discover" replace />} />
        <Route path="/discover" element={<DiscoverPage />} />
        <Route path="/library" element={<LibraryPage />} />
        <Route path="/shelf" element={<ShelfPage />} />
        <Route path="/insights" element={<InsightsPage />} />
        <Route path="/books/:bookId" element={<BookDetailPage />} />
        <Route path="*" element={<Navigate to="/discover" replace />} />
      </Routes>
    </AppShell>
  );
}

function AuthPage() {
  const { saveAuth } = useAuth();
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const authMutation = useMutation({
    mutationFn: () =>
      mode === 'login'
        ? api.login({ usernameOrEmail: username, password })
        : api.register({ displayName, email, username, password }),
    onSuccess: saveAuth
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    authMutation.mutate();
  }

  return (
    <main className="auth-layout">
      <section className="auth-copy">
        <div className="brand-lockup">
          <span className="brand-mark">
            <BookOpen size={24} />
          </span>
          <span>ShelfAware</span>
        </div>
        <h1>Read intentionally. Review thoughtfully. See your taste take shape.</h1>
        <p>
          A full-stack reading journal powered by Spring Boot APIs, JWT auth, external book search,
          personal shelves, reviews, and reading analytics.
        </p>
        <div className="feature-strip">
          <span><ShieldCheck size={18} /> JWT-secured</span>
          <span><Compass size={18} /> Book discovery</span>
          <span><BarChart3 size={18} /> Reading insights</span>
        </div>
      </section>

      <section className="auth-panel" aria-label="Authentication form">
        <div className="segmented">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>
            Login
          </button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>
            Register
          </button>
        </div>

        <form onSubmit={submit} className="form-stack">
          {mode === 'register' && (
            <>
              <label>
                Display name
                <input value={displayName} onChange={(event) => setDisplayName(event.target.value)} required />
              </label>
              <label>
                Email
                <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
              </label>
            </>
          )}
          <label>
            {mode === 'login' ? 'Username or email' : 'Username'}
            <input value={username} onChange={(event) => setUsername(event.target.value)} required />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              minLength={8}
              required
            />
          </label>
          <ErrorMessage error={authMutation.error} />
          <button className="primary-action" type="submit" disabled={authMutation.isPending}>
            {authMutation.isPending && <Loader2 className="spin" size={18} />}
            {mode === 'login' ? 'Login' : 'Create account'}
          </button>
        </form>
      </section>
    </main>
  );
}

function AppShell({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth();

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <Link to="/discover" className="brand-lockup compact">
          <span className="brand-mark">
            <BookOpen size={22} />
          </span>
          <span>ShelfAware</span>
        </Link>

        <nav className="nav-list">
          <NavItem to="/discover" icon={<Compass size={18} />} label="Discover" />
          <NavItem to="/library" icon={<Library size={18} />} label="Library" />
          <NavItem to="/shelf" icon={<BookOpen size={18} />} label="My shelf" />
          <NavItem to="/insights" icon={<BarChart3 size={18} />} label="Insights" />
        </nav>

        <div className="account-box">
          <UserRound size={18} />
          <div>
            <strong>{user?.displayName}</strong>
            <span>@{user?.username}</span>
          </div>
          <button className="icon-button" onClick={logout} aria-label="Logout" title="Logout">
            <LogOut size={18} />
          </button>
        </div>
      </aside>

      <main className="content-shell">{children}</main>
    </div>
  );
}

function NavItem({ to, icon, label }: { to: string; icon: React.ReactNode; label: string }) {
  return (
    <NavLink to={to} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
      {icon}
      {label}
    </NavLink>
  );
}

function DiscoverPage() {
  const { token } = useAuth();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [query, setQuery] = useState('spring boot');
  const [submittedQuery, setSubmittedQuery] = useState('spring boot');
  const [importedBooks, setImportedBooks] = useState<Record<string, Book>>({});

  const searchQuery = useQuery({
    queryKey: ['external-books', submittedQuery],
    queryFn: () => api.searchExternalBooks(submittedQuery),
    enabled: submittedQuery.trim().length > 1
  });

  const importMutation = useMutation({
    mutationFn: (book: ExternalBook) => api.importBook(token!, book),
    onSuccess: (book) => {
      setImportedBooks((current) => ({ ...current, [book.externalId ?? String(book.id)]: book }));
      queryClient.invalidateQueries({ queryKey: ['books'] });
    }
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmittedQuery(query.trim());
  }

  return (
    <section className="page-stack">
      <PageHeader
        eyebrow="Discovery"
        title="Find books worth bringing onto your shelf."
        description="Search Open Library through your Spring Boot backend, then import selected results into ShelfAware."
      />

      <form className="search-row" onSubmit={submit}>
        <Search size={20} />
        <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search by title, author, or topic" />
        <button type="submit">Search</button>
      </form>

      <ErrorMessage error={searchQuery.error ?? importMutation.error} />

      {searchQuery.isLoading ? (
        <LoadingBlock label="Searching Open Library" />
      ) : (
        <div className="book-grid">
          {searchQuery.data?.books.map((book) => {
            const imported = importedBooks[book.externalId];
            return (
              <ExternalBookCard
                key={book.externalId}
                book={book}
                imported={imported}
                importing={importMutation.isPending}
                onImport={() => importMutation.mutate(book)}
                onOpen={() => imported && navigate(`/books/${imported.id}`)}
              />
            );
          })}
        </div>
      )}
    </section>
  );
}

function LibraryPage() {
  const { token } = useAuth();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [statusByBook, setStatusByBook] = useState<Record<number, ReadingStatus>>({});

  const booksQuery = useQuery({
    queryKey: ['books', query],
    queryFn: () => api.searchBooks(query)
  });

  const shelfMutation = useMutation({
    mutationFn: ({ book, status }: { book: Book; status: ReadingStatus }) =>
      api.updateShelfItem(token!, book.id, { status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['shelf'] });
      queryClient.invalidateQueries({ queryKey: ['insights'] });
    }
  });

  return (
    <section className="page-stack">
      <PageHeader
        eyebrow="Library"
        title="Your local catalog."
        description="Imported and manually created books live here before they become reviews, shelf items, and insights."
      />

      <div className="search-row">
        <Search size={20} />
        <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Filter local books" />
      </div>

      <ErrorMessage error={booksQuery.error ?? shelfMutation.error} />

      {booksQuery.isLoading ? (
        <LoadingBlock label="Loading catalog" />
      ) : (
        <div className="book-grid">
          {booksQuery.data?.content.map((book) => (
            <BookCard
              key={book.id}
              book={book}
              footer={
                <div className="card-actions">
                  <StatusSelect
                    value={statusByBook[book.id] ?? 'WANT_TO_READ'}
                    onChange={(status) => setStatusByBook((current) => ({ ...current, [book.id]: status }))}
                  />
                  <button
                    className="small-button"
                    onClick={() => shelfMutation.mutate({ book, status: statusByBook[book.id] ?? 'WANT_TO_READ' })}
                  >
                    <Plus size={16} />
                    Shelf
                  </button>
                </div>
              }
            />
          ))}
        </div>
      )}
    </section>
  );
}

function ShelfPage() {
  const { token } = useAuth();
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<ReadingStatus | ''>('');

  const shelfQuery = useQuery({
    queryKey: ['shelf', status],
    queryFn: () => api.getShelf(token!, status || undefined)
  });

  const updateMutation = useMutation({
    mutationFn: ({ item, nextStatus }: { item: ShelfItem; nextStatus: ReadingStatus }) =>
      api.updateShelfItem(token!, item.book.id, {
        status: nextStatus,
        privateNotes: item.privateNotes,
        startedOn: item.startedOn,
        finishedOn: item.finishedOn
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['shelf'] });
      queryClient.invalidateQueries({ queryKey: ['insights'] });
    }
  });

  return (
    <section className="page-stack">
      <PageHeader
        eyebrow="My shelf"
        title="Keep your reading state tidy."
        description="Move books between statuses and keep the collection honest as your reading changes."
      />

      <div className="toolbar">
        <button className={status === '' ? 'chip active' : 'chip'} onClick={() => setStatus('')}>All</button>
        {STATUS_OPTIONS.map((option) => (
          <button
            key={option.value}
            className={status === option.value ? 'chip active' : 'chip'}
            onClick={() => setStatus(option.value)}
          >
            {option.label}
          </button>
        ))}
      </div>

      <ErrorMessage error={shelfQuery.error ?? updateMutation.error} />

      {shelfQuery.isLoading ? (
        <LoadingBlock label="Loading shelf" />
      ) : shelfQuery.data?.length ? (
        <div className="shelf-list">
          {shelfQuery.data.map((item) => (
            <article key={item.id} className="shelf-row">
              <Cover book={item.book} />
              <div>
                <Link to={`/books/${item.book.id}`} className="row-title">{item.book.title}</Link>
                <p>{item.book.authors}</p>
                <span className="meta-pill">{statusLabel(item.status)}</span>
              </div>
              <StatusSelect value={item.status} onChange={(nextStatus) => updateMutation.mutate({ item, nextStatus })} />
            </article>
          ))}
        </div>
      ) : (
        <EmptyState title="No books here yet" text="Import a book from Discover or add one from Library." />
      )}
    </section>
  );
}

function InsightsPage() {
  const { token } = useAuth();
  const insightsQuery = useQuery({
    queryKey: ['insights'],
    queryFn: () => api.getInsights(token!)
  });

  const ratingData = useMemo(
    () =>
      Object.entries(insightsQuery.data?.ratingsDistribution ?? {}).map(([rating, count]) => ({
        rating: `${rating} star`,
        count
      })),
    [insightsQuery.data]
  );

  return (
    <section className="page-stack">
      <PageHeader
        eyebrow="Insights"
        title="A clearer view of your reading life."
        description="ShelfAware turns reviews and shelf actions into lightweight analytics."
      />

      <ErrorMessage error={insightsQuery.error} />

      {insightsQuery.isLoading ? (
        <LoadingBlock label="Calculating insights" />
      ) : insightsQuery.data ? (
        <>
          <div className="metric-grid">
            <Metric label="Shelf items" value={insightsQuery.data.totalShelfItems} />
            <Metric label="Currently reading" value={insightsQuery.data.readingCount} />
            <Metric label="Finished" value={insightsQuery.data.finishedCount} />
            <Metric label="Average rating" value={insightsQuery.data.averageRating.toFixed(1)} />
          </div>

          <section className="chart-panel">
            <div className="section-heading">
              <h2>Rating distribution</h2>
              <span>{insightsQuery.data.reviewCount} reviews</span>
            </div>
            <div className="chart-frame">
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={ratingData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="rating" />
                  <YAxis allowDecimals={false} />
                  <Tooltip cursor={{ fill: 'rgba(52, 211, 153, 0.12)' }} />
                  <Bar dataKey="count" fill="#0f766e" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </section>
        </>
      ) : null}
    </section>
  );
}

function BookDetailPage() {
  const { token } = useAuth();
  const { bookId } = useParams();
  const queryClient = useQueryClient();
  const id = Number(bookId);
  const [rating, setRating] = useState(5);
  const [body, setBody] = useState('');
  const [publicReview, setPublicReview] = useState(true);
  const [status, setStatus] = useState<ReadingStatus>('READING');

  const bookQuery = useQuery({
    queryKey: ['book', id],
    queryFn: () => api.getBook(id),
    enabled: Number.isFinite(id)
  });

  const reviewsQuery = useQuery({
    queryKey: ['reviews', id],
    queryFn: () => api.getReviews(id),
    enabled: Number.isFinite(id)
  });

  const reviewMutation = useMutation({
    mutationFn: () => api.reviewBook(token!, id, { rating, body, publicReview }),
    onSuccess: () => {
      setBody('');
      queryClient.invalidateQueries({ queryKey: ['reviews', id] });
      queryClient.invalidateQueries({ queryKey: ['insights'] });
    }
  });

  const shelfMutation = useMutation({
    mutationFn: () => api.updateShelfItem(token!, id, { status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['shelf'] });
      queryClient.invalidateQueries({ queryKey: ['insights'] });
    }
  });

  function submitReview(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    reviewMutation.mutate();
  }

  if (bookQuery.isLoading) {
    return <LoadingBlock label="Loading book" />;
  }

  if (!bookQuery.data) {
    return <EmptyState title="Book not found" text="The selected book could not be loaded." />;
  }

  const book = bookQuery.data;

  return (
    <section className="page-stack">
      <div className="detail-hero">
        <Cover book={book} large />
        <div>
          <p className="eyebrow">{book.categories ?? 'Book detail'}</p>
          <h1>{book.title}</h1>
          <p>{book.authors}</p>
          <div className="detail-meta">
            {book.publisher && <span>{book.publisher}</span>}
            {book.publishedDate && <span>{new Date(book.publishedDate).getFullYear()}</span>}
            {book.pageCount && <span>{book.pageCount} pages</span>}
          </div>
          {book.description && <p className="description">{book.description}</p>}
          <div className="inline-controls">
            <StatusSelect value={status} onChange={setStatus} />
            <button className="small-button" onClick={() => shelfMutation.mutate()}>
              <Plus size={16} />
              Add to shelf
            </button>
          </div>
        </div>
      </div>

      <div className="two-column">
        <section className="panel">
          <div className="section-heading">
            <h2>Your review</h2>
            <Sparkles size={18} />
          </div>
          <form className="form-stack" onSubmit={submitReview}>
            <label>
              Rating
              <input type="number" min={1} max={5} value={rating} onChange={(event) => setRating(Number(event.target.value))} />
            </label>
            <label>
              Review
              <textarea value={body} onChange={(event) => setBody(event.target.value)} required rows={7} />
            </label>
            <label className="check-row">
              <input type="checkbox" checked={publicReview} onChange={(event) => setPublicReview(event.target.checked)} />
              Public review
            </label>
            <ErrorMessage error={reviewMutation.error ?? shelfMutation.error} />
            <button className="primary-action" disabled={reviewMutation.isPending}>
              Save review
            </button>
          </form>
        </section>

        <section className="panel">
          <div className="section-heading">
            <h2>Public reviews</h2>
            <span>{reviewsQuery.data?.length ?? 0}</span>
          </div>
          <div className="review-list">
            {reviewsQuery.data?.length ? (
              reviewsQuery.data.map((review) => <ReviewCard key={review.id} review={review} />)
            ) : (
              <EmptyState title="No reviews yet" text="Your review can be the first public note for this book." compact />
            )}
          </div>
        </section>
      </div>
    </section>
  );
}

function ExternalBookCard({
  book,
  imported,
  importing,
  onImport,
  onOpen
}: {
  book: ExternalBook;
  imported?: Book;
  importing: boolean;
  onImport: () => void;
  onOpen: () => void;
}) {
  return (
    <article className="book-card">
      <Cover book={book} />
      <div className="book-card-body">
        <h3>{book.title}</h3>
        <p>{book.authors}</p>
        <div className="tag-row">
          {book.publishedDate && <span>{new Date(book.publishedDate).getFullYear()}</span>}
          {book.categories && <span>{book.categories}</span>}
        </div>
      </div>
      <button className={imported ? 'small-button success' : 'small-button'} onClick={imported ? onOpen : onImport} disabled={importing}>
        {imported ? <Check size={16} /> : <Plus size={16} />}
        {imported ? 'Open' : 'Import'}
      </button>
    </article>
  );
}

function BookCard({ book, footer }: { book: Book; footer?: React.ReactNode }) {
  return (
    <article className="book-card">
      <Link to={`/books/${book.id}`}>
        <Cover book={book} />
      </Link>
      <div className="book-card-body">
        <Link to={`/books/${book.id}`} className="book-title">{book.title}</Link>
        <p>{book.authors}</p>
        <div className="tag-row">
          {book.categories && <span>{book.categories}</span>}
          {book.pageCount && <span>{book.pageCount} pages</span>}
        </div>
      </div>
      {footer}
    </article>
  );
}

function Cover({ book, large = false }: { book: Pick<Book, 'title' | 'coverImageUrl'> | ExternalBook; large?: boolean }) {
  return (
    <div className={large ? 'cover large' : 'cover'}>
      {book.coverImageUrl ? <img src={book.coverImageUrl} alt="" /> : <BookOpen size={large ? 44 : 30} />}
    </div>
  );
}

function StatusSelect({ value, onChange }: { value: ReadingStatus; onChange: (status: ReadingStatus) => void }) {
  return (
    <select value={value} onChange={(event) => onChange(event.target.value as ReadingStatus)}>
      {STATUS_OPTIONS.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
}

function PageHeader({ eyebrow, title, description }: { eyebrow: string; title: string; description: string }) {
  return (
    <header className="page-header">
      <p className="eyebrow">{eyebrow}</p>
      <h1>{title}</h1>
      <p>{description}</p>
    </header>
  );
}

function Metric({ label, value }: { label: string; value: string | number }) {
  return (
    <article className="metric-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function ReviewCard({ review }: { review: Review }) {
  return (
    <article className="review-card">
      <div className="review-rating">
        {Array.from({ length: review.rating }).map((_, index) => (
          <Star key={index} size={15} fill="currentColor" />
        ))}
      </div>
      <p>{review.body}</p>
      <span>@{review.username}</span>
    </article>
  );
}

function LoadingBlock({ label }: { label: string }) {
  return (
    <div className="loading-block">
      <Loader2 className="spin" size={24} />
      <span>{label}</span>
    </div>
  );
}

function EmptyState({ title, text, compact = false }: { title: string; text: string; compact?: boolean }) {
  return (
    <div className={compact ? 'empty-state compact-empty' : 'empty-state'}>
      <BookOpen size={compact ? 24 : 34} />
      <h3>{title}</h3>
      <p>{text}</p>
    </div>
  );
}

function ErrorMessage({ error }: { error: unknown }) {
  if (!error) {
    return null;
  }

  const message = error instanceof ApiError ? error.message : error instanceof Error ? error.message : 'Something went wrong';
  return <div className="error-box">{message}</div>;
}

function statusLabel(status: ReadingStatus) {
  return STATUS_OPTIONS.find((option) => option.value === status)?.label ?? status;
}
