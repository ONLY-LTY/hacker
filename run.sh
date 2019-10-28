#!/usr/bin/env bash
jvm_args="-Xmx2048m -Xms2048m  -XX:SurvivorRatio=8  -XX:NewRatio=2"
exec java -jar ${jvm_args} hacker-shenmegui.jar $1
