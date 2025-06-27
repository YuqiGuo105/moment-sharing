import { render, screen } from '@testing-library/react';
import App from './App';

test('shows login screen by default', () => {
  render(<App />);
  const heading = screen.getByRole('heading', { name: /login/i });
  expect(heading).toBeInTheDocument();
});
