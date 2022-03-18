import Carbon

let command = ProcessInfo.processInfo.arguments.dropFirst().last ?? ""
let filter = command == "list" ? nil : [kTISPropertyInputSourceID: command]

guard let cfSources = TISCreateInputSourceList(filter as CFDictionary?, false),
      let sources = cfSources.takeRetainedValue() as? [TISInputSource] else { exit(-1) }

if filter == nil { // Print all sources
    sources.forEach {
        let cfID = TISGetInputSourceProperty($0, kTISPropertyInputSourceID)!
        print(Unmanaged<CFString>.fromOpaque(cfID).takeUnretainedValue() as String)
    }
    let keyboard = TISCopyCurrentKeyboardInputSource().takeRetainedValue()
    let keyboardString = String(describing: keyboard)
    let range = keyboardString.range(of: "KB Layout: ", options: .literal, range: keyboardString.startIndex..<keyboardString.endIndex)!
    let startingKeyboard = range.upperBound
    let theKeyboardLayout = keyboardString[startingKeyboard ..< keyboardString.endIndex]
    print(theKeyboardLayout)
} else if let firstSource = sources.first { // Select this source
    exit(TISSelectInputSource(firstSource))
}