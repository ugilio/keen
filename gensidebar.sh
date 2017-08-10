#!/bin/bash
fname=$1
base="/"${fname%.*}

folderitems=""
last="####################"
#last=""
cat $fname | egrep '^\s*#+\s' | cut -c 2- | while read line
do
  hashes=`echo $line | perl -p -e 's/\s*(#+)\s.*$/$1/'`

  if [ ${#hashes} -gt ${#last} ] || [ "$last" == "#" ]
  then
    echo "$folderitems"
#Duplicate the entry (as a subentry) so that it is reachable in the accordion
    echo "${spc}  - title: $str"
    echo "${spc}    output: web"
    echo "${spc}    url: $base#$anchor"
  fi

  spc=`echo $hashes | sed 's/#/  /g'`
  str=`echo $line | perl -p -e 's/\s*#+\s*(.+)$/$1/'`
  anchor=`echo $str | perl -p -e 's/ +/-/g'| tr '[:upper:]' '[:lower:]' | tr -d -c '[:alnum:]-'`

  if [[ $str == *:* ]]
  then
    str="\"$str\""
  fi

  echo "${spc}- title: $str"
  echo "${spc}  output: web"
  echo "${spc}  url: $base#$anchor"

  last="$hashes"
  folderitems=`echo "${spc}  folderitems:"`
done

