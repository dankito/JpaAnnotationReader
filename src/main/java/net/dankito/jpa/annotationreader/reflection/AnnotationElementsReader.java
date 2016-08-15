package net.dankito.jpa.annotationreader.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Reading Annotation values is very, very slow, especially on Android.
 * But luckily i found these trick from user931366, yanchenko and Gray (programmer of OrmLite) on https://stackoverflow.com/questions/7417426/why-are-annotations-under-android-such-a-performance-issue-slow
 * This speeds up Annotation values reading by a big factor (around 20).
 *
 * Created by ganymed on 26/08/15.
 */
public class AnnotationElementsReader implements IAnnotationElementsReader {

  protected static Boolean isOnAndroid = null;

  public static boolean isOnAndroid() {
    if(isOnAndroid == null) {
      try {
        Class.forName("android.app.Activity");
        isOnAndroid = true;
      } catch(Exception ex) {
        isOnAndroid = false;
      }
    }

    return isOnAndroid;
  }


  @Override
  public HashMap<String, Object> getElements(Annotation annotation) throws SQLException {
    if(isOnAndroid())
      return resolveMembersTheAndroidWay(annotation);
    else
      return resolveMembersTheJavaSeWay(annotation);
  }


  // only needed for JavaSe
  protected Method invocationHandlerGetMemberMethodsMethod = null;

  protected HashMap<String, Object> resolveMembersTheJavaSeWay(Annotation annotation) throws SQLException{
    HashMap<String, Object> map = new HashMap<String, Object>();
    InvocationHandler handler = Proxy.getInvocationHandler(annotation);

    try {
      if (handler.getClass().getName().startsWith("sun.reflect")) {
        if (invocationHandlerGetMemberMethodsMethod == null) {
          invocationHandlerGetMemberMethodsMethod = handler.getClass().getDeclaredMethod("getMemberMethods");
          invocationHandlerGetMemberMethodsMethod.setAccessible(true);
        }

        Method[] annotationMemberMethods = (Method[]) invocationHandlerGetMemberMethodsMethod.invoke(handler);

        for (Method member : annotationMemberMethods) {
          map.put(member.getName(), member.invoke(annotation));
        }
      }
    } catch(Exception ex) {
      throw new SQLException("Could not read member values of Annotation " + annotation.getClass().getName(), ex);
    }

    return map;
  }


  // only needed for Android
  protected Field elementsField = null;
  protected Field nameField = null;
  protected Method validateValueMethod = null;

  protected HashMap<String, Object> resolveMembersTheAndroidWay(Annotation annotation) throws SQLException {
    HashMap<String, Object> map = new HashMap<String, Object>();
    InvocationHandler handler = Proxy.getInvocationHandler(annotation);

    try {
      // Apache Harmony (Davlik) or libcore.reflect.AnnotationFactory (Android Runtime) implementation
      if (elementsField == null) {
        elementsField = handler.getClass().getDeclaredField("elements");
        elementsField.setAccessible(true);
      }

      Object[] annotationMembers = (Object[]) elementsField.get(handler);
      for (Object annotationMember : annotationMembers) {
        if (nameField == null)
          resolveNameFieldAndValidateValueMethod(annotationMember);

        String name = (String) nameField.get(annotationMember);
        Object value = validateValueMethod.invoke(annotationMember);
        map.put(name, value);
      }
    } catch(Exception ex) {
      throw new SQLException("Could not read member values of Annotation " + annotation.getClass().getName(), ex);
    }

    return map;
  }

  protected void resolveNameFieldAndValidateValueMethod(Object annotationMember) throws NoSuchFieldException, NoSuchMethodException {
    Class<?> cl = annotationMember.getClass();
    nameField = cl.getDeclaredField("name");
    nameField.setAccessible(true);
    validateValueMethod = cl.getDeclaredMethod("validateValue");
    validateValueMethod.setAccessible(true);
  }
}
