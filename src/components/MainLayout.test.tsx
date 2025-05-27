import { render, screen } from '@testing-library/react';
import MainLayout from './MainLayout';
import { describe, it, expect } from 'vitest';

describe('MainLayout', () => {
  it('renders children correctly', () => {
    render(
      <MainLayout>
        <div>Test Child Content</div>
      </MainLayout>
    );
    expect(screen.getByText('Test Child Content')).toBeInTheDocument();
  });

  it('renders sidebar navigation elements', () => {
    render(
      <MainLayout>
        <div>Test Content</div>
      </MainLayout>
    );

    // Check for the main navigation title/link
    expect(screen.getByText('Packing List')).toBeInTheDocument();

    // Check for the "Future Features" heading or a specific placeholder
    expect(screen.getByText('Future Features')).toBeInTheDocument(); // Or check for one of the placeholders by name
    expect(screen.getByText('Campers Management')).toBeInTheDocument();
    expect(screen.getByText('Event Schedule')).toBeInTheDocument();
    // Optionally, check for "Group Chat" if it's consistently present
    // expect(screen.getByText('Group Chat')).toBeInTheDocument(); 
  });
});
