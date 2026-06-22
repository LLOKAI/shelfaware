import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { BookOpen, CalendarDays, Flame, Loader2, Pencil, RotateCcw, Target, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import { api, ApiError } from './api/client';
import { useAuth } from './state/AuthContext';
import { useToast } from './state/ToastContext';
import type { ShelfItem } from './types';

const today = () => new Date().toLocaleDateString('en-CA');

export function JourneyPage() {
  const { token, user } = useAuth();
  const { notify } = useToast();
  const queryClient = useQueryClient();
  const year = new Date().getFullYear();
  const [selectedBook, setSelectedBook] = useState<ShelfItem | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [readOn, setReadOn] = useState(today);
  const [editingGoal, setEditingGoal] = useState(false);
  const [targetBooks, setTargetBooks] = useState('');
  const [targetPages, setTargetPages] = useState('');

  const journeyQuery = useQuery({
    queryKey: ['journey', year],
    queryFn: () => api.getJourney(token!, year)
  });

  useEffect(() => {
    if (journeyQuery.data) {
      setTargetBooks(journeyQuery.data.goal.targetBooks?.toString() ?? '');
      setTargetPages(journeyQuery.data.goal.targetPages?.toString() ?? '');
    }
  }, [journeyQuery.data]);

  const progressMutation = useMutation({
    mutationFn: () => api.updateProgress(token!, selectedBook!.book.id, currentPage, readOn),
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ['journey'] });
      queryClient.invalidateQueries({ queryKey: ['shelf'] });
      queryClient.invalidateQueries({ queryKey: ['insights'] });
      notify(result.milestone ? `${result.milestone}. Beautiful work.` : `${result.session.pagesRead} pages logged.`);
      setSelectedBook(null);
    },
    onError: (error) => notify(message(error), 'error')
  });

  const goalMutation = useMutation({
    mutationFn: () => api.saveReadingGoal(
      token!, year, targetBooks ? Number(targetBooks) : null, targetPages ? Number(targetPages) : null
    ),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['journey'] });
      notify(`${year} reading goal saved.`);
      setEditingGoal(false);
    },
    onError: (error) => notify(message(error), 'error')
  });

  const undoMutation = useMutation({
    mutationFn: (sessionId: number) => api.undoReadingSession(token!, sessionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['journey'] });
      queryClient.invalidateQueries({ queryKey: ['shelf'] });
      queryClient.invalidateQueries({ queryKey: ['insights'] });
      notify('Reading update undone.');
    },
    onError: (error) => notify(message(error), 'error')
  });

  const undoableIds = useMemo(() => {
    const books = new Set<number>();
    return new Set(journeyQuery.data?.recentSessions.filter((session) => {
      if (books.has(session.bookId)) return false;
      books.add(session.bookId);
      return true;
    }).map((session) => session.id));
  }, [journeyQuery.data]);

  function openProgress(item: ShelfItem) {
    setSelectedBook(item);
    setCurrentPage(item.currentPage + 1);
    setReadOn(today());
  }

  if (journeyQuery.isLoading) return <Loading label="Opening your reading journey" />;
  if (journeyQuery.error || !journeyQuery.data) return <ErrorBox error={journeyQuery.error} />;
  const journey = journeyQuery.data;

  return (
    <section className="page-stack journey-page">
      <header className="journey-hero">
        <div>
          <p className="eyebrow">Your reading journey</p>
          <h1>Welcome back, {user?.displayName}.</h1>
          <p>Small reading sessions become a story worth seeing.</p>
        </div>
        <div className="streak-card">
          <Flame size={26} />
          <strong>{journey.streak.currentDays} day{journey.streak.currentDays === 1 ? '' : 's'}</strong>
          <span>Current streak · best {journey.streak.longestDays}</span>
        </div>
      </header>

      <section className="goal-panel">
        <div className="section-heading">
          <div><p className="eyebrow">{year} goals</p><h2>Your year in motion</h2></div>
          <button className="quiet-button" onClick={() => setEditingGoal((value) => !value)}><Pencil size={16} /> Edit goals</button>
        </div>
        {editingGoal ? (
          <form className="goal-form" onSubmit={(event) => { event.preventDefault(); goalMutation.mutate(); }}>
            <label>Books<input type="number" min="1" value={targetBooks} onChange={(event) => setTargetBooks(event.target.value)} placeholder="12" /></label>
            <label>Pages<input type="number" min="1" value={targetPages} onChange={(event) => setTargetPages(event.target.value)} placeholder="5000" /></label>
            <button className="small-button" disabled={goalMutation.isPending || (!targetBooks && !targetPages)}>Save goals</button>
          </form>
        ) : journey.goal.targetBooks || journey.goal.targetPages ? (
          <div className="goal-grid">
            {journey.goal.targetBooks && <GoalRing label="Books finished" value={journey.goal.completedBooks} target={journey.goal.targetBooks} percent={journey.goal.booksProgress} projected={journey.goal.projectedBooks} />}
            {journey.goal.targetPages && <GoalRing label="Pages read" value={journey.goal.pagesRead} target={journey.goal.targetPages} percent={journey.goal.pagesProgress} projected={journey.goal.projectedPages} />}
          </div>
        ) : (
          <button className="empty-goal" onClick={() => setEditingGoal(true)}><Target size={28} /><strong>Set a gentle goal for {year}</strong><span>Choose books, pages, or both.</span></button>
        )}
      </section>

      <div className="journey-layout">
        <section className="panel">
          <div className="section-heading"><h2>Continue reading</h2><span>{journey.activeBooks.length} active</span></div>
          {journey.activeBooks.length ? <div className="continue-list">{journey.activeBooks.map((item) => {
            const percent = item.book.pageCount ? Math.round(item.currentPage / item.book.pageCount * 100) : null;
            return <article className="continue-card" key={item.id}>
              <MiniCover item={item} />
              <div><Link className="row-title" to={`/books/${item.book.id}`}>{item.book.title}</Link><p>{item.book.authors}</p>
                {percent !== null && <><div className="progress-track"><span style={{ width: `${percent}%` }} /></div><small>{item.currentPage} of {item.book.pageCount} pages · {percent}%</small></>}
                {percent === null && <small>{item.currentPage} pages logged</small>}
              </div>
              <button className="small-button" onClick={() => openProgress(item)}>Update</button>
            </article>;
          })}</div> : <Empty title="Nothing in progress" text="Move a book to Reading from your shelf, then log a few pages." />}
        </section>

        <section className="panel">
          <div className="section-heading"><h2>Recent activity</h2><CalendarDays size={18} /></div>
          {journey.recentSessions.length ? <div className="activity-list">{journey.recentSessions.map((session) => <article key={session.id} className="activity-row">
            <div><strong>{session.bookTitle}</strong><span>{session.pagesRead} pages · {formatDate(session.readOn)}</span></div>
            {undoableIds.has(session.id) && <button className="icon-button" title="Undo latest update" aria-label={`Undo latest progress for ${session.bookTitle}`} onClick={() => undoMutation.mutate(session.id)}><RotateCcw size={16} /></button>}
          </article>)}</div> : <Empty title="Your history starts here" text="Reading sessions will collect here as you update progress." />}
        </section>
      </div>

      {selectedBook && <div className="dialog-backdrop" role="presentation" onMouseDown={() => setSelectedBook(null)}>
        <section className="progress-dialog" role="dialog" aria-modal="true" aria-labelledby="progress-title" onMouseDown={(event) => event.stopPropagation()}>
          <button className="dialog-close" onClick={() => setSelectedBook(null)} aria-label="Close"><X size={20} /></button>
          <p className="eyebrow">Quick session</p><h2 id="progress-title">Where are you in {selectedBook.book.title}?</h2>
          <p>Currently on page {selectedBook.currentPage}{selectedBook.book.pageCount ? ` of ${selectedBook.book.pageCount}` : ''}.</p>
          <form className="form-stack" onSubmit={(event: FormEvent) => { event.preventDefault(); progressMutation.mutate(); }}>
            <label>New current page<input autoFocus type="number" min={selectedBook.currentPage + 1} max={selectedBook.book.pageCount ?? undefined} value={currentPage} onChange={(event) => setCurrentPage(Number(event.target.value))} required /></label>
            <label>Reading date<input type="date" value={readOn} onChange={(event) => setReadOn(event.target.value)} required /></label>
            <button className="primary-action" disabled={progressMutation.isPending}>{progressMutation.isPending && <Loader2 className="spin" size={18} />}Log progress</button>
          </form>
        </section>
      </div>}
    </section>
  );
}

function GoalRing({ label, value, target, percent, projected }: { label: string; value: number; target: number; percent: number; projected: number }) {
  return <article className="goal-card"><div className="goal-ring" style={{ '--progress': `${percent * 3.6}deg` } as React.CSSProperties}><div><strong>{Math.round(percent)}%</strong><span>{value.toLocaleString()} / {target.toLocaleString()}</span></div></div><div><strong>{label}</strong><span>At this pace: {projected.toLocaleString()}</span></div></article>;
}

function MiniCover({ item }: { item: ShelfItem }) { return <div className="mini-cover">{item.book.coverImageUrl ? <img src={item.book.coverImageUrl} alt="" /> : <BookOpen size={22} />}</div>; }
function Empty({ title, text }: { title: string; text: string }) { return <div className="journey-empty"><BookOpen size={24} /><strong>{title}</strong><span>{text}</span></div>; }
function Loading({ label }: { label: string }) { return <div className="loading-block"><Loader2 className="spin" size={24} />{label}</div>; }
function ErrorBox({ error }: { error: unknown }) { return <div className="error-box" role="alert">{message(error)}</div>; }
function message(error: unknown) { return error instanceof ApiError ? error.message : error instanceof Error ? error.message : 'Something went wrong'; }
function formatDate(value: string) { return new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric' }).format(new Date(`${value}T12:00:00`)); }
