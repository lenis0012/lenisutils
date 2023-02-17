#!/bin/bash

CURRENT_VERSION=$(./git-semver latest)
NEXT_VERSION=$(./git-semver next)
echo "Getting ready to update from v$CURRENT_VERSION to v$NEXT_VERSION"
echo $(./git-semver log)

# Ask for confirmation
read -p "Are you sure you want to release v$NEXT_VERSION? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    exit 1
fi

CHANGES=$(./git-semver log --markdown)
echo "TEST"

#npx @dwmkerr/standard-version --packageFiles pom.xml --bumpFiles pom.xml
#git push --follow-tags origin master
#
#echo "Waiting 30 seconds before bumping dev version"
#sleep 30
#
#mvn versions:set -DnewSnapshotVersion=true -DgenerateBackupPoms=false