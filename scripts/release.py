#!/usr/bin/env python3
"""Interactive release helper for YagniLauncher.

Usage:
    ./scripts/release.py                   # Auto-bump patch, enter changelog, preview, and release
    ./scripts/release.py --name 1.2.0      # Override suggested versionName
    ./scripts/release.py --dry-run         # Preview only without modifying files or git state

Workflow:
    1. Verifies no uncommitted changes to tracked files exist.
    2. Computes next versionCode / versionName (matching .github/workflows/Release.yml).
    3. Prints commits since the latest tag and reads Fastlane changelog lines.
    4. Shows a full preview (Gradle diff, Fastlane file, commit, tag) and asks to proceed.
    5. Writes files, commits, creates an annotated tag, and optionally pushes atomically.
"""

import argparse
import difflib
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(subprocess.check_output(["git", "rev-parse", "--show-toplevel"], text=True).strip())
GRADLE = ROOT / "app/build.gradle.kts"
# Must match the tag validation regex in .github/workflows/Release.yml (without leading 'v').
NAME_RE = re.compile(r"^\d+\.\d+\.\d+(-[0-9A-Za-z.]+)?$")


def git(*args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    """Run a git command from the repository root."""
    return subprocess.run(["git", *args], cwd=ROOT, text=True, capture_output=True, check=check)


def main() -> int:
    """Run the interactive release flow and return a process exit code."""
    parser = argparse.ArgumentParser(description="Bump version, write Fastlane changelog, commit, and tag.")
    parser.add_argument("--code", type=int, help="Override next versionCode (default: current + 1).")
    parser.add_argument("--name", help="Override next versionName (default: bump patch).")
    parser.add_argument("--dry-run", action="store_true", help="Preview without modifying files or git state.")
    args = parser.parse_args()

    # Ignore untracked build/IDE artifacts (e.g. .kotlin/) while guarding tracked files.
    branch = git("branch", "--show-current").stdout.strip()
    if git("status", "--porcelain", "--untracked-files=no").stdout.strip():
        sys.exit("Error: working tree has uncommitted changes to tracked files.")

    # Read current versionCode and versionName from app/build.gradle.kts and compute next version.
    gradle_text = GRADLE.read_text(encoding="utf-8")
    code = int(re.search(r"versionCode\s*=\s*(\d+)", gradle_text).group(1))
    name = re.search(r'versionName\s*=\s*"([^"]+)"', gradle_text).group(1)

    base, *suffix = name.split("-", 1)
    major, minor, patch = map(int, base.split("."))
    new_code = args.code or (code + 1)
    new_name = args.name or (f"{major}.{minor}.{patch + 1}" + (f"-{suffix[0]}" if suffix else ""))
    if not NAME_RE.match(new_name):
        sys.exit(f"Error: invalid versionName '{new_name}'.")

    tag = f"v{new_name}"
    if git("tag", "-l", tag).stdout.strip():
        sys.exit(f"Error: tag '{tag}' already exists.")

    # Show commit history since the latest tag to help write release notes.
    last_tag = git("describe", "--tags", "--abbrev=0", check=False).stdout.strip()
    log_range = f"{last_tag}..HEAD" if last_tag else "-n 20"
    print(f"Current: {name} ({code}) -> Next: {new_name} ({new_code}) on {branch}")
    print(f"\nCommits since {last_tag or 'start'}:")
    print(git("log", log_range, "--oneline", "--no-decorate").stdout.strip() or "(none)")

    # Collect multi-line Fastlane changelog entries until an empty line or EOF (Ctrl+D).
    print("\nEnter Fastlane changelog (blank line or Ctrl+D to finish, Ctrl+C to cancel):")
    lines: list[str] = []
    while True:
        try:
            line = input("> " if not lines else "  ")
        except EOFError:
            break
        if line == "" and lines:
            break
        if line or lines:
            lines.append(line)

    changelog = "\n".join(lines).strip()
    if not changelog:
        sys.exit("Error: changelog cannot be empty.")

    updated = re.sub(r"(versionCode\s*=\s*)\d+", rf"\g<1>{new_code}", gradle_text, count=1)
    updated = re.sub(r'(versionName\s*=\s*")[^"]+(")', rf"\g<1>{new_name}\g<2>", updated, count=1)
    changelog_rel = Path(f"fastlane/metadata/android/en-US/changelogs/{new_code}.txt")
    commit_msg = f"Bump version to {new_name} ({new_code}) and add changelog"

    # Print full preview of all pending changes before writing anything.
    diff = "\n".join(
        difflib.unified_diff(
            gradle_text.splitlines(),
            updated.splitlines(),
            fromfile="a/app/build.gradle.kts",
            tofile="b/app/build.gradle.kts",
            lineterm="",
        )
    )
    print("\n=== Release Preview ===")
    print(f"Branch: {branch}")
    print(f"Tag:    {tag}")
    print(f"Commit: {commit_msg}")
    print(f"\n{diff}\n\n--- {changelog_rel} ({len(changelog)} chars) ---\n{changelog}\n=======================")
    if len(changelog) > 500:
        print(f"Warning: {len(changelog)} chars exceeds Google Play's 500-char limit.")

    if args.dry_run:
        print("[dry-run] No changes written.")
        return 0
    if input("Proceed (write files, commit, and tag)? [y/N] ").strip().lower() not in {"y", "yes"}:
        print("Cancelled.")
        return 0

    # Commit must precede tagging so the tag points to the updated version/changelog commit.
    GRADLE.write_text(updated, encoding="utf-8")
    (ROOT / changelog_rel).write_text(f"{changelog}\n", encoding="utf-8")
    git("add", "app/build.gradle.kts", str(changelog_rel))
    git("commit", "-m", commit_msg)
    git("tag", "-a", tag, "-m", f"Release {tag}")
    print(f"Committed and tagged {tag}.")

    # Push branch and tag atomically so Release.yml sees the tag pointing to origin/<branch> tip.
    if input(f"Push {branch} and {tag} to origin? [y/N] ").strip().lower() in {"y", "yes"}:
        git("push", "--atomic", "origin", branch, tag)
        print(f"Pushed {branch} and {tag}.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except KeyboardInterrupt:
        print("\nCancelled.")
        raise SystemExit(130)
