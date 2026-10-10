---
name: release
description: Prepare and execute the Android release process after explicit confirmation.
---

Prepare and execute the next Android release only when the user explicitly asks you to do so.

1. Inspect `app/build.gradle.kts` for `versionCode` and `versionName`. Check the working tree first and preserve unrelated changes.
2. Identify the previous release tag (`v*`). If the local clone lacks tags or history, retrieve the necessary refs before proceeding; do not assume the previous version.
3. Analyze commits and relevant diffs since that tag. Select important user-facing changes and write concise release notes.
4. Increment `versionCode` and `versionName` according to the convention established by prior releases. If the convention is unclear, ask the user rather than guessing.
5. Add the Fastlane changelog at `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`. The release workflow requires this file and a tag named `v<versionName>`.
6. Review all changes and run appropriate existing validation, including `./gradlew :app:assembleRelease` when the environment permits.
7. Show the new version, changelog, and complete Git diff. Ask the user for explicit confirmation before committing or pushing, then stop and wait.
8. After confirmation, recheck the working tree and stage only the release changes. Commit with `chore(release): v<versionName>`.
9. Create an annotated tag named `v<versionName>`. If that tag already exists locally or on the remote, stop; never overwrite it.
10. Push the release commit and tag to the existing Git remote without force-pushing. Never discard unrelated changes or rewrite history.
