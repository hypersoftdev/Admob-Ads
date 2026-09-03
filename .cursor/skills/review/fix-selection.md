# Fix selection (shared)

Used by `review-architecture`, `review-performance`, `review-security`, and `review-complete`.

## Numbering (mandatory)

1. Assign a **single continuous integer** (`1`, `2`, `3`, …) to every actionable finding in the report.
2. Order: **Critical → Warnings / High → Medium → Suggestions / optional**.
3. Each numbered line must be one concrete fix (actionable), not a vague note.
4. Skip pure informational “Pass” notes — only number items the agent could implement if asked.
5. For `review-complete`, keep **one** number sequence across Architecture + Performance + Security (do not restart at 1 per section).

Example shape:

```markdown
## Fix list
1. [Critical] …
2. [Critical] …
3. [Warning] …
4. [Suggestion] …
```

## After the report (mandatory)

1. **Stop.** Do not implement any fixes in the same turn as the review.
2. End the user-visible reply by asking which numbers to fix. Use this prompt (adapt only the count if needed):

> Reply with the numbers to fix (e.g. `fix 1, 2, 4, 7`). Say `fix all` for every item, or `none` to skip.

3. Wait for the user’s reply.
4. On reply:
   - Parse selected numbers (and `fix all` / `none`).
   - Implement **only** the selected items.
   - Ignore unselected numbers.
   - If a selected item needs missing secrets / user input, ask once — do not invent values.
5. After applying selected fixes, briefly list what was done by number (e.g. `Fixed 1, 2, 4`).
