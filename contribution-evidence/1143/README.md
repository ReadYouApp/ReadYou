# Nested OPML import #1143

Matched, unedited installed-APK screenshots using the same three-feed synthetic
OPML. Before: official 0.16.2 payload resigned with a disposable fixture key.
After: exact official APK patched through Android Morphe Manager 2.2.0 and
installed through its normal update UI. These are backport APK captures, not a
rebuilt current-main source APK. Source tests separately exercise this PR.

| Before: 1/3 feeds | After: 3/3 feeds |
| :---: | :---: |
| ![Before](before.png) | ![After](after.png) |

Original source fails two nested-import tests; fixed source passes all 13 app
unit tests across five suites. All three feed preferences are retained in both
source regression and installed-app database checks. See provenance.json for
exact revisions, APK/device/fixture hashes and comparison conditions. The source
change keeps the existing flat group model and does not add nested-folder UI.
