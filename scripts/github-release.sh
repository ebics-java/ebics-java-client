#!/usr/bin/env bash
#
# Copyright Uwe Maurer
#
# Tag the current pom version and publish a GitHub release with an auto-generated changelog.
#
# GitHub builds the changelog from the pull requests merged since the previous tag; the
# grouping is configured in .github/release.yml. Run this AFTER the Maven Central release
# (scripts/release.sh) has been published, from a clean, pushed master.
#
# Usage:
#     scripts/github-release.sh              # tag <version>, push it, create the release
#     scripts/github-release.sh --draft      # extra flags are forwarded to `gh release create`
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

VERSION="$(./mvnw -q help:evaluate -Dexpression=project.version -DforceStdout)"
TAG="$VERSION"
TITLE="EBICS Java Version $VERSION"

echo "Releasing $TAG ($TITLE)"

# Create the tag locally if it does not exist yet, then push it.
if git rev-parse -q --verify "refs/tags/$TAG" >/dev/null; then
    echo "Tag $TAG already exists locally."
else
    git tag "$TAG"
fi
git push origin "$TAG"

# Create the GitHub release with notes generated from merged PRs since the previous tag.
gh release create "$TAG" --title "$TITLE" --generate-notes --verify-tag "$@"
