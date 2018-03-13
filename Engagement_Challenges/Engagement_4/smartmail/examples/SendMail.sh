#!/bin/bash
curl -X POST "http://127.0.0.1:8988/email.cgi?from=me@smartmail.com&to=nick@smartmail.com;linda@smartmail.com&subj=Subject&content=somedata"
