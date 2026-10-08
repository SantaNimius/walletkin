# Walletkin commerce backend
MVP: 100 SKR active stake minimum, 100 SKR payment for 30 days. Recipient: 6y8jfXZHHSYYaVLaEDnokW3MEKg1JfzA9S1Ld6zCv4SF.

Node 22+, no npm dependencies. On this workstation use D:/Chainling/toolchain/node/node.exe. Keep TEMP/TMP, DATA_DIR and every log on D:.

Run: node --test; then node server.mjs. Default bind is 127.0.0.1:8087.
Before exposing it, place it behind your HTTPS reverse proxy and set a durable DATA_DIR outside source. Use one server instance and one worker: the atomic JSON ledger is an MVP store, not a distributed database. Back up ledger.json; losing it loses purchase records. SOLANA_RPC must be Solana mainnet HTTPS. Public RPC may throttle getProgramAccounts and finalized transaction lookups; errors never grant purchases.

config.json is loaded for every request. Change premiumPriceSkr and minimumStakeSkr for later releases without updating APK. Future 90/180/365-day plans are metadata only; discounts are not invented and only the reviewed 30-day plan is sold. Store products remain unavailable until you set their individual prices and availability. No staking required for ordinary cosmetics.
Merchant needs an initialized ordinary SPL SKR token account before checkout. The app does not fund or create the merchant account automatically.

Payments: fresh server staking check, exact checked SPL SKR transfer plus unique memo, finality, signer, recipient owner/balance increase, mint, decimals, timestamp/order window, amount, global transaction-signature reuse prevention. Pending quotes preserve agreed prices across config updates. Payment verification is idempotent.
Grace: 7 days from first verified loss of the threshold, bounded by paid expiry. Restore stake before grace deadline to return Active; unknown RPC state preserves timing and marks paid access Unknown. Scans do not establish continuous historical stake.
Actual Premium benefits in this APK: branded 2160x2880 PNG cards and 1536x1536 transparent avatar PNG. Deep DNA, Wrapped, animated/GIF exports, widgets, advanced packs are planned, clearly labeled.

API: GET /v1/config; GET /v1/wallet?address=...; POST /v1/orders {wallet,item}; POST /v1/verify {orderId,signature}.
Inventory/premium status are public wallet-bound records. This prototype does not serve private analytics or protected downloads. Add signed wallet sessions and server-enforced access before introducing those services. Local Android presentation is not a security boundary on modified devices.
No keys, seed phrases, auto billing, token approvals or custody. Wallet signing authorizes payment only.

Deployment and a real Seeker wallet payment test are pending. Unit/HTTP fixture tests are not a mainnet purchase test.

