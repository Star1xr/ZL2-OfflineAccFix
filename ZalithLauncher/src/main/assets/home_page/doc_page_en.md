# Custom Homepage User Guide!

Welcome to Zalith Launcher2's custom homepage! You can use **Markdown** syntax to write your homepage!   
In addition to standard Markdown, you can use the following extension components to enrich your homepage.

### Extension Rules
- **Comments**:
    - Lines starting with `//` are ignored. You can use comments to explain certain content.
    - However, comment lines inside Markdown code blocks will NOT be ignored.
- **Components**:
    - Extension components start with `...`, e.g. `...button`.
    - Components support attributes to control appearance or behavior. After a space on the component line, you can fill in the corresponding attributes.

//This is a comment line, you can only see me when editing the file!

---

### Card Component
Used to wrap content inside a container with background and rounded corners.

**Syntax:**
...card-start title="My Card" shape=large contentPadding=(16, 12)
Here is the content inside the card, supporting standard **Markdown**.
...card-end

**Parameter description:**
- `title`: Card title, optional. If not present or left empty, the card will not show the title block.
...card-start title="Example"
This card demonstrates how to **configure a title**.
...card-end
- `shape`: Corner radius, optional.
    - You can use preset sizes provided by MaterialTheme: `extraSmall`, `small`, `medium`, `large`, `extraLarge`.
    - You can use a specific numeric value to control the corner radius: `12dp` (supports integers and decimals).
    - You can also use a percentage corner radius, e.g., `20%` (only integer percentages supported).
    - Because this attribute distinguishes units, you must include the unit, otherwise the attribute will not take effect.
...card-start title="Shape Example" shape=medium
A card with medium rounded corners!
...card-end

...card-start title="Shape Example" shape=4dp
A card with 4dp rounded corners!
...card-end

...card-start title="Shape Example" shape=20%
A card with 20% rounded corners!
...card-end
- `contentPadding`: Controls the inner padding of the card.
    - Format: `(all)`, `(horizontal, vertical)`, `(left, top, right, bottom)`. Supports integers and decimals.
    - Since the unit for this attribute can only be `dp`, you do NOT need to include the unit; otherwise the attribute will not take effect.
...card-start title="Content Padding Example" contentPadding=(12)
contentPadding=(12)
...card-end

...card-start title="Content Padding Example" contentPadding=(12, 8)
contentPadding=(12, 8)
...card-end

...card-start title="Content Padding Example" contentPadding=(4, 4, 12, 12)
contentPadding=(4, 4, 12, 12)
...card-end


---

### Button Component
Creates a clickable button.

**Syntax:**
...button text="Visit YouTube" event="url=https://www.youtube.com/"
...button-outlined text="Check for updates" event="launcher=check_update"

**Button styles:**
- `...button`: Filled style
...button text="Filled style"
- `...button-outlined`: Outlined style
...button-outlined text="Outlined style"
- `...button-filled-tonal`: Filled tonal style
...button-filled-tonal text="Filled tonal style"
- `...button-text`: Text-only style
...button-text text="Text-only style"

**Parameter description:**
- `text`: The text displayed on the button, required. The value must be wrapped in double quotes.
- `event`: The event to trigger, optional. The value must be wrapped in double quotes.
    - `url=...`: Opens a link in the browser.
    - `launcher=...`: Triggers certain launcher events.

---

### Horizontal Layout (`Row`)
Lays out multiple components (currently only buttons and images) horizontally.  
This component aligns with the Row component in Jetpack Compose.

**Syntax:**
...row-start horizontal=spacedBy(8) vertical=Center
    ...button text="Button 1" weight=(1)
    ...button text="Button 2" weight=(1)
...row-end

**Parameter description:**
- `horizontal`: Horizontal arrangement parameters.
    - Available arrangements: `Start`, `Center`, `End`, `SpaceEvenly`, `SpaceBetween`, `SpaceAround`.
    - You can use `spacedBy` to control the distance between child items:
        - Distance only: `spacedBy(12)` (supports integers and decimals).
        - Control both distance and horizontal alignment: `spacedBy(12, Start)`. Only the values `Start`, `Center`, `End` are supported here.
        - The unit for this attribute can only be `dp`, so you do NOT need to include the unit; otherwise the attribute will not take effect.
    - Examples:
...row-start horizontal=Start
...button text="Start"
...button text="Start"
...row-end

...row-start horizontal=spacedBy(12)
...button text="spacedBy(12)"
...button text="spacedBy(12)"
...row-end

...row-start horizontal=spacedBy(12, End)
...button text="spacedBy(12, End)"
...button text="spacedBy(12, End)"
...row-end
- `vertical`: Vertical alignment.
    - Available alignments: `Top`, `Center` (or the more semantic `CenterVertically`), `Bottom`.
    - Example of vertical centering:
...row-start vertical=Center
...button text="Center"
...image url="https://www.gstatic.com/images/branding/googlelogo/svg/googlelogo_clr_74x24px.svg" width=10%
...row-end
- **Child attribute `weight`**:
    - This attribute can only be used inside a Row component, used to allocate width to child components.
    - You can specify a weight value (supports integers and decimals), and the child's width will be allocated according to the actual width of the homepage.
    - If you add the `noFill` configuration, the component will take the width corresponding to that weight, but it will not actually fill the allocated area.
    - The value of this attribute has no unit; it only represents a proportion.
      - Example:
...row-start
  ...button text="Button 1" weight=(1)
  ...button text="Button 2" weight=(2, noFill)
  ...image url="https://www.gstatic.com/images/branding/googlelogo/svg/googlelogo_clr_74x24px.svg" weight=(2)
...row-end

---

### Image Component
Although Markdown already includes syntax for defining images, it is not very flexible (e.g., you cannot adjust position, width, etc.).  
We can use the advanced image component to solve this problem.

**Syntax:**
...image url="https://picsum.photos/400" width=40% shape=12dp

**Parameter description:**
- `url`: Image link, required.
- `width`: Width of the image, optional.
    - You can use a percentage width, calculated based on the actual width of the homepage: `50%` (only integer percentages supported).
    - You can use DP units to set a more specific width: `200dp` (supports integers and decimals).
    - Because this attribute distinguishes units, you must include the unit, otherwise the attribute will not take effect.
    - Examples:
...image url="https://picsum.photos/500" width=50%
...image url="https://picsum.photos/600" width=120dp
- `shape`: Corner radius of the image, same as the shape parameter for cards.
- `weight`: Weight, only available inside a Row component. If `weight` is set, `width` will be completely ignored.

## Important Notes
- Cards cannot be nested; otherwise the inner card will be treated as plain text.
- Tags must appear in pairs: `...card-start` with `...card-end`, `...row-start` with `...row-end`.
- Extension components cannot be embedded inside standard Markdown containers: extension components are relatively independent and not fully integrated into Markdown syntax. For example, you cannot put `...image` inside a Markdown code block or table.
- Image loading depends on network; ensure the image link is accessible.

Although we provide rich extension components, the entire homepage is still based on **Markdown**.  
If you are not yet familiar with standard Markdown syntax, we recommend spending a few minutes learning it — it's really easy!  
https://www.markdownguide.org/getting-started/