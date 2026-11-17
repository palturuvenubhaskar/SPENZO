# SPENZO logo direction

## Brand brief

SPENZO is an offline, privacy-first expense tracker. Its identity should make money management feel clear and dependable, without implying a bank connection, investment advice, or a complicated accounting tool.

## Design direction

- **Mark:** A custom S letterform drawn as one continuous, rounded path. The uninterrupted line suggests a simple record of money moving through the month.
- **Logo type:** Letterform mark. The app name remains the wordmark in product surfaces and store listings.
- **Distinctiveness:** Avoids the wallet, coin, bar-chart, and tree motifs common in personal-finance products.
- **Character:** Calm, direct, modern, and approachable.

## Color

| Use | HEX | RGB | CMYK (approx.) |
| --- | --- | --- | --- |
| Spenzo mint | `#3DDC97` | `61, 220, 151` | `72, 0, 31, 14` |
| Deep slate background | `#111B24` | `17, 27, 36` | `53, 25, 0, 86` |
| White reverse mark | `#FFFFFF` | `255, 255, 255` | `0, 0, 0, 0` |
| Black monochrome mark | `#000000` | `0, 0, 0` | `0, 0, 0, 100` |

The mint matches the app's dark-theme primary color. The slate gives the launcher icon a distinct but compatible field; it is not a replacement for the app's screen backgrounds. CMYK values are standard conversions; confirm them against the print provider's output profile before a production print run.

## Use

- Keep the mark centered in a square canvas with at least 16% clear space on each side.
- Use the mint mark on deep slate for the app icon. Use white on dark surfaces or black on light surfaces when a single-color mark is needed.
- Preserve the stroke and proportions. Do not stretch, rotate, add a shadow, or place it on a low-contrast background.
- The Android adaptive icon uses the vector mark, so Android can scale and mask it for launcher shapes. Use the SVG source for other vector contexts.
- At 16 px, confirm the S remains recognizable; for smaller uses, use the platform's native app icon treatment rather than adding details.

## Source files

- `spenzo-mark.svg` - scalable master mark.
- `spenzo-mark-white.svg` - reverse mark for dark backgrounds.
- `spenzo-mark-black.svg` - one-color mark for light backgrounds and monochrome output.
- `spenzo-icon.svg` - full app icon (mark on the navy radial field).
- `spenzo-icon-512.png` - 512 px store icon.
- `../app/src/main/res/drawable/ic_launcher_background.xml` - adaptive background (navy radial gradient).
- `../app/src/main/res/drawable/ic_launcher_foreground.xml` - adaptive foreground (mint gradient S).
- `../app/src/main/res/drawable/ic_launcher_monochrome.xml` - themed-icon layer.
- `../tools/logo.py` - regenerates every file above (`python tools/logo.py`).

The icon ships as an adaptive icon only: `minSdk` is 26, so no density-specific PNG fallbacks are needed and the mark stays crisp at every launcher size.
