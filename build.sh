#!/bin/bash

set -e

optClean=
optStage=1
optFormat=
optSite=

shortOptions=c1h
longOptions=format,site

options=("$(getopt -u -o $shortOptions --long $longOptions -- $@)")

function processOptions
{
	while [ $# -gt 0 ]
	do
		case $1 in
			-c) optClean=1;;
			-1) optStage=1;;
			--format) optFormat=1;;
			--site) optSite=1;;
			-h) printf "Usage: %s: [options]\n" $0
			    printf "Options:\n"
			    printf " -c       Perform a clean build\n"
			    printf " -1       Start from stage 1\n"
			    printf " --format Format the source code\n"
			    printf " --site   Update the project web site\n"
			    printf " -h       Print this help\n"
			    exit 0
			    ;;
		esac
		shift
	done
}

processOptions $options

shift $(($OPTIND - 1))

if [ ! -z "$optFormat" ]
then
	mvn java-formatter:format
	mvn license:format
	exit 0
fi

if [ ! -z "$optSite" ]
then
	mvn site:site
	mvn site:deploy
	exit 0
fi

if [ ! -z "$optClean" ]
then
	mvn clean
fi

mvn install
