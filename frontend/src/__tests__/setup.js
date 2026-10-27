// Runs before every test file: adds matchers like toBeInTheDocument()
// and cleans up the rendered components after each test.
import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

afterEach(() => {
  cleanup();
});
