#!/bin/bash

class=$( find -regex ".*$1\.java" | cut -d'.' -f2 | tr '/' '.' | cut -c2- )
echo "Executing $class"
java $class
