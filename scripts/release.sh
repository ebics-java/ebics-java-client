#!/usr/bin/env bash
#
# Copyright Uwe Maurer
#
# Publish ebics-java-client to Maven Central (Sonatype Central Portal).
#
# Production credentials live OUTSIDE this repo in a Gradle-format properties file.
# This script reads them, exposes them to Maven as environment variables, and runs the
# `release` profile (Javadoc jar + GPG signing + Central publishing).
#
# A credential-free local build needs none of this -- just run:
#     ./mvnw clean install
#
# Usage:
#     scripts/release.sh                 # stage a deployment on the Central Portal
#     scripts/release.sh -DskipTests     # extra args are forwarded to Maven
#
# Credentials are read from `release.properties` in the repo root, which is git-ignored.
# Create it as a symlink to your out-of-repo credentials file, e.g.:
#     ln -s ~/secret/maven-central-gradle.properties release.properties
#
# Override the location with:
#     MAVEN_CENTRAL_CREDENTIALS=/path/to/file scripts/release.sh
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CREDS="${MAVEN_CENTRAL_CREDENTIALS:-$REPO_ROOT/release.properties}"

if [[ ! -f "$CREDS" ]]; then
    echo "error: credentials file not found: $CREDS" >&2
    echo "       set MAVEN_CENTRAL_CREDENTIALS to point at it, or build locally without" >&2
    echo "       publishing using:  ./mvnw clean install" >&2
    exit 1
fi

# Read one property value from the Gradle-style (key=value) credentials file.
prop() {
    local value
    value="$(grep -E "^$1=" "$CREDS" | head -1 | cut -d= -f2-)"
    if [[ -z "$value" ]]; then
        echo "error: property '$1' missing or empty in $CREDS" >&2
        exit 1
    fi
    printf '%s' "$value"
}

# Central Portal token -> consumed by release-settings.xml (<server id="central">).
export CENTRAL_USERNAME="$(prop mavenCentralUsername)"
export CENTRAL_PASSWORD="$(prop mavenCentralPassword)"

# GPG signing -> consumed by the maven-gpg-plugin BouncyCastle signer.
export MAVEN_GPG_PASSPHRASE="$(prop signingInMemoryKeyPassword)"
# The signing key is stored on a single line with \n-escaped newlines (Gradle in-memory
# format); restore real newlines so the PGP armor parses.
export MAVEN_GPG_KEY="$(prop signingInMemoryKey | sed 's/\\n/\
/g')"

cd "$REPO_ROOT"
exec ./mvnw -Prelease -s release-settings.xml clean deploy "$@"
