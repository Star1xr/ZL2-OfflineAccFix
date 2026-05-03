# Custom Homepage User Guide!

Welcome to Zalith Launcher2's custom homepage! You can use **Markdown** syntax to write your homepage!  
Just like this homepage, you can refer to its writing style—I'm sure you'll get it~

In addition to standard Markdown syntax, we also provide a series of **extension components** to make the interface richer and the layout more flexible!

---

## Comments

Adding comments in the source code can explain the purpose of certain content ~~(to avoid confusing yourself XD)~~  
Comments are **completely ignored** during rendering and will not be displayed on the interface.

**Correct example:**  
// This is a single-line comment, it will not be displayed on the interface
  // Indented comments also work
Write any content, the comment is ignored~


**Incorrect example:**  
This line is not a comment // The text after this will be rendered as normal text because // must be at the beginning of the line


---

## Cards
Cards are **content grouping containers**, fully supporting Markdown syntax inside.

### Basic Card

...card-start title="Simplest Card"
This is the simplest card, containing just a title and a paragraph of text ~~(nonsense)~~
...card-end

### Supported Attributes

| Attribute        | Description                                                                                                                                                                                | Example                                                                            |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `title`          | Card title. Leave empty `""` to hide the title bar.                                                                                                                                        | `title="Welcome Page"`                                                             |
| `contentPadding` | Padding (in dp).<br>Supports 1, 2 or 4 values, representing: (all sides), (horizontal, vertical), (left, top, right, bottom).                                                              | `contentPadding=(12)`<br>`contentPadding=(8, 16)`<br>`contentPadding=(4, 8, 4, 8)` |
| `shape`          | Corner radius.<br>Options: `extraSmall`, `small`, `medium`, `large`, `extraLarge`<br>Or specify directly like `12dp`<br>Or a percentage (e.g., `50` meaning 50% radius, can form a circle) | `shape=large`<br>`shape=16dp`<br>`shape=50`                                        |

...card-start title="Example Card" shape=large contentPadding=(10)
Inside the card, you can use **bold**, *italic* and other Markdown syntax.

- **title**: Card title, leave empty to hide the title block.
- **contentPadding**: Padding.
- **shape**: Corner radius style, supports Material predefined values, dp values, or percentages.
...card-end

> **Note**: Cards cannot be nested inside cards, otherwise the inner card will be rendered as plain text.

---

## Buttons
Buttons are used to trigger interaction events, or can also be purely decorative.

### Basic Syntax

A button must include `text`, `event` is optional:
...button text="Display-only button"
...button text="Visit YouTube" event="url=https://www.youtube.com/"
...button text="Check for updates" event="launcher=check_update"

- **url**: Opens the specified link in the system browser.
- **launcher**: Triggers a launcher-specific event tag.

### Button Styles

We provide four Material Design 3 button styles:
...button text="Default style"
...button-outlined text="Outlined style"
...button-filled-tonal text="Filled tonal style"
...button-text text="Text style"

The style is specified by the suffix of `...button`:
- No suffix → Filled style
- `-outlined` → Outlined style
- `-filled-tonal` → Filled tonal style
- `-text` → Text style

---

## Horizontal Layout (Row)
Use the `Row` component to arrange **buttons and images** horizontally, avoiding the default vertical layout.

### Basic Usage

...row-start
    // Yes, we can indent child components!
    // No indentation is fine either, just for eye comfort
    // And you can use as many spaces as you want for indentation
    ...button text="Button 1"
    ...button-outlined text="Button 2"
...row-end

### Detailed Row Configuration
Row supports two main attributes, aligned with Jetpack Compose's native Row component:

#### 1. `horizontalArrangement` (Horizontal Distribution)

**Standard values:**
- `Arrangement.Start` (default)
- `Arrangement.Center`
- `Arrangement.End`
- `Arrangement.SpaceBetween`
- `Arrangement.SpaceAround`
- `Arrangement.SpaceEvenly`

...row-start horizontalArrangement=Arrangement.SpaceBetween
    ...button text="Left"
    ...button text="Middle"
    ...button text="Right"
...row-end

**Distribution with spacing:**
...row-start horizontalArrangement=Arrangement.spacedBy(12)
    ...button text="Button A"
    ...button text="Button B"
...row-end

You can also specify the horizontal alignment (`Alignment.Start`, `Center`, `End`):
...row-start horizontalArrangement=Arrangement.spacedBy(12, Alignment.End)
    ...button text="Placed at end, spacing 12"
    ...image url="https://www.gstatic.com/images/branding/googlelogo/svg/googlelogo_clr_74x24px.svg" width=20%
...row-end

#### 2. `verticalAlignment` (Vertical Alignment)

- `Alignment.Top` (default)
- `Alignment.CenterVertically`
- `Alignment.Bottom`

...row-start verticalAlignment=Alignment.CenterVertically
    ...button text="Vertically centered"
    ...image url="https://www.gstatic.com/images/branding/googlelogo/svg/googlelogo_clr_74x24px.svg" width=10%
...row-end

### Weight
Distribute remaining space proportionally to child elements.  
Inside a Row component, you can specify the `weight` parameter for each button or image to control their share of the remaining space.

- Syntax: `weight=(weightValue)` or `weight=(weightValue, noFill)`
- `weightValue`: a decimal or integer, e.g., `1`, `2.5`
- `noFill` (optional): when `noFill` is added, the element will not be forced to fill its allocated space; its width is determined by its own content.

...row-start
    ...button weight=(1) text="1"
    ...button weight=(2) text="2"
    ...button weight=(3, noFill) text="3, noFill"
...row-end

> If you want an element not to take weight and keep its original width, just omit the `weight` parameter.

---

## Image Component
Enhanced image component supporting **percentage width** and **fixed DP width**, with customizable corner radius.

### Supported Attributes

| Attribute | Description                                     | Example                                                                                   |
|-----------|-------------------------------------------------|-------------------------------------------------------------------------------------------|
| `url`     | Image link, this attribute is required.         | `url="https://www.gstatic.com/images/branding/googlelogo/svg/googlelogo_clr_74x24px.svg"` |
| `width`   | Width: a number (in dp) or a percentage string. | `width=150`<br>`width=50%`                                                                |
| `shape`   | Corner radius, usage same as for cards.         | `shape=medium`<br>`shape=20dp`<br>`shape=50`                                              |

### Examples

Width: 50% (50% of the overall homepage width)  
Shape: medium corner radius  
...image url="https://picsum.photos/300/200" width=50% shape=medium
Width: 48 (size 48dp)  
Shape: 50% radius, circle  
...image url="icon.png" width=48 shape=50

---

## Important Notes
- Cards cannot be nested; otherwise the inner card will be treated as plain text.
- Row currently only supports buttons and images inside.
- Weight is only effective for child elements inside a Row and must be written on the same line as the corresponding child component's parameters.
- Tags must appear in pairs: `...card-start` with `...card-end`, `...row-start` with `...row-end`.
- Extension components cannot be embedded in standard Markdown containers: extension components are relatively independent and not fully integrated into Markdown syntax. For example, you cannot put `...card-start` inside a Markdown list item or blockquote.
- Image loading depends on the network; ensure the image link is accessible.

Although we provide rich extension components, the entire homepage is still based on **Markdown**.  
If you're not yet familiar with standard Markdown syntax, we recommend spending a few minutes learning it — it's really easy!  
https://www.markdownguide.org/getting-started/