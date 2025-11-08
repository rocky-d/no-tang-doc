import { render } from '@testing-library/react';
import React from 'react';
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';

describe('Avatar', () => {
  it('renders root and either image or fallback', () => {
    const { container } = render(
      <Avatar>
        <AvatarImage alt="user" />
        <AvatarFallback>U</AvatarFallback>
      </Avatar>
    );
    const root = container.querySelector('[data-slot="avatar"]');
    const img = container.querySelector('[data-slot="avatar-image"]');
    const fallback = container.querySelector('[data-slot="avatar-fallback"]');
    expect(root).toBeInTheDocument();
    expect(img || fallback).toBeTruthy();
  });
});
