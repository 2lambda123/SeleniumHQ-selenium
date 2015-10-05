/*
Copyright 2015 Software Freedom Conservancy
Copyright 2007-2009 Selenium committers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.openqa.selenium.support;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.DefaultFieldDecorator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.FieldDecorator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;


/**
 * Factory class to make using Page Objects simpler and easier.
 *
 * @see <a href="https://github.com/SeleniumHQ/selenium/wiki/PageObjects">Page Objects Wiki</a>
 */
public class PageFactory {
  /**
   * Instantiate an instance of the given class, and set a lazy proxy for each of the WebElement and
   * List&lt;WebElement&gt; fields that have been declared, assuming that the field
   * name is also the HTML element's "id" or "name". This means that for the class:
   *
   * <code> public class Page { private WebElement submit; } </code>
   *
   * there will be an element that can be located using the xpath expression "//*[@id='submit']" or
   * "//*[@name='submit']"
   *
   * By default, the element or the list is looked up each and every time a method is called upon
   * it. To change this behaviour, simply annotate the field with the {@link CacheLookup}. To change
   * how the element is located, use the {@link FindBy} annotation.
   *
   * This method will attempt to instantiate the class given to it, preferably using a constructor
   * which takes a WebDriver instance as its only argument or falling back on a no-arg constructor.
   * An exception will be thrown if the class cannot be instantiated.
   *
   * @param context          The org.openqa.selenium.SearchContext instance that will be used to
   *                         look up the elements
   * @param pageClassToProxy A class which will be initialised.
   * @return An instantiated instance of the class with WebElement and List&lt;WebElement&gt;
   * fields proxied
   * @see FindBy
   * @see CacheLookup
   */
  public static <T> T initElements(SearchContext context, Class<T> pageClassToProxy) {
    T page = instantiatePage(context, pageClassToProxy);
    initElements(context, page);
    return page;
  }

  /**
   * As {@link org.openqa.selenium.support.PageFactory#initElements(org.openqa.selenium.SearchContext,
   * Class)} but will only replace the fields of an already instantiated Page Object.
   *
   * @param context The org.openqa.selenium.SearchContext instance that will be used to look up the elements
   * @param page   The object with WebElement and List&lt;WebElement&gt; fields that
   *               should be proxied.
   */
  public static void initElements(SearchContext context, Object page) {
    final SearchContext searchContextRef = context;
    initElements(new DefaultElementLocatorFactory(searchContextRef), page);
  }

  /**
   * Similar to the other "initElements" methods, but takes an {@link ElementLocatorFactory} which
   * is used for providing the mechanism for fniding elements. If the ElementLocatorFactory returns
   * null then the field won't be decorated.
   *
   * @param factory The factory to use
   * @param page    The object to decorate the fields of
   */
  public static void initElements(ElementLocatorFactory factory, Object page) {
    final ElementLocatorFactory factoryRef = factory;
    initElements(new DefaultFieldDecorator(factoryRef), page);
  }

  /**
   * Similar to the other "initElements" methods, but takes an {@link FieldDecorator} which is used
   * for decorating each of the fields.
   *
   * @param decorator the decorator to use
   * @param page      The object to decorate the fields of
   */
  public static void initElements(FieldDecorator decorator, Object page) {
    Class<?> proxyIn = page.getClass();
    while (proxyIn != Object.class) {
      proxyFields(decorator, page, proxyIn);
      proxyIn = proxyIn.getSuperclass();
    }
  }

  private static void proxyFields(FieldDecorator decorator, Object page, Class<?> proxyIn) {
    Field[] fields = proxyIn.getDeclaredFields();
    for (Field field : fields) {
      int modifiers = field.getModifiers();
      if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers))
        continue;
      
      Object value = decorator.decorate(page.getClass().getClassLoader(), field);
      if (value != null) {
        try {
          field.setAccessible(true);
          field.set(page, value);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T instantiatePage(SearchContext context, Class<T> pageClassToProxy) {
    try {
      Constructor<?>[] availableConstructors = pageClassToProxy.getDeclaredConstructors();
      for (Constructor<?> c: availableConstructors){

          Class<?>[] parameterTypes = c.getParameterTypes();
          if (parameterTypes.length != 1)
            continue;

          Class<?> parameterClazz = parameterTypes[0];
          if (!parameterClazz.isAssignableFrom(context.getClass()))
            continue;
          c.setAccessible(true);
          return (T) c.newInstance(context);
      }

      return pageClassToProxy.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
