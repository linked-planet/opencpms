#!/bin/bash
set -e

# example: test-data-generator.sh
# Requires json-schema_file-faker-cli (https://github.com/oprogramador/json-schema_file-faker-cli)
# Install via `npm install -g json-schema_file-faker-cli`

cd "$(dirname "$0")"

num_of_files=5 # number of files to create for every schema_file
src_dir="../../ocpp16-protocol/src/main/json" # location of the schema_file files
src_dir_files="$src_dir/*" # location of the schema_file files
target_dir="src/test/resources/generated" # location of the generated json objects

read -p "Are you sure you want to delete old test data and regenerate it? [y/n]" -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
  rm -r "$target_dir"
  mkdir "$target_dir"
  for schema_path in $src_dir_files
  do
    schema_file=$(basename "$schema_path")
    echo "Processing schema_file $schema_file ..."
    for ((i=1;i<=num_of_files;i++));
    do
      file_name_without_ignored_suffix="${schema_file%.json.ignored}"
      file_name_without_extension="${file_name_without_ignored_suffix%.*}"
      target_file="$target_dir/$file_name_without_extension-$i.json"
      echo "-> $target_file"
       generate-json "$src_dir/$schema_file" "$target_file"
    done
  done
else
  echo "Skipped"
fi
