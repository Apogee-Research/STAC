#!/usr/bin/env phantomjs

var BASE_URL = 'http://localhost:8989';
var QUERY_URL =
      BASE_URL
      + '/tour?point=Cambridge&point=Lowell&point=Worcester&point=Springfield'
      + '&progress=true';

console.log(QUERY_URL);

var evtSource = new EventSource(QUERY_URL);

evtSource.addEventListener('progress', function(e) {
  var obj = JSON.parse(e.data);
  console.log("progress:", JSON.stringify(obj));
});

evtSource.addEventListener('result', function(e) {
  var obj = JSON.parse(e.data);
  console.log("result:", JSON.stringify(obj));
  evtSource.close();
  phantom.exit();
});
