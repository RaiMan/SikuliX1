RUNTIME = RunTime.get();

USEJSON = false;

jsonOn = function() {
	USEJSON = true;
	Debug.log(3, "JSON: Now returning JSONized objects");
};

jsonOff = function() {
	USEJSON = false;
	Debug.log(3, "JSON: Now returning normal objects");
};

isNull = function(aObj) {
	if (!JavaScriptSupport.isJSON(aObj)) {
		return aObj == null;
	}
	return JavaScriptSupport.fromJSON(aObj) == null;
};

json = function(aObj) {
	if (!JavaScriptSupport.isJSON(aObj)) {
		return aObj;
	}
	return JavaScriptSupport.fromJSON(aObj);
};

getArgsForJ = function(args) {
  var jargs;
  if (JavaScriptSupport.isNashorn()) {
    jargs = Java.to(args);
  } else {
    jargs = java.lang.reflect.Array.newInstance(java.lang.Object, args.length);
    for(n = 0; n < args.length; n++) {
      jargs[n] = args[n];
    }
  }
	return jargs;
};

makeRetVal = function(aObj) {
  JavaScriptSupport.restoreUsed();
  return makeRetValDo(aObj);
};

makeRetValDo = function(aObj) {
  if (!USEJSON) {
		return aObj;
	} else {
		try {
			return aObj.toJSON();
		} catch (ex) {
		}
		try {
			return "[\"" + aObj.getClass().getName() + ", \"" + aObj.toString + "\"]";
		} catch (ex) {
			return "[\"NULL\"]";
		}
	}
};

run = function() {
  if (arguments.length < 1) {
    return;
  }
  return makeRetValDo(JavaScriptSupport.call("run", getArgsForJ(arguments)));
}

use = function() {
  return makeRetValDo(JavaScriptSupport.call("use", getArgsForJ(arguments)));
};

use1 = function() {
  return makeRetValDo(JavaScriptSupport.call("use1", getArgsForJ(arguments)));
};

wait = function() {
  return makeRetVal(JavaScriptSupport.call("wait", getArgsForJ(arguments)));
};

waitVanish = function() {
  return makeRetVal(JavaScriptSupport.call("waitVanish", getArgsForJ(arguments)));
};

exists = function() {
  return makeRetVal(JavaScriptSupport.call("exists", getArgsForJ(arguments)));
};

click = function() {
  return makeRetVal(JavaScriptSupport.call("click", getArgsForJ(arguments)));
};

doubleClick = function() {
  return makeRetVal(JavaScriptSupport.call("doubleClick", getArgsForJ(arguments)));
};

rightClick = function() {
  return makeRetVal(JavaScriptSupport.call("rightClick", getArgsForJ(arguments)));
};

hover = function() {
  return makeRetVal(JavaScriptSupport.call("hover", getArgsForJ(arguments)));
};

type = function() {
  return makeRetVal(JavaScriptSupport.call("type", getArgsForJ(arguments)));
};

write = function() {
  return makeRetVal(JavaScriptSupport.call("write", getArgsForJ(arguments)));
};

paste = function() {
  return makeRetVal(JavaScriptSupport.call("paste", getArgsForJ(arguments)));
};

closeApp = function() {
	if (RunTime.get().runningMac) {
		write("#M.q");
	} else if (RunTime.get().runningWindows) {
		write("#A.#F4.");
	} else {
		write("#C.q");
	};
};

closeBrowserWindow = function() {
	if (RunTime.get().runningMac) {
		write("#M.w");
	} else {
		write("#C.w");
	};
};

circle = function() {
  return makeRetVal(JavaScriptSupport.call("circle", getArgsForJ(arguments)));
};

rectangle = function() {
  return makeRetVal(JavaScriptSupport.call("rectangle", getArgsForJ(arguments)));
};

text = function() {
  return makeRetVal(JavaScriptSupport.call("text", getArgsForJ(arguments)));
};

tooltip = function() {
  return makeRetVal(JavaScriptSupport.call("tooltip", getArgsForJ(arguments)));
};

flag = function() {
  return makeRetVal(JavaScriptSupport.call("flag", getArgsForJ(arguments)));
};

callout = function() {
  return makeRetVal(JavaScriptSupport.call("callout", getArgsForJ(arguments)));
};

image = function() {
  return makeRetVal(JavaScriptSupport.call("image", getArgsForJ(arguments)));
};

arrow = function() {
  return makeRetVal(JavaScriptSupport.call("arrow", getArgsForJ(arguments)));
};

bracket = function() {
  return makeRetVal(JavaScriptSupport.call("bracket", getArgsForJ(arguments)));
};

button = function() {
  return makeRetVal(JavaScriptSupport.call("button", getArgsForJ(arguments)));
};

