# Copyright 2010-2014, Sikuli.org, sikulix.com
# Released under the MIT License.
from org.sikuli.basics import HotkeyListener
from org.sikuli.basics import HotkeyManager
from org.sikuli.script import Env as JEnv

class Env(JEnv):

    @classmethod
    def addHotkey(cls, key, modifiers, handler):
        class AnonyListener(HotkeyListener):
            def hotkeyPressed(self, event):
                handler(event)
        return HotkeyManager.getInstance().addHotkey(key, modifiers, AnonyListener())


