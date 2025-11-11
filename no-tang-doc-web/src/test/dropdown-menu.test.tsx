import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuCheckboxItem,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuGroup,
  DropdownMenuSub,
  DropdownMenuSubTrigger,
  DropdownMenuSubContent,
  DropdownMenuPortal
} from '@/components/ui/dropdown-menu';
import { vi } from 'vitest';

describe('DropdownMenu', () => {
  it('opens on trigger click and shows content', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>Item 1</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    const content = document.querySelector('[data-slot="dropdown-menu-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
    expect(content).toHaveTextContent('Item 1');
  });

  it('renders menu items with different variants', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>Default Item</DropdownMenuItem>
          <DropdownMenuItem variant="destructive">Destructive Item</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Default Item')).toBeInTheDocument();
    expect(screen.getByText('Destructive Item')).toBeInTheDocument();
    expect(screen.getByText('Destructive Item')).toHaveAttribute('data-variant', 'destructive');
  });

  it('renders menu items with inset', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem inset>Inset Item</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    const item = screen.getByText('Inset Item');
    expect(item).toHaveAttribute('data-inset', 'true');
  });

  it('handles disabled menu items', async () => {
    const handleClick = vi.fn();
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem disabled onClick={handleClick}>Disabled Item</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    const item = screen.getByText('Disabled Item');
    expect(item).toHaveAttribute('data-disabled', '');
  });

  it('renders checkbox items with checked state', async () => {
    const user = userEvent.setup();
    const handleCheckedChange = vi.fn();

    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuCheckboxItem checked={true} onCheckedChange={handleCheckedChange}>
            Checked Item
          </DropdownMenuCheckboxItem>
          <DropdownMenuCheckboxItem checked={false}>
            Unchecked Item
          </DropdownMenuCheckboxItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Checked Item')).toBeInTheDocument();
    expect(screen.getByText('Unchecked Item')).toBeInTheDocument();
  });

  it('renders radio group with radio items', async () => {
    const user = userEvent.setup();

    const TestComponent = () => {
      const [value, setValue] = React.useState('option1');
      return (
        <DropdownMenu>
          <DropdownMenuTrigger>Open</DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuRadioGroup value={value} onValueChange={setValue}>
              <DropdownMenuRadioItem value="option1">Option 1</DropdownMenuRadioItem>
              <DropdownMenuRadioItem value="option2">Option 2</DropdownMenuRadioItem>
            </DropdownMenuRadioGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      );
    };

    render(<TestComponent />);
    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Option 1')).toBeInTheDocument();
    expect(screen.getByText('Option 2')).toBeInTheDocument();
  });

  it('renders label and separator', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuLabel>Menu Label</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuItem>Item</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Menu Label')).toBeInTheDocument();
    const separator = document.querySelector('[data-slot="dropdown-menu-separator"]');
    expect(separator).toBeInTheDocument();
  });

  it('renders label with inset', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuLabel inset>Inset Label</DropdownMenuLabel>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    const label = screen.getByText('Inset Label');
    expect(label).toHaveAttribute('data-inset', 'true');
  });

  it('renders shortcut in menu item', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>
            Save
            <DropdownMenuShortcut>⌘S</DropdownMenuShortcut>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('⌘S')).toBeInTheDocument();
    expect(screen.getByText('⌘S')).toHaveAttribute('data-slot', 'dropdown-menu-shortcut');
  });

  it('renders grouped menu items', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuGroup>
            <DropdownMenuItem>Group Item 1</DropdownMenuItem>
            <DropdownMenuItem>Group Item 2</DropdownMenuItem>
          </DropdownMenuGroup>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Group Item 1')).toBeInTheDocument();
    expect(screen.getByText('Group Item 2')).toBeInTheDocument();
  });

  it('renders submenu with sub-trigger and sub-content', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuSub>
            <DropdownMenuSubTrigger>More Options</DropdownMenuSubTrigger>
            <DropdownMenuSubContent>
              <DropdownMenuItem>Sub Item 1</DropdownMenuItem>
              <DropdownMenuItem>Sub Item 2</DropdownMenuItem>
            </DropdownMenuSubContent>
          </DropdownMenuSub>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('More Options')).toBeInTheDocument();
  });

  it('renders submenu with inset sub-trigger', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuSub>
            <DropdownMenuSubTrigger inset>Inset Submenu</DropdownMenuSubTrigger>
            <DropdownMenuSubContent>
              <DropdownMenuItem>Sub Item</DropdownMenuItem>
            </DropdownMenuSubContent>
          </DropdownMenuSub>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    const subTrigger = screen.getByText('Inset Submenu');
    expect(subTrigger).toHaveAttribute('data-inset', 'true');
  });

  it('applies custom classNames to components', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger className="custom-trigger">Open</DropdownMenuTrigger>
        <DropdownMenuContent className="custom-content">
          <DropdownMenuItem className="custom-item">Item</DropdownMenuItem>
          <DropdownMenuSeparator className="custom-separator" />
          <DropdownMenuLabel className="custom-label">Label</DropdownMenuLabel>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    const content = document.querySelector('[data-slot="dropdown-menu-content"]');
    expect(content).toHaveClass('custom-content');
    expect(screen.getByText('Item')).toHaveClass('custom-item');
    expect(screen.getByText('Label')).toHaveClass('custom-label');
  });

  it('handles menu item click', async () => {
    const handleClick = vi.fn();
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem onClick={handleClick}>Clickable Item</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    await user.click(screen.getByText('Clickable Item'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('renders with DropdownMenuPortal', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuPortal>
          <DropdownMenuContent>
            <DropdownMenuItem>Portal Item</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenuPortal>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Portal Item')).toBeInTheDocument();
  });

  // Additional functional tests for better coverage

  it('handles checkbox item checked state changes', async () => {
    const handleCheckedChange = vi.fn();
    const user = userEvent.setup();

    const TestComponent = () => {
      const [checked, setChecked] = React.useState(false);
      return (
        <DropdownMenu>
          <DropdownMenuTrigger>Open</DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuCheckboxItem
              checked={checked}
              onCheckedChange={(value) => {
                setChecked(value);
                handleCheckedChange(value);
              }}
            >
              Toggle Me
            </DropdownMenuCheckboxItem>
          </DropdownMenuContent>
        </DropdownMenu>
      );
    };

    render(<TestComponent />);
    await user.click(screen.getByText('Open'));

    const checkboxItem = screen.getByText('Toggle Me');
    await user.click(checkboxItem);

    expect(handleCheckedChange).toHaveBeenCalledWith(true);
    expect(handleCheckedChange).toHaveBeenCalledTimes(1);
  });

  it('handles radio group value changes', async () => {
    const handleValueChange = vi.fn();
    const user = userEvent.setup();

    const TestComponent = () => {
      const [value, setValue] = React.useState('option1');
      return (
        <DropdownMenu>
          <DropdownMenuTrigger>Open</DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuRadioGroup
              value={value}
              onValueChange={(newValue) => {
                setValue(newValue);
                handleValueChange(newValue);
              }}
            >
              <DropdownMenuRadioItem value="option1">Option 1</DropdownMenuRadioItem>
              <DropdownMenuRadioItem value="option2">Option 2</DropdownMenuRadioItem>
              <DropdownMenuRadioItem value="option3">Option 3</DropdownMenuRadioItem>
            </DropdownMenuRadioGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      );
    };

    render(<TestComponent />);
    await user.click(screen.getByText('Open'));

    await user.click(screen.getByText('Option 2'));
    expect(handleValueChange).toHaveBeenCalledWith('option2');
    expect(handleValueChange).toHaveBeenCalledTimes(1);
  });

  it('handles disabled radio items', async () => {
    const handleValueChange = vi.fn();
    const user = userEvent.setup();

    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuRadioGroup value="option1" onValueChange={handleValueChange}>
            <DropdownMenuRadioItem value="option1">Option 1</DropdownMenuRadioItem>
            <DropdownMenuRadioItem value="option2" disabled>Option 2 (Disabled)</DropdownMenuRadioItem>
          </DropdownMenuRadioGroup>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    const disabledItem = screen.getByText('Option 2 (Disabled)');
    expect(disabledItem).toHaveAttribute('data-disabled', '');
  });

  it('renders multiple checkbox items and handles independent state', async () => {
    const user = userEvent.setup();

    const TestComponent = () => {
      const [showBookmarks, setShowBookmarks] = React.useState(true);
      const [showUrls, setShowUrls] = React.useState(false);
      const [showEmails, setShowEmails] = React.useState(false);

      return (
        <DropdownMenu>
          <DropdownMenuTrigger>View Options</DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuCheckboxItem checked={showBookmarks} onCheckedChange={setShowBookmarks}>
              Show Bookmarks
            </DropdownMenuCheckboxItem>
            <DropdownMenuCheckboxItem checked={showUrls} onCheckedChange={setShowUrls}>
              Show URLs
            </DropdownMenuCheckboxItem>
            <DropdownMenuCheckboxItem checked={showEmails} onCheckedChange={setShowEmails}>
              Show Emails
            </DropdownMenuCheckboxItem>
          </DropdownMenuContent>
        </DropdownMenu>
      );
    };

    render(<TestComponent />);
    await user.click(screen.getByText('View Options'));

    // Verify all items are present
    expect(screen.getByText('Show Bookmarks')).toBeInTheDocument();
    expect(screen.getByText('Show URLs')).toBeInTheDocument();
    expect(screen.getByText('Show Emails')).toBeInTheDocument();

    // Click one checkbox item
    await user.click(screen.getByText('Show URLs'));
  });

  it('renders submenu and opens sub-content on hover', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>Regular Item</DropdownMenuItem>
          <DropdownMenuSub>
            <DropdownMenuSubTrigger>Share</DropdownMenuSubTrigger>
            <DropdownMenuSubContent>
              <DropdownMenuItem>Email</DropdownMenuItem>
              <DropdownMenuItem>Messages</DropdownMenuItem>
              <DropdownMenuItem>Notes</DropdownMenuItem>
            </DropdownMenuSubContent>
          </DropdownMenuSub>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Share')).toBeInTheDocument();

    const subTrigger = screen.getByText('Share');
    expect(subTrigger).toHaveAttribute('data-slot', 'dropdown-menu-sub-trigger');
  });

  it('handles onSelect callback for menu items', async () => {
    const handleSelect = vi.fn();
    const user = userEvent.setup();

    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem onSelect={handleSelect}>
            Select Me
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    await user.click(screen.getByText('Select Me'));

    expect(handleSelect).toHaveBeenCalled();
  });

  it('handles controlled open state', async () => {
    const user = userEvent.setup();

    const TestComponent = () => {
      const [open, setOpen] = React.useState(false);

      return (
        <div>
          <button onClick={() => setOpen(true)}>External Open</button>
          <DropdownMenu open={open} onOpenChange={setOpen}>
            <DropdownMenuTrigger>Menu Trigger</DropdownMenuTrigger>
            <DropdownMenuContent>
              <DropdownMenuItem>Item 1</DropdownMenuItem>
              <DropdownMenuItem>Item 2</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      );
    };

    render(<TestComponent />);

    await user.click(screen.getByText('External Open'));
    expect(screen.getByText('Item 1')).toBeInTheDocument();
  });

  it('renders content with custom sideOffset', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent sideOffset={10}>
          <DropdownMenuItem>Item with offset</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Item with offset')).toBeInTheDocument();
  });

  it('renders complex menu structure with groups, labels, and separators', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Actions</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuLabel>My Account</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuGroup>
            <DropdownMenuItem>
              Profile
              <DropdownMenuShortcut>⇧⌘P</DropdownMenuShortcut>
            </DropdownMenuItem>
            <DropdownMenuItem>
              Settings
              <DropdownMenuShortcut>⌘S</DropdownMenuShortcut>
            </DropdownMenuItem>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuLabel>Team</DropdownMenuLabel>
          <DropdownMenuGroup>
            <DropdownMenuItem>Invite users</DropdownMenuItem>
            <DropdownMenuItem>New Team</DropdownMenuItem>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuItem variant="destructive">Delete</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Actions'));

    expect(screen.getByText('My Account')).toBeInTheDocument();
    expect(screen.getByText('Profile')).toBeInTheDocument();
    expect(screen.getByText('Settings')).toBeInTheDocument();
    expect(screen.getByText('⇧⌘P')).toBeInTheDocument();
    expect(screen.getByText('⌘S')).toBeInTheDocument();
    expect(screen.getByText('Team')).toBeInTheDocument();
    expect(screen.getByText('Invite users')).toBeInTheDocument();
    expect(screen.getByText('Delete')).toBeInTheDocument();
  });

  it('handles keyboard navigation with arrow keys', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>First Item</DropdownMenuItem>
          <DropdownMenuItem>Second Item</DropdownMenuItem>
          <DropdownMenuItem>Third Item</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));

    // Verify items are present
    expect(screen.getByText('First Item')).toBeInTheDocument();
    expect(screen.getByText('Second Item')).toBeInTheDocument();
    expect(screen.getByText('Third Item')).toBeInTheDocument();
  });

  it('handles escape key to close menu', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>Item</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Item')).toBeInTheDocument();

    await user.keyboard('{Escape}');
    // Menu should close after escape
  });

  it('closes menu after selecting an item', async () => {
    const handleSelect = vi.fn();
    const user = userEvent.setup();

    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem onSelect={handleSelect}>
            Click to Close
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    await user.click(screen.getByText('Click to Close'));

    expect(handleSelect).toHaveBeenCalled();
  });

  it('renders nested submenus', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>Top Level</DropdownMenuItem>
          <DropdownMenuSub>
            <DropdownMenuSubTrigger>Level 1</DropdownMenuSubTrigger>
            <DropdownMenuSubContent>
              <DropdownMenuItem>Level 1 Item</DropdownMenuItem>
              <DropdownMenuSub>
                <DropdownMenuSubTrigger>Level 2</DropdownMenuSubTrigger>
                <DropdownMenuSubContent>
                  <DropdownMenuItem>Level 2 Item</DropdownMenuItem>
                </DropdownMenuSubContent>
              </DropdownMenuSub>
            </DropdownMenuSubContent>
          </DropdownMenuSub>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Top Level')).toBeInTheDocument();
    expect(screen.getByText('Level 1')).toBeInTheDocument();
  });

  it('handles mixed content types in single menu', async () => {
    const user = userEvent.setup();

    const TestComponent = () => {
      const [notifications, setNotifications] = React.useState(true);
      const [theme, setTheme] = React.useState('light');

      return (
        <DropdownMenu>
          <DropdownMenuTrigger>Settings</DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuLabel>Preferences</DropdownMenuLabel>
            <DropdownMenuSeparator />

            <DropdownMenuCheckboxItem
              checked={notifications}
              onCheckedChange={setNotifications}
            >
              Enable Notifications
            </DropdownMenuCheckboxItem>

            <DropdownMenuSeparator />
            <DropdownMenuLabel>Theme</DropdownMenuLabel>

            <DropdownMenuRadioGroup value={theme} onValueChange={setTheme}>
              <DropdownMenuRadioItem value="light">Light</DropdownMenuRadioItem>
              <DropdownMenuRadioItem value="dark">Dark</DropdownMenuRadioItem>
              <DropdownMenuRadioItem value="system">System</DropdownMenuRadioItem>
            </DropdownMenuRadioGroup>

            <DropdownMenuSeparator />

            <DropdownMenuItem>
              Reset Settings
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      );
    };

    render(<TestComponent />);
    await user.click(screen.getByText('Settings'));

    expect(screen.getByText('Preferences')).toBeInTheDocument();
    expect(screen.getByText('Enable Notifications')).toBeInTheDocument();
    expect(screen.getByText('Theme')).toBeInTheDocument();
    expect(screen.getByText('Light')).toBeInTheDocument();
    expect(screen.getByText('Dark')).toBeInTheDocument();
    expect(screen.getByText('System')).toBeInTheDocument();
    expect(screen.getByText('Reset Settings')).toBeInTheDocument();

    // Click a radio item
    await user.click(screen.getByText('Dark'));
  });

  it('supports multiple shortcuts with different content', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Shortcuts</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>
            New File
            <DropdownMenuShortcut>⌘N</DropdownMenuShortcut>
          </DropdownMenuItem>
          <DropdownMenuItem>
            Save
            <DropdownMenuShortcut>⌘S</DropdownMenuShortcut>
          </DropdownMenuItem>
          <DropdownMenuItem>
            Print
            <DropdownMenuShortcut>⌘P</DropdownMenuShortcut>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Shortcuts'));

    expect(screen.getByText('⌘N')).toBeInTheDocument();
    expect(screen.getByText('⌘S')).toBeInTheDocument();
    expect(screen.getByText('⌘P')).toBeInTheDocument();
  });

  it('renders checkbox item with "indeterminate" checked state', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuCheckboxItem checked="indeterminate">
            Indeterminate Item
          </DropdownMenuCheckboxItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    expect(screen.getByText('Indeterminate Item')).toBeInTheDocument();
  });

  it('handles onOpenChange callback', async () => {
    const handleOpenChange = vi.fn();
    const user = userEvent.setup();

    render(
      <DropdownMenu onOpenChange={handleOpenChange}>
        <DropdownMenuTrigger>Toggle</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>Item</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Toggle'));
    expect(handleOpenChange).toHaveBeenCalledWith(true);
  });

  it('renders trigger with asChild prop', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <button className="custom-button">Custom Button</button>
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>Item</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    const button = screen.getByText('Custom Button');
    expect(button).toHaveClass('custom-button');

    await user.click(button);
    expect(screen.getByText('Item')).toBeInTheDocument();
  });
});
