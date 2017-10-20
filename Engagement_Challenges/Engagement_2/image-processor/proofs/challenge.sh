#!/usr/bin/env bash

. ./functions.sh

case "$1" in
        clean)
            clean
            ;;
        default-tags)
            initialize
            ;;
        list)
            list
            ;;
        tag)
            shift; run train "$@"
            ;;
        classify)
            shift; run cluster "$@"
            ;;
        *)
            cat <<EOF
Usage: ./challenge.sh clean|initialize|list|tag|classify
    clean:
        Delete tag database.
    default-tags:
        Apply default tags to all images.
    list:
        List images that can be tagged.
    tag <image> <tag>:
        Tag an image manually.
    classify <image>:
        Classify a user-provided image.
EOF
            ;;
esac
