# Application UI and UX Design

## Vision

TaskFlow provides a clean, accessible interface that reduces cognitive load while managing daily task workflows. The design emphasizes clarity, quick actions, and role-aware features.

## Design Principles

1. **Clarity Over Decoration:** Information-first layout, minimal visual noise
2. **Progressive Disclosure:** Show only relevant controls per role
3. **Rapid Task Entry:** Form optimized for quick task creation
4. **Scannable Lists:** Tasks sortable/filterable for focus
5. **Role-Aware Context:** Leads see team oversight; members see personal focus
6. **Accessibility First:** Keyboard navigation, screen reader support, color contrast

## Color Palette

### Primary Brand Colors
- **Brand Teal:** `#0f8b8d` (primary actions, navigation)
- **Brand Dark Teal:** `#0a6b6c` (hover states, contrast)
- **Accent Orange:** `#e07a5f` (error, alerts, warnings)
- **Warning Red:** `#c0392b` (critical alerts, overdue)

### Neutral Colors
- **Ink (text):** `#15212f` (primary text)
- **Slate (secondary text):** `#41566d` (secondary text, labels)
- **Background Soft:** `#f4f8fb` (page background)
- **Card (white):** `#ffffff` (card/panel background)
- **Line (border):** `#d5e1ea` (dividers, borders)

### Semantic Colors
- **Success Green:** `#27ae60` (completed, done)
- **Info Blue:** `#3498db` (informational messages)
- **Warning Amber:** `#f39c12` (warnings, caution)
- **Error Red:** `#c0392b` (overdue, errors)

## Typography

### Font Stack
```scss
font-family: 'Segoe UI', 'Trebuchet MS', sans-serif;
```

### Type Scale

| Element | Size | Weight | Line Height | Usage |
|---------|------|--------|------------|-------|
| H1 | 28-32px | 700 | 1.2 | Page title |
| H2 | 20-24px | 600 | 1.3 | Section headers |
| H3 | 16-18px | 600 | 1.4 | Card titles |
| Body | 14-16px | 400 | 1.5 | Regular text |
| Small | 12-13px | 400 | 1.4 | Labels, metadata |
| Code | 12-13px | 500 | 1.5 | Monospace |

## Layout & Spacing

### Grid System
- 12-column responsive grid
- Breakpoints: 1200px (desktop), 960px (tablet), 680px (mobile)
- Gutter: 16-24px (varies with breakpoint)

### Spacing Scale
```
xs: 4px
sm: 8px
md: 16px
lg: 24px
xl: 32px
xxl: 48px
```

### Layout Regions

```
┌─────────────────────────────────────────┐
│ TOPBAR (h=80px)
│  Logo/Title  |  User Sign-In Widget
├─────────────────────────────────────────┤
│ DASHBOARD GRID (4 metric cards, 2 rows)
├─────────────────────────────────────────┤
│ CONTENT GRID (Form [2/3] | Reminders [1/3])
├─────────────────────────────────────────┤
│ TASK PANEL (Filters + List)
├─────────────────────────────────────────┤
│ REPORT PANEL (Lead Only)
└─────────────────────────────────────────┘
```

### Responsive Adjustments

**Desktop (> 1200px):**
- Form 2/3 width, Reminders 1/3 width (side-by-side)
- 4-column metric grid
- Full-width task list

**Tablet (960-1200px):**
- Form and Reminders stack vertically
- 2-column metric grid
- Full-width task list

**Mobile (< 960px):**
- Single-column layout
- 1-column metric grid (or hidden, scrollable)
- Full-width form, reminders below
- Full-width task list

## Component Specifications

### Topbar

**Height:** 80px  
**Content:**
- Left: Logo + "TaskFlow" title + subtitle
- Right: User sign-in dropdown

**Style:**
- Background: Gradient with soft background and teal accent
- Border-bottom: 1px solid line color
- Box shadow: Subtle elevation

```scss
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 80px;
  border-bottom: 1px solid var(--line);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
```

### Metric Card

**Layout:** 4 cards in responsive grid  
**Content:**
- Title (H3 size)
- Large metric number
- Optional: Trend indicator or subtext

**States:**
- Normal: White card with teal title
- Alert (Overdue): Subtle red border + warm background

**Example:**
```
┌──────────────────┐
│ Visible Tasks    │
│      12          │
└──────────────────┘
```

### Form Panel

**Layout:** Two-column grid with full-width textarea/selects  
**Fields:**
1. Title (text input) | Due Date (date input)
2. Description (textarea, full-width)
3. Priority (select) | Status (select)
4. Assign To (select, full-width)
5. Add Task (button, full-width at bottom)

**Validation:**
- Red border on invalid inputs
- Error message below field
- Submit button disabled until valid

**Accessibility:**
- `<label>` for each input
- `aria-label` on selects
- Error messages associated with inputs

### Task Card

**Layout:** Vertical stack  
**Sections:**
1. **Header:**
   - Title (H3)
   - Owner & Assignee (small text)
   - Description (body text)

2. **Metadata Chips:**
   - Priority badge (colored pill)
   - Status badge (colored pill)
   - Due date badge

3. **Controls:**
   - Assignment dropdown (left)
   - Status buttons (right)

**States:**
- Normal: White card with gray border
- Overdue: Warm border + soft background

**Example:**
```
┌─────────────────────────────┐
│ Prepare Sprint Goals        │
│ Owner: Alex  Assign: Priya  │
│ Align team on roadmap       │
│                             │
│ [High] [In Progress] [Due 12/31]
│                             │
│ Assign: [v Alex v]          │
│ [To Do] [Progress] [Done]   │
└─────────────────────────────┘
```

### Filter Panel

**Layout:** 4-column grid of selects (responsive to 2-col on tablet, 1-col mobile)  
**Filters:**
1. Owner (All / User 1 / User 2 / ...)
2. Status (All / To Do / In Progress / Done)
3. Priority (All / High / Medium / Low)
4. Due Date (All / Overdue / Today / Next 7 Days)

**Interaction:**
- Immediate filtering (no submit button)
- Visual feedback on active filters

### Reminder Panel

**Layout:** Vertical list or empty state  
**Item:** 
- Task title (clickable to scroll to task)
- Due date + assignee name (small text)

**Empty State:**
- Centered message: "No reminders right now"

**Example:**
```
┌─────────────────────┐
│ Due in 24 Hours     │
│                     │
│ Finalize sprint ... │
│ 2026-06-22 · Priya  │
│                     │
│ Fix dashboard ...   │
│ 2026-06-22 · Daniel │
└─────────────────────┘
```

### Report Panel (Lead Only)

**Layout:** Scrollable table  
**Columns:**
- Assignee Name
- Open (count)
- Completed (count)
- Overdue (count)

**Styling:**
- Header row: Teal background, white text
- Data rows: Alternating white/off-white background
- Hover: Highlight row background

**Example:**
```
┌──────────────┬─────┬──────────┬────────┐
│ Assignee     │Open │Completed │Overdue │
├──────────────┼─────┼──────────┼────────┤
│ Priya Nair   │  3  │    5     │   1    │
│ Daniel Kim   │  2  │    3     │   0    │
└──────────────┴─────┴──────────┴────────┘
```

## Interaction Patterns

### Create Task Workflow

1. User fills form (title, due date, description, priority, assignee)
2. Form validates in real-time
3. User clicks "Add Task"
4. Form submits and resets
5. New task appears at top of list
6. Toast notification: "Task created"

### Edit Task Workflow

1. User selects new status or assignee from task card
2. UI updates immediately
3. Card refreshes with new values
4. Toast notification: "Task updated"

### Filter & Search Workflow

1. User adjusts any filter dropdown
2. List updates immediately (no page reload)
3. Task count updates in metric cards
4. For narrow filters: "No tasks match your filters" message

### Archive/Delete Workflow

1. User clicks delete icon on task card
2. Confirm dialog: "Delete this task permanently?"
3. On confirm, task removed from list
4. Toast notification: "Task deleted"

## Accessibility Features

### Keyboard Navigation

- Tab through all interactive elements
- Enter/Space to activate buttons
- Arrow keys in dropdowns
- Escape to close modals/dropdowns

### Screen Reader Support

```html
<!-- Semantic HTML -->
<header role="banner">
  <h1>TaskFlow</h1>
</header>

<main role="main">
  <!-- Form labels -->
  <label for="task-title">Task Title</label>
  <input id="task-title" type="text" required />
  
  <!-- Disabled state -->
  <button [disabled]="!isFormValid" aria-disabled="true">Add Task</button>
  
  <!-- Error messages -->
  <input aria-describedby="title-error" />
  <div id="title-error" role="alert">Title is required</div>
</main>
```

### Color Contrast

- Normal text (# 1.0pt): Ratio 4.5:1 (WCAG AA)
- Large text (28pt+): Ratio 3:1 (WCAG AA)
- No information conveyed by color alone

### Focus States

```scss
button:focus,
input:focus,
select:focus,
a:focus {
  outline: 2px solid var(--brand);
  outline-offset: 2px;
}
```

### Motion & Animation

- Minimal animations (avoid flashing/seizure triggers)
- Respect `prefers-reduced-motion` media query
- Animations < 300ms for snappy feel

## Mobile-First Responsive Design

### Strategy
1. Design for mobile first (< 680px)
2. Enhance for tablet (680-960px)
3. Optimize for desktop (> 960px)

### Touch-Friendly Controls
- Minimum touch target: 44x44px
- Button padding: `0.55rem 0.9rem` minimum
- Spacing between buttons: >= 8px

### Orientation Support
- Landscape mode (optional): Adjust column counts
- Prevent layout shift on orientation change
- Sticky control panel during scroll (optional)

## Dark Mode (Future, Phase 2)

**Color Variables:**
```scss
@media (prefers-color-scheme: dark) {
  :host {
    --ink: #f0f2f5;
    --slate: #b0b8c0;
    --bg-soft: #1a1f26;
    --card: #242a32;
    --line: #3a4252;
    --brand: #4db8bb;
  }
}
```

## Design System Components

### Button Styles

```scss
// Primary Action
.primary-btn {
  background: var(--brand);
  color: white;
  border-radius: 10px;
  padding: 0.55rem 0.9rem;
  border: none;
  cursor: pointer;
  
  &:hover {
    background: var(--brand-dark);
  }
}

// Secondary Action
.secondary-btn {
  background: transparent;
  color: var(--brand);
  border: 1px solid var(--brand);
  border-radius: 8px;
  padding: 0.42rem 0.6rem;
  
  &:hover {
    background: rgba(15, 139, 141, 0.05);
  }
}

// Danger Action (Delete)
.danger-btn {
  background: var(--warning);
  color: white;
  
  &:hover {
    background: darken(var(--warning), 10%);
  }
}
```

### Form Input Styles

```scss
input,
textarea,
select {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 0.45rem 0.55rem;
  width: 100%;
  font-size: 14px;
  background: white;
  
  &:focus {
    outline: none;
    border-color: var(--brand);
    box-shadow: 0 0 0 3px rgba(15, 139, 141, 0.1);
  }
  
  &[aria-invalid="true"] {
    border-color: var(--warning);
  }
}
```

### Badge/Chip Styles

```scss
.chip {
  background: #eef6f6;
  color: var(--brand-dark);
  border-radius: 999px;
  padding: 0.12rem 0.6rem;
  font-size: 0.75rem;
  font-weight: 500;
  display: inline-block;
}
```

---

**Document Version:** 1.0  
**Last Updated:** June 2026  
**Status:** MVP Design Specification
