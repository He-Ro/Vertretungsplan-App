#!/bin/bash
cd /home/hendrik/Projekte/eclipseWorkspace/Vertretungsplan/proguard/
echo -n "Version: "
read version
tar -cf proguard-$version.tar *.txt