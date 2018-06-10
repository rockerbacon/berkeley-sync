#!/bin/bash

remove=$( find -regex ".*\.class" )
rm $remove
