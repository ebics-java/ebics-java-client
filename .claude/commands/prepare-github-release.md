---
description: Draft and publish a GitHub release for the current pom version, with AI-written categorized notes covering merged PRs AND direct commits.
argument-hint: "[--draft] [version]"
allowed-tools: Bash(git:*), Bash(gh:*), Bash(./mvnw:*), Read, Write
---

Prepare a GitHub release for this repository (`ebics-java/ebics-java-client`). Follow these
steps. Do NOT create the tag or release until the user has approved the drafted notes.

## 1. Determine the version and previous tag
- Version to release: `./mvnw -q help:evaluate -Dexpression=project.version -DforceStdout`
  (or use an explicit version passed in `$ARGUMENTS`). The tag is the bare version, e.g. `2.1.0`
  (no `v` prefix — match existing tags). The release title is `EBICS Java Version <version>`.
- Previous released tag: the most recent existing tag by version, e.g.
  `git tag --sort=-v:refname | head -5` — pick the highest tag that is not the new version.

## 2. Gather the raw material (this is the whole point — cover EVERYTHING, not just PRs)
- All commits in range, including direct-to-master commits and merges:
  `git log <prev>..HEAD --pretty=format:'%h %s (%an)'`
- GitHub's PR-based data (for accurate PR links, @author attribution, new contributors, and the
  compare URL):
  `gh api repos/ebics-java/ebics-java-client/releases/generate-notes -f tag_name=<version> -f previous_tag_name=<prev> -f target_commitish=master --jq '.body'`

## 3. Draft the notes
Write clean Markdown release notes that:
- Group changes under these headings (omit any that are empty):
  `## New Features & Enhancements`, `## Bug Fixes`, `## Dependency Updates`, `## Other Changes`.
- Reword terse commit subjects into clear, user-facing bullets (e.g. drop `feat(cli):` prefixes,
  make them read as human sentences). Do not invent changes — every bullet must trace to a real
  commit or PR.
- Include direct-to-master commits that have no PR, alongside the PR-based entries.
- Preserve PR links and `@author` attribution where a change came from a PR.
- Keep a `## New Contributors` section and the `**Full Changelog**: <compare-url>` line from the
  generated data if present.
- Lead with a one or two sentence summary of the release if there's a notable theme.

## 4. Review with the user
Show the full drafted notes in the chat and ask the user to approve or request edits. STOP here
until they approve. Iterate on wording if asked.

## 5. Publish (only after approval)
- Confirm local `HEAD` is pushed to `origin/master` (`git rev-parse HEAD` == `git rev-parse origin/master`); warn if not.
- Create the tag if it does not exist, then push it:
  `git tag <version>` (skip if it exists) and `git push origin <version>`.
- Write the approved notes to a temp file in the scratchpad, then create the release:
  `gh release create <version> --title "EBICS Java Version <version>" --notes-file <file> --verify-tag`
- If `$ARGUMENTS` contains `--draft`, add `--draft` so the user can eyeball it on GitHub before
  it goes public.
- Report the release URL that `gh` prints.
