var last = "Initial";
var e = document.getElementById("container");
var interval = setInterval(function () {
    xhr = new XMLHttpRequest();
    xhr.open("POST", "/stac.example.Next", false);
    xhr.send(last);
    last = xhr.responseText;
    if (last === "Halt") {
        clearInterval(interval);
    } else {
        e.innerHTML += "<b> " + last + " </b>";
    }
}, 600);
