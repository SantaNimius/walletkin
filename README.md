# Walletkin
A living, explainable biography of your Solana wallet, built for Seeker.

Connect a wallet with Mobile Wallet Adapter and reveal deterministic Genesis DNA. Confirmed activity contributes memories, traits and personal lore. Story explains the creature; Data shows the evidence and scan limits.

## Download and demo
[Release v0.4.1](https://github.com/SantaNimius/walletkin/releases/tag/v0.4.1) | [Android APK](https://github.com/SantaNimius/walletkin/releases/download/v0.4.1/walletkin-0.4.1.apk) | [75-second demo](https://github.com/SantaNimius/walletkin/releases/download/v0.4.1/walletkin-demo-75s-en.mp4) | [Pitch PDF](https://github.com/SantaNimius/walletkin/releases/download/v0.4.1/walletkin-pitch-0.4.0.pdf)

## Hackathon build
Android **0.4.1** (versionCode 19), package `app.chainling`. The user ran this build on Seeker and supplied the screen recording used for the 75-second English-captioned demo. This is a debug test APK, not a dApp Store release. The payment flow has not been tested on mainnet.

APK SHA-256: `6A2531C0991544BA9FEA2327A522741C8A18A5BCE95D3796BED3DB76BA31C68C`.

## Implemented
- MWA connect, reconnect and disconnect; public mainnet RPC by default.
- Versioned deterministic Genesis DNA v2: 13 canonical ordinary lineages and six inherited color palettes. Local migration preserves memories and previously verified Seekerborn.
- Six independent visual trait gates: SKR Mark/Core, Active Resonance, Seekerborn, Jupiter Navigator, JUP Orbit Keeper and NFT Collector. Painted material inlays cover all 13 ordinary species; visual previews are clearly distinguished from wallet evidence.
- Sixteen evidence-based trait rules, timestamped memories, Story/Data explanation, profile and rescan/mutation transitions.
- SKR balance and staking analysis. Earned Resonance Mark/Core persist locally; dynamic staking effects become dormant after verified zero stake. Unavailable checks stay unknown.
- Seekerborn after positive Token-2022 ownership and extension checks; locally permanent after verification.
- Jupiter v6 successful interaction and a separate verified JUP governance staking decoder.
- My Colony, native PNG sharing, background traits, basic idle/blink/breathing and tap reactions.
- Legends: Astrafox, Riftdrake, Jadewarden, Lumenwisp, Abyssfin, Fernroot, Byteforge, Foldwing, Chromagel, Orbitron, Degen Kong, Glyphcat and Prismlynx.
- One Anomaly catalogue entry with five forms: Opal Dachshund, Celestial Manta, Psychedelic Octopus, Prismatic Fractal and Galactic Snail. Anomaly is not automatically assigned to wallets.
- Premium eligibility/terms, HD and transparent PNG exports, eight Store previews, and Android/backend code for configured SKR checkout.

## SKR and Premium
Free Resonance effects activate above zero verified active SKR stake. MVP Premium eligibility requires at least **100 actively staked SKR**; the proposed paid plan is **100 SKR / 30 days**. Staking and purchases do not change Genesis DNA or rarity.

The checkout source uses a wallet-confirmed checked SKR transfer and unique order memo. A Node backend verifies finalized transfers and prevents transaction reuse. **HTTPS deployment and a real Seeker payment test are pending.** Individual Store prices are not configured. Future 3/6/12-month discounts, richer animations, widgets, Deep DNA and GIF/video exports are planned, not shipped.

## Build on Windows, entirely on D:
Install JDK 17 or 21, Android SDK platform 35, build-tools 35.0.0, platform-tools and Gradle 8.9 under the project:
```text
D:\Chainling\toolchain\java\<jdk-directory>
D:\Chainling\toolchain\android-sdk
D:\Chainling\toolchain\gradle-8.9
```
Clone/copy this repository to `D:\Chainling`. Create an untracked `local.properties` containing `sdk.dir=D:/Chainling/toolchain/android-sdk`, then run:
```powershell
powershell -ExecutionPolicy Bypass -File D:\Chainling\scripts\build.ps1
```
The script routes SDK state, Gradle caches, Java home and temp files to D:, validates artwork, runs unit tests/lint and assembles the debug APK. It preserves an existing D: debug key or creates one if absent. A fresh reviewer key cannot update the supplied APK in place: use a separate device or uninstall first, which removes local profiles.

Kotlin 2.1.10; AGP 8.7.3; Gradle 8.9; MWA 2.0.7; min SDK 26; target/compile SDK 35. No Gradle wrapper or SDK is bundled. Android Studio reviewers must configure their own SDK/caches and local debug key.

Backend: Node 22+, no npm dependencies. In `backend`, run `node --test` and `node server.mjs`. See [backend setup](backend/README.md) for deployment requirements.

## Validation and limits
The packaged build passed **55 Android tests**, Android lint with **0 errors / 21 warnings**, and **7 backend tests**. The supplied demo is a real Seeker screen recording; this does not prove a completed payment/deployment test.

Public RPC scans examine the newest 100 signatures and up to 20 unseen successful transaction details per pass, accumulating evidence locally. This is not full lifetime history. Optional checks may remain unknown; core failures retain the previous snapshot. NFT Collector signals successful NFT protocol interaction, not a verified inventory of owned NFT collections. Basic animation is MVP quality.

Settings and Colony are local. No seed phrase or private key is requested. Purchases require explicit wallet confirmation. The current MWA identity `https://walletkin.invalid` is a reserved development URI and must be replaced with a team-owned identity before public distribution.

## Repository contents and disclosure
Android source/runtime art: `app/src`; backend: `backend`; validation and design notes: `docs`; Windows build/audit scripts: `scripts`. SDKs, caches, private signing keys, raw screen recordings and generated APKs are excluded from Git. Distribute APK/demo/deck as Release assets or separate hosted files.

Code and character artwork were created with AI assistance and reviewed by the builder. Protocol references belong to their respective owners; no affiliation is claimed. No blanket open-source license is granted in this submission; licensing terms remain a team decision. See [art provenance](docs/ART-PROVENANCE.md) and [privacy](docs/PRIVACY.md).

Official hackathon: https://solanamobile.radiant.nexus/
