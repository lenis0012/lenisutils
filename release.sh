#!/bin/bash

# Print error if uncommitted changes are found
if [[ -n $(git status --porcelain) ]]
then
    echo "Uncommitted changes found, please commit or stash them before releasing"
    exit 1
fi

# Get new version
CURRENT_VERSION=$(./git-semver latest)
NEXT_VERSION=$(./git-semver next)
echo "Getting ready to update from v$CURRENT_VERSION to v$NEXT_VERSION"
./git-semver log

# Ask for confirmation
read -p "Are you sure you want to release v$NEXT_VERSION? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    exit 1
fi

echo Updating maven version
mvn versions:set -DnewVersion=$NEXT_VERSION -DgenerateBackupPoms=false > /dev/null

echo Updating changelog
echo >> CHANGELOG.md
echo "## [$NEXT_VERSION](https://github.com/lenis0012/lenisutils/compare/v$CURRENT_VERSION...v$NEXT_VERSION)" >> CHANGELOG.md
(./git-semver log --markdown) >> CHANGELOG.md

echo Committing changes
git add -A
git commit -m "chore(release): release v$NEXT_VERSION"
git tag -a "v$NEXT_VERSION" -m "v$NEXT_VERSION"
git push --follow-tags origin master

echo "Waiting 60 seconds before bumping dev version"
sleep 60

DEV_VERSION=$(echo ${NEXT_VERSION} | awk -F. -v OFS=. '{$NF += 1 ; print}')-SNAPSHOT
echo "Bumping dev version to $DEV_VERSION"
mvn versions:set -DnewVersion=$DEV_VERSION -DgenerateBackupPoms=false > /dev/null
git add -A
git commit -m "chore(release): prepare for next development iteration [ci skip]"
git push origin master

echo "Done!"
