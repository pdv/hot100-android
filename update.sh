#!/bin/bash

db_path=app/src/main/assets/hot100.db
csv_file=$(mktemp)

rm ${db_path}

curl https://raw.githubusercontent.com/utdata/rwd-billboard-data/main/data-out/hot-100-current.csv -o ${csv_file}

sqlite-utils insert ${db_path} hot100 ${csv_file} --csv --detect-types
sqlite-utils transform ${db_path} hot100 --pk id

