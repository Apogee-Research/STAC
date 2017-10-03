#!/usr/bin/env sh

source ./util.sh

str='http://localhost:8080/this/example/Doesnt/Exist'

results=(
$(run $str)
$(run $str)
$(run $str)
$(run $str)
$(run $str)
$(run $str)
$(run $str)
$(run $str)
$(run $str)
$(run $str)
)

echo "Average Time in ${results[@]}: $(avg "results[@]")"
