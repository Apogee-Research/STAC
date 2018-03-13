#!/bin/bash

#curl -s -X POST "http://127.0.0.1:8988/gdoc.cgi?name=Sitemap.xml"
echo -e $( curl -s -X POST "http://127.0.0.1:8988/gdoc.cgi?name=Sitemap.xml" | sed 's/\/n/\\n/g' ) 
