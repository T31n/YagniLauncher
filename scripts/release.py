#!/usr/bin/env python3
"""Interactive release helper for YagniLauncher.

Usage:
    ./scripts/release.py                   # Auto-bump patch, enter changelog, preview, and release
    ./scripts/release.py --name 1.2.0      # Override suggested versionName
    ./scripts/release.py --editor          # Write changelog in $VISUAL/$EDITOR (or git core.editor)
    ./scripts/release.py --dry-run         # Preview only without modifying files or git state

Workflow:
    1. Verifies the default branch, clean tracked working tree, and remote sync with origin.
    2. Computes next versionCode / versionName (matching .github/workflows/Release.yml).
    3. Prints commits since the latest tag and reads Fastlane changelog lines.
    4. Shows a full preview (Gradle diff, Fastlane file, commit, tag) and asks to proceed.
    5. Writes files, commits, creates an annotated tag, and optionally pushes atomically.
"""

import argparse
import difflib
import os
import re
import shlex
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(subprocess.check_output(["git", "rev-parse", "--show-toplevel"], text=True).strip())
GRADLE = ROOT / "app/build.gradle.kts"
# Must match the tag validation regex in .github/workflows/Release.yml (without leading 'v').
NAME_RE = re.compile(r"^\d+\.\d+\.\d+(-[0-9A-Za-z.]+)?$")


def git(*args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    """Run a git command from the repository root."""
    return subprocess.run(["git", *args], cwd=ROOT, text=True, capture_output=True, check=check)


def local_default_branch() -> str:
    """Return origin's locally cached default branch name, or empty string if unknown."""
    origin_head = git("symbolic-ref", "--short", "refs/remotes/origin/HEAD", check=False).stdout.strip()
    return origin_head.removeprefix("origin/") if origin_head.startswith("origin/") else ""


def verify_remote_sync(branch: str, tag: str) -> None:
    """Verify origin's default branch, remote tag availability, and that HEAD includes origin/<branch>."""
    default_branch = local_default_branch()
    remote_branch_sha = ""
    remote_tag_exists = False

    remote = git(
        "ls-remote",
        "--symref",
        "origin",
        "HEAD",
        f"refs/heads/{branch}",
        f"refs/tags/{tag}",
        check=False,
    )
    if remote.returncode == 0:
        for line in remote.stdout.splitlines():
            left, _, ref = line.partition("\t")
            if ref == "HEAD" and left.startswith("ref: refs/heads/"):
                default_branch = left.removeprefix("ref: refs/heads/")
            elif ref == f"refs/heads/{branch}":
                remote_branch_sha = left
            elif ref == f"refs/tags/{tag}":
                remote_tag_exists = True
    else:
        print("Warning: could not reach 'origin'; checking local tracking refs only.")
        remote_branch_sha = git(
            "rev-parse", "--verify", f"refs/remotes/origin/{branch}", check=False
        ).stdout.strip()

    if default_branch and branch != default_branch:
        sys.exit(f"Error: on branch '{branch}', but Release.yml requires releasing from '{default_branch}'.")
    if remote_tag_exists:
        sys.exit(f"Error: tag '{tag}' already exists on origin.")
    if remote_branch_sha and git("merge-base", "--is-ancestor", remote_branch_sha, "HEAD", check=False).returncode != 0:
        sys.exit(
            f"Error: local '{branch}' is behind or diverged from 'origin/{branch}'. Pull/rebase before releasing."
        )


def read_changelog_inline() -> str:
    """Collect multi-line Fastlane changelog entries from stdin until an empty line or EOF."""
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
    return "\n".join(lines).strip()


def read_changelog_from_editor(new_name: str, new_code: int, last_tag: str, commits: str) -> str:
    """Open $VISUAL/$EDITOR (falling back to git's configured editor) to write the changelog."""
    editor = (
        os.environ.get("VISUAL")
        or os.environ.get("EDITOR")
        or git("var", "GIT_EDITOR", check=False).stdout.strip()
        or "vi"
    )
    commit_lines = "\n".join(f"# {line}" for line in commits.splitlines()) if commits else "# (none)"
    template = (
        f"\n"
        f"# Enter Fastlane changelog for {new_name} ({new_code}).\n"
        f"# Lines starting with '#' are ignored. Save and close to continue.\n"
        f"#\n"
        f"# Commits since {last_tag or 'start'}:\n"
        f"{commit_lines}\n"
    )
    with tempfile.NamedTemporaryFile(
        mode="w",
        suffix=".txt",
        prefix=f"changelog-{new_code}-",
        encoding="utf-8",
        delete=False,
    ) as tmp:
        tmp.write(template)
        tmp_path = Path(tmp.name)

    try:
        result = subprocess.run([*shlex.split(editor), str(tmp_path)])
        if result.returncode != 0:
            sys.exit(f"Error: editor '{editor}' exited with status {result.returncode}.")
        content = tmp_path.read_text(encoding="utf-8")
    finally:
        tmp_path.unlink(missing_ok=True)

    return "\n".join(line for line in content.splitlines() if not line.startswith("#")).strip()


def main() -> int:
    """Run the interactive release flow and return a process exit code."""
    parser = argparse.ArgumentParser(description="Bump version, write Fastlane changelog, commit, and tag.")
    parser.add_argument("--code", type=int, help="Override next versionCode (default: current + 1).")
    parser.add_argument("--name", help="Override next versionName (default: bump patch).")
    parser.add_argument(
        "-e",
        "--editor",
        action="store_true",
        help="Write the Fastlane changelog in $VISUAL/$EDITOR (or git core.editor).",
    )
    parser.add_argument("--dry-run", action="store_true", help="Preview without modifying files or git state.")
    args = parser.parse_args()

    # Guard against detached HEAD, non-default branches (required by Release.yml), and dirty tracked files.
    branch = git("branch", "--show-current").stdout.strip()
    if not branch:
        sys.exit("Error: detached HEAD. Checkout the default branch before releasing.")
    default_branch = local_default_branch()
    if default_branch and branch != default_branch:
        sys.exit(f"Error: on branch '{branch}', but Release.yml requires releasing from '{default_branch}'.")
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
    verify_remote_sync(branch, tag)

    # Show commit history since the latest tag to help write release notes.
    last_tag = git("describe", "--tags", "--abbrev=0", check=False).stdout.strip()
    log_range = f"{last_tag}..HEAD" if last_tag else "-n 20"
    commits = git("log", log_range, "--oneline", "--no-decorate").stdout.strip()
    print(f"Current: {name} ({code}) -> Next: {new_name} ({new_code}) on {branch}")
    print(f"\nCommits since {last_tag or 'start'}:")
    print(commits or "(none)")
    if last_tag and not commits:
        print(f"Warning: no new commits since {last_tag}.")

    changelog = (
        read_changelog_from_editor(new_name, new_code, last_tag, commits)
        if args.editor
        else read_changelog_inline()
    )
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
