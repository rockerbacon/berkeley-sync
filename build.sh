#!/bin/bash

file=$( find -regex ".*$1\.java" )
echo "Building $file"
javac $file
