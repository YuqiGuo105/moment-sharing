// src/App.test.js
import { render, screen } from '@testing-library/react';
import App from './App';

test('shows the login screen by default', async () => {
  render(<App />);

  /**
   * The component sets `loading` true, registers onAuthStateChanged,
   * then flips `loading` false inside the callback we mocked.
   * `findByText` waits for that state change before asserting.
   *
   * ▸ Adjust the regex below if your heading/button reads
   *   “Sign in”, “Welcome”, etc.
   */
  const loginHeading = await screen.findByText(/login/i);

  expect(loginHeading).toBeInTheDocument();
});
