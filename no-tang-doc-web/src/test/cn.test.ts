import { describe, it, expect } from 'vitest';
import { cn } from '@/components/ui/utils';

describe('cn', () => {
  it('merges class names and resolves tailwind conflicts', () => {
    const result = cn('p-2', 'text-sm', 'p-4', { hidden: false, block: true });
    expect(result).toContain('text-sm');
    expect(result).toContain('block');
    // tailwind-merge should keep the last padding utility
    expect(result).toMatch(/\bp-4\b/);
    expect(result).not.toMatch(/\bp-2\b/);
  });
});

