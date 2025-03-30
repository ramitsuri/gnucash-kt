#!/bin/bash
write_log()
{
  message=$1
  timestamp=$(date +"%Y-%m-%dT%H:%M:%S%z")
  echo "$timestamp $message" >> log.txt
  echo "$timestamp $message"
}

main_dir="$HOME/usbdrv/gnucash"
gnu_file="$HOME/usbdrv/gnucash/data/v1.0/v1.0.gnucash"
gnu_file_modified_time=$(date -ur "$gnu_file" +%s)

cd "$main_dir" || exit
info_file="info.txt"
last_run_time=$(grep -Po "(?<=^last_run_time=).*" $info_file)

timestamp=$(date +"%Y-%m-%dT%H:%M:%S%z")
difference=$(($last_run_time-$gnu_file_modified_time))
if [[ $difference =~ ^[0-9]+$ ]] # contains numbers only (positive = last run greater than modified)
then
  write_log "Already ran at $last_run_time, modified: $gnu_file_modified_time"
  write_log "-----------"
  exit
fi

# Check if new version needs to be downloaded
cd "$HOME"/usbdrv/gnucash/gnucash-kt || exit

current_version=$(cat current_app_version.txt)
current_version_code="${current_version//.}"
current_version_code="$((current_version_code))"
if [[ $current_version_code == 0 ]]
then
  echo "Failed to get current app version"
  write_log "-----------"
  exit
fi

wget https://github.com/ramitsuri/gnucash-kt/releases/latest/download/app_version.txt
new_version=$(cat app_version.txt) || exit
new_version_code="${new_version//.}"
new_version_code="$((new_version_code))"
if [[ $new_version_code == 0 ]]
then
  write_log "Failed to get new app version"
  write_log "-----------"
  exit
fi

if [[ $new_version_code -gt $current_version_code ]]
then
  rm app.jar
  wget https://github.com/ramitsuri/gnucash-kt/releases/latest/download/app.jar
  rm current_version.txt
  mv app_version.txt current_version.txt
else
  rm app_version.txt
fi

java -jar app.jar
cp -r "$HOME"/usbdrv/gnucash/gnucash-kt/output/*  "$HOME"/usbdrv/gnucash/reports/json-v2/

cd "$main_dir" || exit
current_time=$(date +%s)
echo "last_run_time=$current_time" > $info_file

timestamp=$(date +"%Y-%m-%dT%H:%M:%S%z")
write_log "Completed. Last ran: $last_run_time, modified: $gnu_file_modified_time"
write_log "-----------"
