# Перевірка графіки Walletkin 0.2.0
7 жовтня 2026. Проєкт і результати на D:.

Переглянуто 13 звичайних істот і 5 форм Аномалії у шести діагностичних станах: спокій, два крайні положення idle, два крайні положення реакції на дотик, моргання. Це складання runtime PNG за координатами; воно не перевіряє Android hardware rendering, частоту кадрів чи роботу на фізичному Seeker.

Перевірені: Astrafox, Riftdrake, Jadewarden, Lumenwisp, Abyssfin, Fernroot, Byteforge, Foldwing, Chromagel, Orbitron, Degen Kong, Glyphcat, Prismlynx. Аномалія: Opal Dachshund, Celestial Manta, Psychedelic Octopus, Prismatic Fractal, Galactic Snail.

Виправлення:
- Astrafox, Abyssfin, Glyphcat: хвіст малюється один раз із плавним вигином і нерухомою зоною кріплення; у кота усунено неправильну точку обертання.
- Riftdrake: новий погоджений комплект, два крила, чотири лапи, один хвіст; виправлена область моргання.
- Degen Kong: обидві руки перегенеровано без одягу; жилет нерухомий, непрозора заглушка пройми усунена при відображенні.
- Решта: переглянуто стики, перекриття шарів, крайні положення й моргання. Orbitron має передню/задню частини кільця; Восьминіг — вісім щупалець, Равлик — окреме моргання обох стебелець.

Genesis v2 використовує ті самі 13 звичайних видів у Legends, reveal, profile, Colony і share. Шість кольорів детерміновані адресою; палітра однакова для тіла, рухомих частин і моргання. Аномалія залишається окремим каталогом.

MVP має idle, дихання/ширяння, моргання й tap. Складніші пози, емоції та хореографія подій — план повного продукту.

Фізична перевірка 0.2.0 на Seeker залишається відкритою за SEEKER-ACCEPTANCE.md. Діагностичні кадри не видавати за скриншоти/відео застосунку.

0.3.1: user reported Degen Kong arm overlapping the vest edge. Reduced the left armhole cutout to the inner opening and reduced both arm rotation amplitudes from 0.8 to 0.55 degrees idle (tap multiplier unchanged). Body stays above both bare arm layers. Re-rendered and reviewed rest, idle extrema, tap extrema and blink. Physical Seeker confirmation remains pending.

## 0.3.2 — Degen Kong: accepted whole illustration
The 0.3.1 cutout adjustment and regenerated front-arm assembly were rejected visually and are superseded. The regenerated arm is retained only as an unsuccessful art experiment.
DegenKongView renders the accepted full portrait with a 32x28 bitmap mesh. Local weights keep shoulders, vest and face fixed; hand displacement is limited to 3 pixels idle / 8 pixels on tap. There are no independently overlaid arm or clothing layers. Breathing, tap bounce and blink remain.
Six desktop diagnostic states were rendered and inspected using scripts/audit-kong.ps1. These are geometry previews, not device screenshots or a measured Seeker performance test. Physical acceptance remains pending. Other characters are unchanged in this patch.
