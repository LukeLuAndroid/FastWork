#!/bin/sh
echo "start build project"
work_path=$(cd `dirname $0`; pwd)
${work_path}/gradlew assembleDevDebug --offline

read -p "Press any key to continue." var
echo "build project end"