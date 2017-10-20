#!/usr/bin/env sh

run() {
    if [ "x$1" == "x" ]; then
        echo -e "Missing the url argument" >&2
        return 1
    fi
    # return total time in seconds
    curl -so /dev/null --connect-timeout 1 -m 10 -w "scale=5;\n%{time_starttransfer}-%{time_connect};\n" $1 | bc
}

avg() {
    declare -a arr=("${!1}")
    math="scale=5; (0"
    for item in ${arr[@]}; do math="$math + $item"; done
    math="$math) / ${#arr[@]}"
    echo $math | bc
}
