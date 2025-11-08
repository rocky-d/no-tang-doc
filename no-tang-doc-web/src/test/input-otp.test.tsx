import { render } from '@testing-library/react';
import React from 'react';
import { InputOTP, InputOTPGroup, InputOTPSlot, InputOTPSeparator } from '@/components/ui/input-otp';

describe('InputOTP', () => {
  it('renders group, slots and separator', () => {
    const { container } = render(
      <InputOTP maxLength={4} value="12" onChange={() => {}}>
        <InputOTPGroup>
          <InputOTPSlot index={0} />
          <InputOTPSlot index={1} />
        </InputOTPGroup>
        <InputOTPSeparator />
      </InputOTP>
    );
    expect(container.querySelector('[data-slot="input-otp-group"]')).toBeInTheDocument();
    expect(container.querySelectorAll('[data-slot="input-otp-slot"]').length).toBe(2);
    expect(container.querySelector('[data-slot="input-otp-separator"]')).toBeInTheDocument();
  });
});

