#!/bin/bash

npx @dwmkerr/standard-version --packageFiles pom.xml --bumpFiles pom.xml
git push --follow-tags origin master

