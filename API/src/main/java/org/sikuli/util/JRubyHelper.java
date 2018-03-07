/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import org.sikuli.basics.Debug;

/**
 * This class implements JRuby specific parts
 */
public class JRubyHelper implements JLangHelperInterface {
    private static final String me = "JRubyHelper: ";

    /**
     * Mandatory method which returns an instance of the helper
     *
     * @return
     */
    public static JRubyHelper get() {
        return new JRubyHelper();
    }

    @Override
    public boolean runObserveCallback(Object[] args) {
        boolean result = false;
        Object callback = args[0];
        Object e = args[1];
        try {
            Class<?> rubyProcClass = callback.getClass();
            Method getRuntime = rubyProcClass.getMethod("getRuntime", new Class<?>[0]);
            Object runtime = getRuntime.invoke(callback, new Object[0]);
            Class<?> runtimeClass = getRuntime.getReturnType();

            Method getCurrentContext = runtimeClass.getMethod("getCurrentContext", new Class<?>[0]);
            Object context = getCurrentContext.invoke(runtime, new Object[0]);

            Class<?> jrubyUtil = Class.forName("org.jruby.javasupport.JavaUtil");
            Method convertJavaToRuby = jrubyUtil.getMethod("convertJavaToRuby",
                    new Class<?>[] { runtimeClass, Object.class });

            Object paramForRuby = convertJavaToRuby.invoke(null, new Object[] { runtime, e });

            Object iRubyObject = Array.newInstance(Class.forName("org.jruby.runtime.builtin.IRubyObject"), 1);
            Array.set(iRubyObject, 0, paramForRuby);

            Method call = rubyProcClass.getMethod("call",
                    new Class<?>[] { context.getClass(), iRubyObject.getClass() });
            call.invoke(callback, new Object[] { context, iRubyObject });
            result = true;
        } catch (Exception ex) {
            String msg = ex.getMessage();
            Debug.error("ObserverCallBack: problem with scripting handler: %s\n%s\n%s", me, callback, msg);
        }
        return result;
    }
}
