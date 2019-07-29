   public static class GetAccesibleMethod {

          private Method method;

          public Method getMethod() {
             return method;
         }

          public GetAccesibleMethod(String className, String methodName) throws Throwable {

              if (System.getSecurityManager() == null) {
                 method = Class.forName(className).getMethod(methodName);
                 method.setAccessible(true);
             } else {
                 method = AccessController.doPrivileged(new PrivilegedAction<Method>() {
                     @Override
                     public Method run() {
                         try {
                             method = Class.forName(className).getMethod(methodName);
                             method.setAccessible(true);
                         } catch (Throwable t) {
                             method = null;
                             throw new RuntimeException("JDK did not allow accessing class", t);
                         }
                         return method;
                     }
                 });
             }
         }
     }		 