#!/usr/bin/env bash
set -euo pipefail

checkout_root="${LINEAGE_BUILD_LOGIC_ROOT:-..}"

checkout_pinned() {
    local repository="$1"
    local branch="$2"
    local revision="$3"
    local directory="$4"

    if [[ -e "$directory" ]]; then
        printf 'Checkout path already exists: %s\n' "$directory" >&2
        exit 1
    fi

    git clone --quiet --depth 1 --branch "$branch" \
        "https://github.com/${repository}.git" "$directory"
    local actual_revision
    actual_revision="$(git -C "$directory" rev-parse HEAD)"
    if [[ "$actual_revision" != "$revision" ]]; then
        printf 'Unexpected revision for %s: expected %s, got %s\n' \
            "$repository" "$revision" "$actual_revision" >&2
        exit 1
    fi
}

checkout_pinned \
    flowdigger/gradle-lineage-plugin \
    feature/1.0.2 \
    f35f7ee9ef043ea426ec2a47a58531660855a2b7 \
    "$checkout_root/gradle-lineage-plugin"
checkout_pinned \
    flowdigger/flowdigger-gradle \
    main \
    0710c3735f1af07f92225ee5b2757ee5d8d996e0 \
    "$checkout_root/flowdigger-gradle"
