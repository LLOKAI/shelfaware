import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Loader2 } from 'lucide-react';
import { api, ApiError } from './api/client';
import { useAuth } from './state/AuthContext';

export function InsightsPage() {
  const { token } = useAuth();
  const insightsQuery = useQuery({ queryKey: ['insights'], queryFn: () => api.getInsights(token!) });
  const ratingData = useMemo(() => Object.entries(insightsQuery.data?.ratingsDistribution ?? {}).map(([rating, count]) => ({ rating: `${rating} star`, count })), [insightsQuery.data]);
  const monthlyData = useMemo(() => Object.entries(insightsQuery.data?.monthlyPages ?? {}).map(([month, pages]) => ({ month, pages })), [insightsQuery.data]);

  return <section className="page-stack">
    <header className="page-header"><p className="eyebrow">Insights</p><h1>A clearer view of your reading life.</h1><p>ShelfAware turns reviews and reading sessions into meaningful analytics.</p></header>
    {insightsQuery.error && <div className="error-box" role="alert">{insightsQuery.error instanceof ApiError ? insightsQuery.error.message : 'Could not load insights'}</div>}
    {insightsQuery.isLoading ? <div className="loading-block"><Loader2 className="spin" size={24} />Calculating insights</div> : insightsQuery.data && <>
      <div className="metric-grid">
        <Metric label="Shelf items" value={insightsQuery.data.totalShelfItems} />
        <Metric label="Currently reading" value={insightsQuery.data.readingCount} />
        <Metric label="Finished" value={insightsQuery.data.finishedCount} />
        <Metric label="Favorites" value={insightsQuery.data.favoriteCount} />
        <Metric label="Pages this year" value={insightsQuery.data.pagesRead.toLocaleString()} />
        <Metric label="Reading streak" value={`${insightsQuery.data.currentStreak} days`} />
      </div>
      <Chart title="Pages by month" detail={`${insightsQuery.data.averagePagesPerSession.toFixed(1)} pages / session`} data={monthlyData} dataKey="month" barKey="pages" color="#b45309" />
      <Chart title="Rating distribution" detail={`${insightsQuery.data.reviewCount} reviews · ${insightsQuery.data.averageRating.toFixed(1)} average`} data={ratingData} dataKey="rating" barKey="count" color="#0f766e" />
    </>}
  </section>;
}

function Metric({ label, value }: { label: string; value: string | number }) {
  return <article className="metric-card"><span>{label}</span><strong>{value}</strong></article>;
}

function Chart({ title, detail, data, dataKey, barKey, color }: { title: string; detail: string; data: Array<Record<string, string | number>>; dataKey: string; barKey: string; color: string }) {
  return <section className="chart-panel"><div className="section-heading"><h2>{title}</h2><span>{detail}</span></div><div className="chart-frame"><ResponsiveContainer width="100%" height={280}><BarChart data={data}><CartesianGrid strokeDasharray="3 3" vertical={false} /><XAxis dataKey={dataKey} /><YAxis allowDecimals={false} /><Tooltip cursor={{ fill: 'rgba(52, 211, 153, 0.12)' }} /><Bar dataKey={barKey} fill={color} radius={[6, 6, 0, 0]} /></BarChart></ResponsiveContainer></div></section>;
}
