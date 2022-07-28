/* (C)2022 */
package io.github.shalk.mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class Foo {

  String say() {
    return "hi";
  }

  String echo(String a) {
    return a;
  }

  public static String hold() {
    return "static";
  }

  public static String reply(String a) {
    return a;
  }
}

public class AppTest {

  @Test
  void returnWhatIWant() {
    Foo foo = Mockito.mock(Foo.class);
    Mockito.when(foo.say()).thenReturn("nice to meet you");
    String result = foo.say();
    Assertions.assertEquals("nice to meet you", result);
  }

  @Test
  void returnBaseOneParamOrSomething() {
    Foo foo = Mockito.mock(Foo.class);
    Mockito.when(foo.echo(anyString()))
        .thenAnswer(
            new Answer<String>() {
              @Override
              public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                String argument = invocationOnMock.getArgument(0, String.class);
                return "hi," + argument;
              }
            });
    String result = foo.echo("all");
    Assertions.assertEquals("hi,all", result);
  }

  @Test
  void verifyMethod() {
    Foo foo = Mockito.mock(Foo.class);
    foo.say();

    Mockito.verify(foo).say();
    Mockito.verify(foo, Mockito.times(1)).say();
    Mockito.verify(foo, Mockito.timeout(1000)).say();
  }

  @Test
  void mockStaticMethod() {
    try (MockedStatic<Foo> fooMockedStatic = Mockito.mockStatic(Foo.class)) {
      fooMockedStatic.when(() -> Foo.hold()).thenReturn("hi");
      String result = Foo.hold();
      Assertions.assertEquals("hi", result);
    }

    try (MockedStatic<Foo> fooMockedStatic = Mockito.mockStatic(Foo.class)) {
      fooMockedStatic
          .when(() -> Foo.reply(anyString()))
          .thenAnswer(
              new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                  String arg = invocationOnMock.getArgument(0, String.class);
                  return "hi," + arg;
                }
              });
      String result = Foo.reply("1024");
      Assertions.assertEquals("hi,1024", result);
    }
  }

  @Test
  void mockConstruction() {
    try (MockedConstruction<Foo> fooMockedConstruction = Mockito.mockConstruction(Foo.class)) {
      Foo foo = new Foo();
      foo.say();
      Mockito.verify(foo).say();
    }

    try (MockedConstruction<Foo> fooMockedConstruction =
        Mockito.mockConstruction(
            Foo.class,
            new MockedConstruction.MockInitializer<Foo>() {
              @Override
              public void prepare(Foo foo1, MockedConstruction.Context context) {
                Mockito.when(foo1.say()).thenReturn("mock");
              }
            })) {
      Foo foo3 = new Foo();
      String result = foo3.say();
      Mockito.verify(foo3, Mockito.times(1)).say();

      Assertions.assertEquals("mock", result);
    }
  }

  @Test
  void mockSpecificArgument() {
    Foo foo = Mockito.mock(Foo.class);
    Mockito.when(foo.echo(eq("hello"))).thenReturn("nonono");
    Assertions.assertEquals("nonono", foo.echo("hello"));
    Mockito.verify(foo).echo("hello");

    Assertions.assertNull(foo.echo("other"));
    Mockito.verify(foo).echo("other");
  }
}
