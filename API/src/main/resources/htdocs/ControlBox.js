var serverAddress = location.protocol + "//" + location.host;

document.querySelector("button#start").addEventListener("click", initCmd);
document.querySelector("button#startp").addEventListener("click", initCmd);
document.querySelector("button#scripts").addEventListener("click", setCmd);
document.querySelector("button#images").addEventListener("click", setCmd);
document.querySelector("button#run").addEventListener("click", runCmd);
document.querySelector("button#stop").addEventListener("click", stopCmd);

function initCmd(e) {
  var command = e.target.id;
  var url = encodeURI(serverAddress + "/" + command);
  outputToMonitor(true, command + ": GET " + url);
  ajax("GET", url);
}

function setCmd(e) {
  var folderPath = document.querySelector("input#folder-path").value;
  if (folderPath === null || folderPath.trim() == "") {
    alert("Input the folder path.");
    return false;
  }

  var command = e.target.id;
  var url = encodeURI(serverAddress + "/" + command + "/" + folderPath.replace(/\\|:\\/gi, "/"));
  outputToMonitor(true, command + ": GET " + url);
  ajax("GET", url);
}

function runCmd(e) {
  var scriptName = document.querySelector("input#script-name").value;
  if (scriptName === null || scriptName.trim() == "") {
    alert("Input the script name.");
    return false;
  }
  var scriptArgs = document.querySelector("textarea#script-args").value;

  var command = e.target.id;
  var url = (scriptArgs !== null && scriptArgs.trim() != "") ? (scriptName + "?" + scriptArgs) : scriptName;
  url = encodeURI(serverAddress + "/" + command + "/" + url);
  outputToMonitor(true, command + ": GET " + url);
  ajax("GET", url);
}

function stopCmd(e) {
  var command = e.target.id;
  var url = encodeURI(serverAddress + "/" + command);
  outputToMonitor(true, command + ": GET " + url);
  ajax("GET", url);
}

function ajax(method, url, body) {
  switchButtonsDisabled(true);

  var xhr = new XMLHttpRequest();
  xhr.addEventListener("load", xhrEventListener);
  xhr.addEventListener("timeout", xhrEventListener);
  xhr.addEventListener("error", xhrEventListener);
  xhr.addEventListener("abort", xhrEventListener);
  xhr.open(method, url);
  xhr.setRequestHeader("If-Modified-Since", "Thu, 01 Jun 1970 00:00:00 GMT");
  xhr.send(body);
}

function xhrEventListener(e) {
  if (e.type == "load") {
    var message = String(this.responseText);
    if (message.indexOf("PASS") == 0) {
      message = '<span class="font-pass">PASS</span>' + message.substring(4);
    } else if (message.indexOf("FAIL") == 0) {
      message = '<span class="font-fail">FAIL</span>' + message.substring(4);
    }
    outputToMonitor(false, message);
  } else if(e.type == "timeout") {
    outputToMonitor(false, '<span class="font-fail">Request timed out</span>');
  } else {
    outputToMonitor(false, '<span class="font-fail">Can\'t be reached the server</span>');
  }

  switchButtonsDisabled(false);
}

function switchButtonsDisabled(toDisable) {
  var buttons = document.querySelectorAll("button");
  for (var i=0, len=buttons.length; i<len; i++) {
    if (!buttons[i].hasAttribute("disabled")) {
      if (toDisable)
        buttons[i].setAttribute("disabled", "disabled");
    } else {
      if (!toDisable)
        buttons[i].removeAttribute("disabled");
    }
  }
}

function outputToMonitor(isRequest, message) {
  var monitor = document.querySelector("#monitor pre");

  var beforeScrollTop = monitor.scrollTop;
  var beforeScrollTopMax = monitor.scrollHeight-monitor.clientHeight;

  var padding = monitor.querySelector("p#padding");
  if (padding !== null) {
    monitor.removeChild(padding);
  }
  var element = document.createElement(isRequest?"kbd":"samp");
  element.innerHTML = message + "<br>";
  monitor.appendChild(element);
  padding = document.createElement("p");
  padding.id = "padding";
  padding.innerHTML = "<br>";
  monitor.appendChild(padding);

  // scrolling
  if (((monitor.scrollHeight-monitor.clientHeight) > 0) && (beforeScrollTop == beforeScrollTopMax)) {
    monitor.scrollTop = monitor.scrollHeight-monitor.clientHeight;
  }
}