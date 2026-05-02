# Custom Homepage User Guide!

Welcome to Zalith Launcher2's custom homepage! You can use Markdown syntax to write your homepage!  
Just like this homepage, you can refer to its writing style, I'm sure you'll get it~  

Of course, in addition to standard Markdown syntax, we also provide some **extension components** to enhance the interface interaction!


---


### Comments
We can use simple comments to explain certain parts of the homepage ~~(to avoid confusing ourselves XD)~~  
Comments are used to add notes in the source code and are completely ignored during rendering.  
If you add `//` at the beginning of a line, that line will be treated as a comment line.


**Correct example:**  
// This is a single-line comment, it will not be displayed on the interface
  // Indented comments also work
Write any content, the comment is ignored~


**Incorrect example:**  
This line is not a comment // The text after this will be rendered as normal text because // must be at the beginning of the line


---


### Cards
Cards are containers that group content, and they fully support Markdown syntax inside.

**Below is a basic card**  
...card-start title="Simplest Card"
This is the simplest card, containing just a title and a paragraph of text ~~(nonsense)~~
...card-end

**Supported attributes**: title, contentPadding, shape  
You can use attributes to control the appearance of the card:

> title="Custom Styled Card"
> shape=large
> contentPadding=(10)

...card-start title="Custom Styled Card" shape=large contentPadding=(10)
Inside the card, you can use Markdown syntax like **bold**, *italic*  
Here's a detailed introduction to the card attributes~

- **title**: The title of the card. Leave it empty `""` to hide the title bar
    - ```text
      title="This is a title!"
      ```
- **contentPadding**: Margins. Supports `(all sides)`, `(horizontal, vertical)`, or `(left, top, right, bottom)`.
    - ```text
      Padding of 12 on all sides
      contentPadding=(12)
      Horizontal 0 padding, vertical 12 padding
      contentPadding=(0, 12)
      Left 0 padding, top 12 padding, right 12 padding, bottom 0 padding
      contentPadding=(0, 12, 12, 0)
      ```
- **shape**: Corner radius. Supports `extraSmall`, `small`, `medium`, `large`, `extraLarge` or a specific value like `16dp`
    - ```text
      Use a very small corner radius, provided by MaterialTheme!
      shape=extraSmall
      Use a relatively small corner radius, provided by MaterialTheme!
      shape=small
      Use a medium corner radius, provided by MaterialTheme!
      shape=medium
      Use a relatively large corner radius, provided by MaterialTheme!
      shape=large
      Use a very large corner radius, provided by MaterialTheme!
      shape=extraLarge
      Jetpack Compose uses dp as the measurement unit, you can specify how large you want the corner radius to be
      shape=16dp
      If you don't specify the dp unit, we will treat it as a percentage, e.g., 50 means a perfect circle
      shape=50
      ```
...card-end

...card-start title="Important Notes" shape=large contentPadding=(10)
When using cards, we need to be aware of the following:
1. A card is a component that allows nesting of child components. To delimit the content, we must use a start tag and an end tag to enclose the content, otherwise the launcher won't know which components are inside the card.
2. Nesting cards inside cards is not allowed! If you insist on nesting, it will look like this:

...card-start title="Nested Card"
Content inside the nested card
...card-end
...card-end


---


### Buttons and Layout
Buttons are used to trigger interaction events, but they can also do nothing!

**Basic buttons and events**  
A button must include `text`, `event` is optional:  
...button text="Display-only button"
...button text="Visit YouTube" event="url=https://www.youtube.com/"
...button text="Launcher function" event="launcher=check_update"

- **url**: Opens the specified link in the system browser  
- **launcher**: Triggers a launcher-specific event tag  

**Button appearance styles**
We provide four Material Design 3 button styles:
...button text="Default style"
...button-outlined text="Outlined style"
...button-filled-tonal text="Filled tonal style"
...button-text text="Text style"

**Horizontal layout control: Row component**  
This is a quite important layout component. As you can see from the examples above, buttons are arranged vertically by default—this is very bad!  
At this point, we can use `Row` to control the layout!  
`Row` places child components horizontally. Currently, `Row` only supports buttons inside! ~~Nonsense, buttons are the only components available for now~~

...row-start
...button text="Button 1"
...button-outlined text="Button 2"
...row-end

**Detailed Row configuration options:**  
These attributes align with Jetpack Compose's native Row component.  
- **horizontalArrangement**: Controls horizontal distribution
    - Standard: `Arrangement.Start`, `Center`, `End`, `SpaceBetween`, `SpaceAround`, `SpaceEvenly`
      - ```text
        horizontalArrangement=Arrangement.Start
        horizontalArrangement=Arrangement.Center
        horizontalArrangement=Arrangement.End
        horizontalArrangement=Arrangement.SpaceBetween
        horizontalArrangement=Arrangement.SpaceAround
        horizontalArrangement=Arrangement.SpaceEvenly
        ```
    - Spacing: `Arrangement.spacedBy(12)`
      - ```text
        Places a fixed distance of 12 between every two adjacent child components.
        If the remaining width is empty, the distance parameter can be negative, in which case child components will overlap!
        You can specify alignment to horizontally align the spaced items within the parent.
        horizontalArrangement=Arrangement.spacedBy(12)
        
        You can specify a horizontal Alignment to align the spaced items within the parent.
        spacedBy only supports horizontal Alignment: Alignment.Start, Alignment.End, Alignment.CenterHorizontally
        horizontalArrangement=Arrangement.spacedBy(12, Alignment.Start)
        ```
- **verticalAlignment**: Controls vertical alignment  
    - `Alignment.Top`, `Alignment.CenterVertically`, `Alignment.Bottom`


---


### Important Notes
When writing extension components, please adhere to the following rules to ensure correct rendering:

**Strict nesting prohibition**:  
- **No card nesting**: For interface simplicity, you cannot define another card inside a card.  
- **No Row nesting**: For now, Row can only contain button series components.  
- **Combination allowed**: You can place `Row` components and button components inside a card.  

**Syntax details**:  
- Extension syntax cannot be embedded directly inside standard Markdown syntax (e.g., cards cannot be placed inside Markdown lists, blockquotes, etc.).  
- Component tags that can contain child components must appear in pairs.  

**You must at least know basic Markdown**:  
- After all, this is an extension of Markdown; basic content still follows standard Markdown syntax.  
- If you don't know it, no worries—it's really easy to learn! https://www.markdownguide.org/getting-started/
