#!/bin/bash

npx @dwmkerr/standard-version --packageFiles pom.xml --bumpFiles pom.xml
git push --follow-tags origin master

echo "Waiting 30 seconds before bumping dev version"
sleep 30

mvn versions:set -DnewSnapshotVersion=true -DgenerateBackupPoms=false