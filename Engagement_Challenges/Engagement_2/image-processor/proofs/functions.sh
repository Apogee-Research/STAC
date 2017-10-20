IMAGES=/var/lib/trainer/images

addAll() {
    for file in $(ls "${IMAGES}/${1}"); do
        echo "Tagging $1/${file} as '$2'"
        java -jar ipchallenge-0.1.jar train "$1/$file" $2
    done
}

clean() {
    echo "Cleaning up tags"
    rm -rf ~/.imageClustering
}

initialize() {
    echo "Setting default tags"
    addAll red red
    addAll blue blue
}

list() {
    (cd "${IMAGES}" && find . -name '*.jpg' | sed 's,^[.]/,,' | sort)
}

run() {
    time java -Xint -jar ipchallenge-0.1.jar "$@"
}
