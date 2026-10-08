# Current 0.4.1 status — October 8, 2026
Organic materials now cover all 13 species with six independent visual gates, including separate JUP staking. The user provided a real Seeker recording. HTTPS payment deployment and real checkout acceptance remain pending. See ORGANIC-TRAITS-0.4.1.md and VALIDATION.md.

## Historical implementation notes below
# Walletkin 0.4.0 — Premium, Store and organic traits
The current user specification is PREMIUM-STORE-SPEC-UA.md. Historical eligibility-only instructions in 0.3.0 are superseded.

MVP policy: minimum active stake 100 SKR; payment 100 SKR for 30 days; recipient 6y8jfXZHHSYYaVLaEDnokW3MEKg1JfzA9S1Ld6zCv4SF.
Free Resonance still uses verified stake >0. Natural species/DNA/rarity never depend on amount or payment.

Implemented in source:
- Premium screen: wallet-specific Locked/Eligible, verified Active/Grace/Expired/Inactive/Unknown, configured threshold/price, expiry, benefit previews.
- Explicit MWA sign-and-send checkout; client constructs only checked SKR transfer + unique order memo, checks token accounts, mint and mainnet; confirms price and recipient before wallet signing.
- Pending order/signature persistence and idempotent verification recovery. No automatic resend or automatic billing.
- Backend fresh finalized staking check before quote; finalized transaction verification checks signer, memo, mint, decimals, source, destination, recipient ownership/increase, amount, timestamp and signature reuse. Durable single-instance JSON ledger.
- 30-day wallet-bound entitlement; 7-day grace below configured stake, capped by paid expiry. Unknown RPC never fabricates unstake or entitlement.
- Actual Premium benefits: branded 2160x2880 HD PNG card and 1536x1536 transparent avatar PNG export.
- Store tab, eight previews (three backgrounds, three material color themes, two effects), wallet-bound verified inventory, Equip/Unequip. Store does not require stake/Premium. Accessory and animation packs are planned.
- Server configuration can change threshold and prices without rebuilding APK. Future 3/6/12 month plans have no invented discounts and are not sold yet.
- Existing earned history and free functionality remain available.

Still required before real purchases:
- Deploy backend behind trusted HTTPS with durable storage; enter its URL under connection settings.
- Merchant's initialized ordinary SPL SKR token account.
- Configure/approve individual Store prices; products are currently preview-only.
- Real Seeker checkout/receipt/recovery/export acceptance. Local unit and HTTP fixture tests are not a mainnet payment test.
Backend details and deployment limits: backend/README.md. The public wallet record API does not serve private data; signed wallet sessions are required before adding protected server content.

Trait artwork:
- The initial procedural increase in size was rejected by user. It is not approved organic artwork.
- User approved Astrafox organic master v3 and its full five-trait brief. A generated body atlas is integrated into AstrafoxView using four independently selected, body-alpha-clipped, feathered regions: permanent SKR structure/active light share chest; Genesis forehead; Jupiter fur flow; NFT crystalline memory.
- Material state comes solely from existing evidence predicates. Only explicit Legends preview turns every material on. Tail, blink, breathing and tap remain separate and consistent.
- New organic conversion for the other twelve species is NOT complete or approved. Their existing procedural renderer remains until reviewed replacement packages are generated. Abyssfin brief is currently awaiting user approval.
- NFT Collector is observed successful NFT protocol interaction, not verified owned NFT collections.
- Seven Astrafox assembly diagnostics were inspected. These are desktop geometry previews, not Android screenshots.

Presentation 0.4.0: eight slides, existing approved organic concept comparisons, SKR/Seekerborn, current Premium terms, actual vs planned features, and pending deployment/device acceptance. No clickable hyperlinks.


## Abyssfin organic materials — approved
The user approved the assembled Abyssfin with Seeker Genesis at the stationary tail base. Runtime now selects painted SKR, active SKR light, Genesis, Jupiter gills and NFT memory regions independently from confirmed profile evidence. The approved atlas is registered onto the existing body and clipped to its alpha. No synthetic preview state is stored as wallet evidence.
The tail mesh keeps a 190-pixel radius around (1230,545) stationary, covering the overlapping peduncle; free lobes retain idle and tap deformation. This preserves the approved resting silhouette. Desktop assembly is reviewed; physical Seeker motion acceptance remains pending.
Organic raster materials now cover Astrafox and Abyssfin (2/13 normal species). The remaining eleven still use the earlier procedural treatment pending individual reviewed conversion.
