Current status — 0.2.0, 2026-10-07: Genesis v2 assigns the thirteen canonical ordinary lineages. All five Anomaly packages are complete. Riftdrake replaces the copper Dragon; Degen Kong uses approved bare arms. Six stable color palettes and halos are implemented. Older pending/v1 statements below are historical and superseded by this status and MOTION-AUDIT-UA.md.

# World recovery and character generation state

The user requires preserving the world from the earlier ChatGPT conversation. There is no callable read_thread tool in this session. The user provided a text fragment on 2026-10-06.

Source fragment lists 16 species archetypes: Fox, Dragon, Golem, Wisp, Shark, Feline, Plant, Mech, Demon, Bird, Slime, Insectoid, Cosmic, Reptile, Beast, Unknown; it also proposes a separate Anomaly. The latest user request is 13 lineages + Anomaly (14 total), with several Anomaly forms in one catalog entry. Do not pretend the fragment already supplies a complete 14-species canon or named lore for each.

Pending roster proposal: Fox, Dragon, Golem, Wisp, Shark, Plant, Mech, Demon, Bird, Slime, Insectoid, Cosmic, Reptile + Anomaly. Await user selection before making this canonical.

Only confirmed named example in the supplied fragment: Astrafox. See art/canonical/astrafox/BRIEF.md. No character regeneration is authorized until its own brief is explicitly approved.

The eight Emberkin/Tidekeeper/etc sprites are a rejected visual preview: the user objected to their shared anatomy. They remain temporarily in the UI preview APK; they must not be described as the final original world or the finished fourteen species.

The Legends UI and loader are prepared for fourteen unique entries, including exactly one Anomaly entry with multiple forms. assets/legends.json deliberately contains no invented names or lore while the canonical entries await confirmation.

User-required process: present one character's name, source lore, anatomy, silhouette, detail palette and animation layers; ask generate or do not generate; wait for an explicit answer; generate only approved character; review it before proceeding to the next. Do not batch regenerate all fourteen under the previous broad authorization.

UI 0.1.2 preview includes five background images and six evidence-based background traits (Guardian and SKR share the sanctuary art). Backgrounds were independently requested and approved. Genesis selection is unchanged; approved canonical expansion needs versioned DNA and migration decisions after the roster is known.
Latest state: Astrafox base/body/tail/blink is approved and generated. User requires one combined approval for every character's complete set of animation frames and parts, all explained upfront. Dragon full package brief was presented and generation approval requested; no Dragon generation yet. Working roster now includes Degen Ape and two feline categories (Cat/Lynx) in place of Demon/Insectoid/Reptile. Anomaly has five concept-approved bodies: dachshund, manta, octopus, fractal and singularity snail. They count as one species. Request a complete Anomaly art-package approval before generating its frames. Canonical art appears incrementally in Legends; current profile Genesis v1 assignment is preserved pending versioned full-roster migration.

Dragon complete package generated and its static assembly checked; Android integration is in the next 0.1.4 preview build. Golem full package brief has been presented, awaiting explicit approval. Canonical PNG packs now share a bounded 64 MiB cache so adding the roster does not retain all image packs indefinitely.

Golem full appearance accepted by user, then complete animation bundle authorized ("ок роби").
Body, separate viewer-left and viewer-right arms and blink are generated. Initial wrong right-arm output retained as rejected; targeted correction used in app. Eye-only clipping avoids unrelated generative changes during blink.
Static assembly preview uses the Android renderer's source/destination coordinates; it is not a device screenshot.

Wisp full appearance generated from approved brief. Animation layers still require appearance acceptance and complete package authorization.

Wisp appearance accepted and entire animation bundle authorized by user. Body, upper trail, lower trail and blink saved under art/canonical/wisp, with generation prompts. Static assembly visually checked. Legends now contains four approved appearances; full fourteen-species Genesis migration remains pending.
Canonical sprite cache now bounds rendered layers to 768px with a 64 MiB shared cache. Source rectangles are converted to decoded pixel coordinates; original PNGs retained unchanged.

Shark full appearance explicitly authorized ("я ж погодив генеруй"), generated and saved under art/canonical/shark. Animation package awaits acceptance of the full appearance.

Shark style v1 rejected. User authorized dark marine fantasy / cel-shading proposal ("давай"). Version 2 saved as art/canonical/shark/base-v2.png with PROMPT-v2.md. Await appearance acceptance before animation layers.

Shark v2 appearance accepted and full animation package explicitly authorized. Five layers saved: body, tail, near fin, far fin and blink. Runtime feather mask at tail joint smooths the extraction boundary without editing source PNGs. Independent swimming motions, eye-only blink, gill pulse and tap dash implemented in Legends.

Plant full botanical-engraving appearance accepted, entire animation bundle authorized. Four layers generated and saved on D:, static assembly checked, independently swaying branches and eye-only blink added to Legends. Six approved species now displayed; expanded Genesis assignment remains pending.

Mech full appearance brief authorized ("давай"), generated and saved under art/canonical/mech with original prompt. Animation package awaits appearance acceptance.

Mech complete animation package explicitly authorized and generated. Four layers saved on D:, static assembly checked, independent manipulator movement and sensor-only shutter added in Legends. Seven approved appearances displayed; expanded Genesis assignment remains pending.

Bird full appearance generated following user instruction to proceed to the presented brief. Saved under art/canonical/bird with prompt. Await appearance acceptance before full animation package.

Bird paper sculpture full appearance generated following explicit style selection. Saved under art/canonical/bird/base-v2-paper.png with PROMPT-v2-paper.md. Await appearance acceptance before animation layers.

Paper Bird complete animation package accepted and generated. Five layers saved on D:, static assembly checked, wing/tail movement and eye-only blink added in Legends. Eight approved appearances now displayed; fourteen-species Genesis assignment remains pending.

User changed approval workflow: batch review of five remaining normal species (Slime/Cosmic/Degen Ape/Cat/Lynx). Five style options for each; complete appearances after choices; animation packages after batch acceptance. Anomaly handled separately. Bird package already approved and being built.

Batch style choices explicitly supplied by user:
Slime: art glass with refracted colors and internal bubbles.
Cosmic: retrofuturism, geometric orbits, limited palette and grain.
Degen Ape: street graphics, forceful brushwork and distressed texture.
Cat: Art Nouveau, flowing contours and organic markings.
Lynx: painted realism, natural anatomy and textured fur.
User authorized full appearance generation through the agreed batch workflow. Await batch appearance acceptance before animation packages. Anomaly excluded.

Batch appearance review changes requested by user:
- Degen Ape: keep street-graphic style; add cyberpunk clothing and crypto-degen reference.
- Cat: keep Art Nouveau; add bright neon color accents.
- Lynx: reject overly realistic appearance; user explicitly chose low-poly sculpture with large faceted planes and geometric anatomy.
- Cosmic: remove accidental tiny lettering; corrected full appearance saved as base-v2.png.
- Cat: framing corrected first in base-v2.png to retain full ears/tail with margin; neon edit uses that version.
Full appearance updates authorized; animation packages still await batch acceptance. Slime original art-glass appearance remains unchanged.

User explicitly accepted all five current appearances and authorized every described animation package together.
Slime: body/blink and animated deformation/pulse.
Cosmic: core, two main rings, satellite group and blink.
Degen Ape: clothed body, two arms and blink.
Cat: body, long tail and blink.
Lynx: body, short tail and blink.
Anomaly remains excluded. Portrait catalogue cards will use complete accepted artwork; detailed creatures retain layered animation, keeping gallery memory bounded as the roster expands.

## 2026-10-06: approved five-species animation batch
Slime, Cosmic, Degen Ape, Cat and Lynx animation packages generated after explicit collective approval. Thirteen ordinary species appear in Legends. Anomaly excluded pending its separate process. Gallery uses accepted full portraits; detail uses animated layered rigs. Runtime artwork is bounded at 768px long edge and shared 64 MiB cache; all 13 portraits total 25.05 MiB, largest one detail package 11.25 MiB. Existing Genesis v1 wallet assignment remains compatible, so canonical Legends expansion does not silently change existing wallet profiles.
Wallet 6y8j...v4SF: generated canonical Astrafox comparison concept, not current APK screenshot. Public RPC returned a 100-successful-signature window across 40 UTC days, spanning 38.82 days. Proposed appearance maps Rhythm/Veteran/Roots plus four seed motif nodes. Genesis v1 actually assigns Coralune. No Jupiter/SKR/Seeker claim made.

2026-10-06 user art steering: organic trait integration required. No SGT/protocol lettering on creatures; use verified Genesis artwork and actual emblems as material-aware features. Shark swap arrows/JUP words rejected. Lynx prior inlay scale and placement preferred. Regenerate 13 conceptual comparisons before modular trait art acceptance. Anomaly: five style choices per approved form, await explicit selections; full appearances first, animation packages only after collective appearance acceptance. Bright saturated, crisp line/detail, unique legendary designs required.

Latest user steering: Dragon Jupiter should blend into native skin; Slime trait integration still too detached. Revise into scale rhythm / internal glass lamellae, remove detached protocol glyphs. User wants scalable design for dozens/hundreds of traits: proposed channel-based material/pattern/core/motion/ambient composition, not new MVP detectors or stacked badges. See TRAIT-VISUAL-SYSTEM.md.

User confirmed the main trait principle: harmonious organic placement in a coherent picture. Cosmic Genesis moved to original tiny circle between/above eyes and positively received. Preserve this final appearance; do not reintroduce an oversized forehead medal. Anomaly style choices remain pending; no Anomaly images or animation packages generated.
2026-10-07: all five Anomaly full appearances generated and accepted. User explicitly authorized complete animation packages. Dachshund opal, Manta stained glass, Octopus psychedelic screenprint, Fractal prismatic geometry, Snail galactic enamel. Anomaly remains one species with five forms.

