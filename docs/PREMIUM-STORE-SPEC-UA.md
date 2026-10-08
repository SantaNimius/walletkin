Так, які умови реалізаціі преміуму? напиши
Я б зафіксував умови Premium для Walletling ось так, щоб їх уже можна було реалізовувати:

Walletling — Resonance Premium
1. Головна умова
Resonance Premium неможливо придбати без активного staking SKR.

Система повинна перевіряти стан staking безпосередньо для підключеного Solana-гаманця.

Стани:

SKR не застейканий → Premium Locked

SKR активно застейканий → Premium Eligible

SKR застейканий + оплачений Premium → Premium Active

Таким чином користувач не може просто заплатити за Premium — активний staking SKR є обов'язковою умовою.

2. Мінімальний staking
Не робити великий поріг, який перетворить Premium на функцію для великих власників SKR.

Для MVP бажано використовувати мінімальну технічну умову:

Active SKR Stake > 0

Пізніше можна встановити невеликий мінімальний поріг, якщо це буде необхідно для захисту від зловживань.

Кількість застейканих SKR не повинна визначати rarity Walletling.

3. Вартість Premium
Premium оплачується в SKR.

Цільова ціна:

приблизно $1–3 на місяць в еквіваленті SKR.

Не бажано назавжди фіксувати конкретну кількість SKR, оскільки курс токена може змінюватися.

Система повинна дозволяти змінювати Premium Price без оновлення застосунку.

Наприклад:

premium_price_skr = configurable

4. Перевірка перед оплатою
Перед активацією Premium:

Connect Wallet

↓

Check SKR Staking

↓

якщо staking відсутній:

RESONANCE PREMIUM LOCKED

Active SKR staking is required.

↓

якщо staking активний:

RESONANCE DETECTED

Premium Available

↓

користувач підтверджує оплату SKR

↓

після успішної on-chain транзакції:

RESONANCE PREMIUM ACTIVATED

5. Premium прив'язується до Wallet Address
Premium належить не акаунту застосунку, а конкретній Solana-адресі.

Наприклад:

Wallet A — Premium Active

Wallet B — Free

Wallet C — Premium Eligible

У My Colony статус кожного Walletling показується окремо.

Premium не переноситься автоматично між адресами.

6. Термін дії
Базовий варіант:

30 днів Premium з моменту успішної оплати.

Не потрібно одразу робити автоматичне списання SKR.

Перед завершенням:

Resonance Premium expires in 3 days.

Після завершення користувач сам підтверджує наступну оплату.

Це значно простіше та прозоріше для першої версії.

7. Умова продовження
Для продовження Premium одночасно повинні виконуватися дві умови:

Active SKR staking



Premium payment

Якщо staking відсутній, продовжити Premium неможливо.

8. Unstake
Якщо користувач робить unstake під час активного Premium, Premium не потрібно відключати миттєво.

Запускається:

7-day Resonance Grace Period

Статус:

PREMIUM — GRACE PERIOD

Користувач отримує повідомлення:

SKR Resonance has been lost.

Restore your SKR stake within 7 days to maintain Resonance Premium.

Якщо staking відновлений протягом 7 днів:

Premium → Active

Якщо ні:

Premium → Inactive

Оплачений Premium-період не повертається і не компенсується.

9. Що відбувається після втрати Premium
Нічого з історії Walletling не видаляється.

Зберігаються:

Genesis DNA, species, mutations, Memories, Lore, Seekerborn, SKR historical traits, Protocol Memories, Timeline data та всі earned traits.

Блокуються лише Premium-функції.

Після повторної активації вони стають доступними знову.

10. Що дає Premium
Resonance Premium може відкривати:

Full Evolution Timeline;

Deep DNA Analytics;

Deep Chronicle / Biography;

Advanced Animations;

додаткові реакції Walletling;

Weekly / Monthly / Annual Walletling Wrapped;

HD Share Cards;

animated share cards;

GIF/video export;

transparent avatar export;

Home Screen Widgets;

Premium backgrounds;

Premium environments;

cosmetic collections;

animation packs;

додаткову персоналізацію.

11. Що Premium ніколи не дає
За Premium не можна купити:

Species

Genesis DNA

Seekerborn

Anomaly

Wallet Age

Memories

Protocol achievements

SKR historical achievements

Earned mutations

Natural rarity

Premium = додаткові можливості та presentation layer.

Premium ≠ сильніший або рідкісніший Walletling.

12. Seekerborn
Для Walletling із Seeker Genesis можна зробити окрему перевагу.

Наприклад:

25% Resonance Premium discount

При цьому Seekerborn визначається виключно реальною on-chain наявністю/історією Seeker Genesis.

Купити статус Seekerborn неможливо.

13. Staking Streak
Окремо від Premium система відстежує безперервний SKR staking.

Наприклад:

30 days → Resonance Background

90 days → Special Idle Animation

180 days → Resonance Artifact

365 days → Legacy Resonance Effect

Це косметичні Loyalty Rewards.

Вони не впливають на DNA або natural rarity.

14. Статуси Premium
У системі бажано мати чіткі стани:

LOCKED

Немає активного SKR staking.

ELIGIBLE

Staking активний, Premium не оплачений.

ACTIVE

Staking активний + Premium оплачений.

GRACE_PERIOD

Premium оплачений, але staking припинено.

EXPIRED

Закінчився оплачений Premium-період.

INACTIVE

Premium був раніше, але зараз умови не виконуються.

15. Дані
Для кожного wallet достатньо зберігати приблизно:

wallet_address

skr_staking_active

skr_staked_amount

skr_staking_since

premium_status

premium_started_at

premium_expires_at

grace_period_until

last_payment_signature

premium_price_skr

seekerborn

staking_streak_days

16. Захист
Стан Premium не повинен визначатися лише локальними даними телефона.

Backend повинен перевіряти:

wallet address;

SKR staking;

payment transaction;

transaction signature;

amount;

recipient;

timestamp;

expiration.

Користувач ніколи не передає Walletling private key або seed phrase.

Для оплати використовується звичайне підтвердження транзакції через підключений Solana wallet.

17. MVP
Для хакатонної версії не потрібно реалізовувати всю систему.

Достатньо:

Check active SKR staking

↓

Premium Locked / Eligible

↓

показати Resonance Premium screen

↓

показати майбутні Premium benefits

А якщо встигаємо:

SKR payment → Premium Active

Автопродовження, складну billing-систему, Wrapped, widgets та весь Premium-контент можна реалізувати після хакатону.

Основна формула
No Stake → Free

Stake SKR → Resonance + Premium Eligibility

Stake SKR + Pay SKR → Resonance Premium

Unstake → 7-day Grace Period

No Restake → Premium Inactive

При цьому:

Wallet history creates the Walletling.

SKR staking activates its Resonance.

Resonance Premium unlocks deeper interaction with it.

Я б саме Stake > 0 → Premium Eligible залишив для першої версії, а мінімальну кількість SKR вводив уже після того, як побачимо реальну економіку продукту.

мінімальний стейкінг від наприклад 100 SKR, реалізувати механіку для покупки преміуму в апк, і щоб можна було подивитись які саме плюшки доступні з покупкою преміум, також можна зразу організувати магазин в додатку в якому наприклад продаватимуться якісь фони скіни і т.д.
Так, це вже можна оформити як конкретне ТЗ для реалізації в APK. Я б зробив мінімум 100 SKR в активному стейкінгу, а Premium і Store розділив: Premium потребує staking, магазин доступний усім.

Walletling — Resonance Premium & Store
1. Умова доступу до Premium
Для придбання Resonance Premium користувач повинен мати щонайменше:

100 SKR в активному staking

Логіка:

Stake < 100 SKR → Premium Locked

Stake ≥ 100 SKR → Premium Eligible

Stake ≥ 100 SKR + Premium Payment → Premium Active

Кількість SKR перевіряється за підключеною Solana-адресою.

Сам факт наявності 100 SKR на балансі не підходить — токени повинні відповідати визначеній нами механіці активного staking.

2. Premium безпосередньо в APK
У Walletling створюється окремий екран:

RESONANCE PREMIUM

На ньому користувач одразу бачить:

Your staking

73 / 100 SKR

або:

184 SKR staked ✓

Нижче:

Premium Status

LOCKED

ELIGIBLE

ACTIVE

GRACE PERIOD

EXPIRED

Якщо застейкано менше 100 SKR:

Stake at least 100 SKR to unlock Resonance Premium.

Якщо ≥100:

Your Walletling is eligible for Resonance Premium.

З'являється кнопка:

ACTIVATE PREMIUM

3. Оплата Premium
Оплата здійснюється безпосередньо з Walletling через підключений Solana wallet.

Flow:

Premium

→ Check SKR Stake

→ Stake ≥100

→ Choose Premium

→ Confirm

→ відкривається wallet

→ користувач підписує SKR transaction

→ backend перевіряє transaction

→ PREMIUM ACTIVATED

Користувач ніколи не вводить seed phrase або private key.

4. Premium Plans
На старті достатньо одного:

Resonance Premium — 30 days
Ціну задаємо конфігураційно.

Наприклад:

premium_price_skr

В APK кількість не повинна бути hardcoded.

У майбутньому можна додати:

30 Days

90 Days

365 Days

з вигіднішою ціною за довший період.

5. Premium Benefits
До покупки користувач повинен бачити повний каталог того, що отримає.

Екран:

What's inside Resonance Premium?
Deep DNA
Повна деталізація DNA Walletling:

Activity
Explorer
Trader
Collector
Holder
DeFi
DePIN
Gaming
SKR
Chaos

Evolution Timeline
Повна історія розвитку Walletling.

Можливість побачити, як він змінювався з часом.

Deep Chronicle
Розширена біографія Walletling, створена з реальної історії гаманця.

Advanced Animations
Додаткові анімації та реакції:

Happy
Curious
Sleep
Wake
Charged
Trade
Collect
Stake
Mutation

Walletling Wrapped
Weekly / Monthly / Annual summaries.

Наприклад:

This month your Walletling discovered 4 protocols.

17 swaps

2 new Memories

1 mutation

Premium Share
HD Share Cards.

Animated Cards.

GIF/video export.

Transparent avatar export.

Premium Customization
Додаткові можливості персоналізації зовнішнього представлення Walletling.

Widgets
Walletling Home Screen Widget.

6. Preview Premium
Дуже важливо:

користувач Free повинен бачити Premium-функції, але не користуватися ними повністю.

Наприклад:

🔒 Evolution Timeline

🔒 Deep DNA

🔒 Advanced Animations

🔒 Deep Chronicle

Натискання відкриває Preview:

Available with Resonance Premium.

Це набагато краще, ніж просто показувати кнопку «Buy Premium».

7. Walletling Store
У застосунку створюється окрема вкладка:

STORE
Store не повинен бути частиною DNA Engine.

Куплені предмети — це cosmetics, а не earned traits.

8. Категорії Store
Backgrounds
Фони для Walletling:

Cosmic Station
Solana Night
Neon City
Crystal Cave
Ancient Temple
Cyber Room
Forest
Deep Space
Void
Beach

Skins / Cosmetic Overlays
Візуальні cosmetic themes.

Наприклад:

Neon
Cyber
Crystal
Void
Golden
Retro
Holographic

Важливо:

skin не змінює Genesis DNA.

Це лише presentation layer.

Accessories
Окуляри
капелюхи
навушники
ланцюги
рюкзаки
маски
scarves
holographic accessories.

Environments
Повноцінні сцени, де живе Walletling.

Animation Packs
Наприклад:

Playful Pack

Cyber Pack

Degen Pack

Sleep Pack

Effects
Particles
glow
weather
holograms
portal effects
energy effects.

9. Оплата в Store
Основна валюта Store:

SKR

Flow:

Store

→ Select Item

→ Preview

→ BUY — XX SKR

→ wallet confirmation

→ SKR transaction

→ backend verification

→ item added to Inventory.

10. Store доступний без Premium
Це важлива відмінність.

Для покупки звичайної косметики Premium та staking не потрібні.

Наприклад:

Free user:

0 SKR staked

але він може купити:

Cyber Background — 5 SKR

Premium:

потребує:

≥100 SKR actively staked

Таким чином ми не блокуємо магазин за Premium.

11. Premium Store Benefits
Premium-користувач може отримувати додаткові переваги в Store.

Наприклад:

10% Store Discount

або

щомісячний:

Premium Cosmetic Drop

Також можна створити категорію:

Resonance Collection
Доступна тільки користувачам з активним Premium.

12. Seekerborn Benefits
Для Seekerborn можна зробити:

Premium discount

та окрему:

Seeker Collection
Наприклад:

Seeker Background
Genesis Aura
Seeker Room
Genesis Particles.

Але сам Seekerborn / Genesis Core ніколи не продається.

13. Inventory
Після покупки предмет потрапляє в:

MY ITEMS

Категорії:

Backgrounds
Skins
Accessories
Animations
Effects
Environments.

Користувач може:

Preview

Equip

Unequip

Предмет назавжди прив'язується до wallet, який його придбав.

14. Preview перед покупкою
Користувач повинен бачити предмет на власному Walletling до покупки.

Наприклад:

Cyber City Background

→ Preview

Walletling автоматично показується на цьому фоні.

Кнопки:

CANCEL

BUY — 10 SKR

Це особливо важливо для skins, accessories та effects.

15. Purchased vs Earned
У UI необхідно чітко розділяти:

DNA / Earned
Отримано з реальної історії wallet.

Наприклад:

Seekerborn
Explorer Horn
Jupiter Affinity
Resonance Core
Third Eye.

Cosmetics
Придбано або отримано через Store/Premium.

Наприклад:

Cyber Glasses
Neon Background
Crystal Environment.

Косметика ніколи не впливає на rarity Walletling.

16. Store Item Structure
Кожний товар повинен мати приблизно:

item_id

name

description

category

price_skr

preview_asset

asset

premium_required

seeker_required

available

limited

created_at

Таким чином товари та ціни можна змінювати без випуску нового APK.

17. Purchase Record
Для покупки зберігати:

wallet_address

item_id

price_skr

transaction_signature

purchased_at

status

Backend перевіряє транзакцію перед додаванням предмета в Inventory.

18. Основна навігація APK
Я б зробив:

HOME

Walletling

MEMORIES

Timeline / Lore

COLONY

Інші Walletlings користувача

STORE

Cosmetics / backgrounds / animations

PROFILE

Wallet / Settings / Premium

А Resonance Premium має отримати помітний блок у Profile та на Home.

19. Premium Status
LOCKED
SKR Staked: 42 / 100

58 more SKR required

ELIGIBLE
SKR Staked: 146 / 100 ✓

ACTIVATE PREMIUM

ACTIVE
RESONANCE PREMIUM

23 days remaining

GRACE PERIOD
SKR staking requirement is no longer met.

Restore ≥100 SKR staking within 7 days.

20. Важлива умова при падінні staking нижче 100 SKR
Не тільки повний unstake.

Якщо було:

150 SKR

і користувач unstake 60:

90 SKR remaining

умова Premium більше не виконується.

Запускається:

7-day Grace Period

Якщо баланс активного staking знову стає ≥100 SKR:

Premium Active

Якщо ні:

Premium Inactive.

21. Що реалізувати зараз
Для APK уже зараз закласти:

Premium screen.

Перевірку ≥100 SKR staked.

Premium Locked / Eligible / Active.

Каталог Premium Benefits.

Preview заблокованих Premium-функцій.

SKR Premium purchase flow.

Store.

Background category.

Skin category.

Accessories.

Effects.

Preview item на Walletling.

SKR purchase flow.

My Items / Inventory.

Equip / Unequip.

Backend verification покупки.

Конфігураційні ціни товарів.

Чітке розділення Cosmetics та DNA Traits.

Фінальна модель
Wallet history → DNA & Mutations

SKR history → Resonance mutations

≥100 SKR staking → Premium eligibility

≥100 SKR staking + SKR payment → Resonance Premium

SKR → Store purchases

Premium → deeper Walletling experience

Store → visual customization

При цьому:

You can buy cosmetics.

You cannot buy history.

You cannot buy rarity.

You cannot buy DNA.

Це фундаментальне правило економіки Walletling.

Єдине, що я б ще змінив порівняно з попередньою схемою: магазин справді варто закласти вже зараз, навіть якщо для хакатону в ньому буде лише 6–10 тестових товарів. Це одразу демонструє, навіщо SKR потрібен у продукті крім Premium.