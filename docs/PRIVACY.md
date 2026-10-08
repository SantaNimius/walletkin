# Walletkin MVP privacy
The app stores connected public addresses, scan snapshots, memories, local settings and wallet authorization locally on the device. The Solana RPC endpoint receives public wallet addresses and requests to fetch blockchain data. Providers can log network metadata under their own policies.
Mobile Wallet Adapter passes authorization to the user's selected wallet; Walletkin does not collect private keys or seed phrases and does not submit transactions.
There is no application analytics or generative-lore server in this MVP. Story / Data text derives from local confirmed evidence. Public blockchain data remains public.
The native share card excludes the configured RPC URL and full wallet address. Users choose where to share it through Android's chooser.
Disconnect revokes wallet authorization; it does not erase every saved Colony entry or make public blockchain history private. Clearing application storage removes local snapshots/settings but cannot remove public transactions.
This is a description of the prototype, not a claim that a published privacy-policy site already exists. Team contact and public policy URL must be supplied before store publication.

## 0.4.0 checkout update
Wallet history scanning stays read-only. Explicit purchases may request signing and sending one SKR transfer plus an order memo through an MWA wallet.
The user reviews amount and recipient before approving in the wallet. No seed phrase, private key, automatic recurring payment or token allowance is requested.
A configured commerce service receives wallet address, order, payment signature, verified staking observations and purchased item IDs to verify purchases and restore inventory. It stores wallet-bound payment/entitlement records. The operator controls retention and deployment; no service is configured by default.
Local pending payment details and cosmetic choices are kept per wallet. RPC providers continue to receive public on-chain requests.

