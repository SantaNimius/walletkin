# Walletkin working rules

- Keep this native Android MVP and every SDK, dependency cache, temporary file, keystore and build result on D:. Use scripts/build.ps1; do not invoke a default Gradle/npm setup that writes to C:.
- Keep the golden path stable: connect/reconnect, bounded scan, reveal, profile, rescan/mutation. Do not add features outside the agreed MVP.
- Genesis v1 uses public-key bytes and the fixed ordered eight-species registry. Never reorder it or change its modulus when adding the six reserved species. Add an explicit DNA version for expansion.
- Traits, memories and lore must be backed by confirmed successful on-chain evidence. Preserve null/unknown status and scan-window limits. Never invent balances, wallet age, full-history coverage or Seeker ownership.
- Seekerborn is a permanent local cosmetic flag after Token-2022 ownership/extension verification. This MVP has no server authentication, global anti-Sybil guarantee or reward claims.
- Keep the public mainnet RPC golden path usable with partial results, bounded timeouts, retained snapshots and rescan retry. Do not require a paid API key.
- Preserve the D: debug keystore across rebuilds. Never commit cache, local.properties, provider credentials or signing keys.
- Run the existing meaningful domain/analyzer tests and Android lint when changing behavior, then assemble the debug APK. The low-memory JVM configuration is intentional for this computer.
- Do not describe a build as physically Seeker-tested until the device acceptance run has actually passed. Use docs/SEEKER-ACCEPTANCE.md.

- User explicitly authorized a visual redesign and a Legends catalog containing the original 13 lineages plus one multi-form Anomaly. Preserve the original world: await the user-provided canonical names/lore instead of inventing substitutes. Existing Genesis v1 species order must remain compatible; any expanded assignment requires explicit versioning.

- Latest user instruction: every regenerated character needs its own reviewed brief and explicit generate/do-not-generate answer before image generation. Never infer character approval from a preselected option or elapsed time. See docs/WORLD-RECOVERY.md. Current eight sprites are a rejected preview, not the final canon.

- User clarified approvals are per complete character package, including every animation frame/layer explained upfront; do not split approval into individual frames. Astrafox's base/body/tail/blink package is approved. Other characters require a complete brief and their own explicit approval before generation.

Latest user workflow override: remaining normal species (Slime, Cosmic, Degen Ape, Cat, Lynx) are reviewed together. Offer five styles per species, await explicit style choices, generate complete appearances as one batch, review them together, then generate animation packages after acceptance. Anomaly remains a separate generation/review process.
