import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { App } from './App';
import { api } from './api/client';

const saveAuth = vi.fn();

vi.mock('./state/AuthContext', () => ({
  useAuth: () => ({ isAuthenticated: false, saveAuth })
}));

vi.mock('./api/client', async (importOriginal) => {
  const original = await importOriginal<typeof import('./api/client')>();
  return { ...original, api: { ...original.api, demo: vi.fn() } };
});

describe('demo entry', () => {
  beforeEach(() => vi.clearAllMocks());

  it('opens a populated demo session without credentials', async () => {
    const response = {
      tokenType: 'Bearer' as const,
      accessToken: 'demo-token',
      expiresAt: '2099-01-01T00:00:00Z',
      user: { id: 10, displayName: 'Alex Morgan', email: 'demo@shelfaware.app', username: 'demo_test', roles: ['USER' as const] }
    };
    vi.mocked(api.demo).mockResolvedValue(response);
    const client = new QueryClient({ defaultOptions: { mutations: { retry: false } } });

    render(<QueryClientProvider client={client}><MemoryRouter><App /></MemoryRouter></QueryClientProvider>);
    fireEvent.click(screen.getByRole('button', { name: 'Explore the live demo' }));

    await waitFor(() => expect(api.demo).toHaveBeenCalledOnce());
    expect(saveAuth).toHaveBeenCalledWith(response);
  });
});
