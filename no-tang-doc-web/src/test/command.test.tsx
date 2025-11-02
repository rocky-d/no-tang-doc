import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import {
    CommandDialog,
    CommandInput,
    CommandList,
    CommandGroup,
    CommandItem,
    CommandEmpty,
} from '@/components/ui/command'

describe('Command', () => {
    it('renders search input and list inside dialog', async () => {
        render(
            <CommandDialog open>
                <CommandInput placeholder="search" />
                <CommandList>
                    <CommandEmpty>Nothing</CommandEmpty>
                    <CommandGroup heading="Files">
                        <CommandItem>Item1</CommandItem>
                    </CommandGroup>
                </CommandList>
            </CommandDialog>
        )

        // 等 portal 真正插到 body 里
        await waitFor(() => {
            // 有的版本可能没有 cmdk-input-wrapper，那就退一步找 input
            const inputWrapper =
                document.querySelector('[cmdk-input-wrapper]') ??
                document.querySelector('input[cmdk-input]')?.parentElement

            expect(inputWrapper).toBeInTheDocument()
            expect(document.querySelector('[cmdk-list]')).toBeInTheDocument()
            expect(document.querySelector('[cmdk-item]')).toBeInTheDocument()
        })
    })
})
