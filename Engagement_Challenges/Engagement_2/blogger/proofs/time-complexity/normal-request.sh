#!/usr/bin/env sh

source ./util.sh

str='http://localhost:8080/Index'

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
