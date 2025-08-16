#!/bin/bash
root="/app"
gnu_file="./input/v1.0.gnucash"
info_file="./output/info.txt"
log_file="./output/log.txt"
current_version_file="./output/current_app_version.txt"
new_version_file="./app_version.txt"
jar_file="app.jar"
jar_url="https://github.com/ramitsuri/gnucash-kt/releases/latest/download/app.jar"
version_url="https://github.com/ramitsuri/gnucash-kt/releases/latest/download/app_version.txt"

# This function logs a message with a timestamp
write_log() {
  local message="$1"
  local timestamp=$(date +"%Y-%m-%dT%H:%M:%S%z")
  echo "$timestamp $message" >> "$log_file"
  echo "$timestamp $message"
}

# This function logs an error and exits the script
error_exit() {
  local message="$1"
  write_log "ERROR: $message"
  write_log "-----------"
  exit 1
}

cd "$root" || error_exit "Failed to change directory to $root"

if [ ! -f "$info_file" ]; then
  echo "last_run_time=0" > "$info_file"
  write_log "Created new info file."
fi

if [ ! -f "$log_file" ]; then
  touch "$log_file"
  write_log "Created new log file."
fi

if [ ! -f "$current_version_file" ]; then
  echo "1.0" > "$current_version_file"
  write_log "Created new app version file."
fi

if [ ! -f "$gnu_file" ]; then
  error_exit "Input file $gnu_file not found. Exiting."
fi

gnu_file_modified_time=$(date -ur "$gnu_file" +%s || error_exit "Failed to get file modification time.")

last_run_time=$(grep -Po "(?<=^last_run_time=).*" "$info_file" || error_exit "Failed to read last run time from $info_file.")

difference=$((last_run_time-gnu_file_modified_time))
if [[ $difference -ge 0 ]]
then
  write_log "Already ran at $last_run_time, modified: $gnu_file_modified_time"
  write_log "-----------"
  exit 0
fi

current_version=$(cat "$current_version_file" || error_exit "Failed to read current app version from $current_version_file.")
current_version_code="${current_version//.}"
current_version_code=$((current_version_code))
if [[ "$current_version_code" -eq 0 ]]; then
  error_exit "Failed to get current app version"
fi

if [ ! -f "$jar_file" ]; then
  write_log "JAR file not found. Downloading the latest version."
  wget "$jar_url" -O "$jar_file" || error_exit "Failed to download JAR file."
fi

write_log "Checking for new app version..."
wget "$version_url" -O "$new_version_file" || error_exit "Failed to download new app version file."
new_version=$(cat "$new_version_file")
new_version_code="${new_version//.}"
new_version_code=$((new_version_code))
if [[ "$new_version_code" -eq 0 ]]; then
  error_exit "Failed to get a valid new app version code."
fi

if [[ "$new_version_code" -gt "$current_version_code" ]]
then
  write_log "New app version ($new_version) found. Updating..."
  rm "$jar_file" || write_log "Could not remove old jar file, continuing..."
  wget "$jar_url" -O "$jar_file" || error_exit "Failed to download new app JAR file."
  rm "$current_version_file" || write_exit "Could not remove old version file, continuing..."
  mv "$new_version_file" "$current_version_file" || error_exit "Failed to move new version file."
else
  write_log "App is up to date ($current_version)."
  rm "$new_version_file" || write_log "Could not remove new version file, continuing..."
fi

write_log "Running..."
java -jar "$jar_file" || error_exit "Failed to run Java application. Jar file might be invalid or corrupt."

current_time=$(date +%s)
echo "last_run_time=$current_time" > "$info_file" || error_exit "Failed to update info file."

write_log "Completed. New last run time: $current_time"
write_log "-----------"