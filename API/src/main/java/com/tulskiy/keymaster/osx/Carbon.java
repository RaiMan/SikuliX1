/*
 * Copyright (c) 2011 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.keymaster.osx;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Denis Tulskiy
 * Date: 6/15/11
 */

public interface Carbon extends Library {
    public static com.tulskiy.keymaster.osx.Carbon Lib = (com.tulskiy.keymaster.osx.Carbon) Native.loadLibrary("Carbon", com.tulskiy.keymaster.osx.Carbon.class);

    public static final int cmdKey = 0x0100;
    public static final int shiftKey = 0x0200;
    public static final int optionKey = 0x0800;
    public static final int controlKey = 0x1000;

    public Pointer GetEventDispatcherTarget();

    /* OSStatus InstallEventHandler(EventTargetRef inTarget, EventHandlerUPP inHandler, ItemCount inNumTypes, const EventTypeSpec* inList, void* inUserData, EventHandlerRef *outRef) */
    public int InstallEventHandler(Pointer inTarget, EventHandlerProcPtr inHandler, int inNumTypes, EventTypeSpec[] inList, Pointer inUserData, PointerByReference outRef);

    public int RegisterEventHotKey(int inHotKeyCode, int inHotKeyModifiers, EventHotKeyID.ByValue inHotKeyID, Pointer inTarget, int inOptions, PointerByReference outRef);

    public int GetEventParameter(Pointer inEvent, int inName, int inDesiredType, Pointer outActualType, int inBufferSize, IntBuffer outActualSize, EventHotKeyID outData);

    public int RemoveEventHandler(Pointer inHandlerRef);

    public int UnregisterEventHotKey(Pointer inHotKey);

    /* struct EventTypeSpec { UInt32 eventClass; UInt32 eventKind; }; */
    public class EventTypeSpec extends Structure {
        public int eventClass;
        public int eventKind;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("eventClass", "eventKind");
        }
    }

    /* struct EventHotKeyID { OSType signature; UInt32 id; }; */
    public static class EventHotKeyID extends Structure {
        public int signature;
        public int id;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("signature", "id");
        }

        public static class ByValue extends EventHotKeyID implements Structure.ByValue {

        }
    }

    /* typedef OSStatus (*EventHandlerProcPtr) ( EventHandlerCallRef inHandlerCallRef, EventRef inEvent, void * inUserData ); */
    public static interface EventHandlerProcPtr extends Callback {
        @SuppressWarnings({"UnusedDeclaration"})
        public int callback(Pointer inHandlerCallRef, Pointer inEvent, Pointer inUserData);
    }
}