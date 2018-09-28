package io.jooby;

import org.jooby.funzy.Throwing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UrlParserTest {

  @Test
  public void simpleQueryString() {
    queryString("?&foo=bar", queryString -> {
      assertEquals("?&foo=bar", queryString.queryString());
      assertEquals("bar", queryString.get("foo").value());
      assertEquals(1, queryString.size());
    });
    queryString("/path?foo=bar&", queryString -> {
      assertEquals("?foo=bar&", queryString.queryString());
      assertEquals("bar", queryString.get("foo").value());
      assertEquals(1, queryString.size());
    });
    queryString("/path?foo=bar&&", queryString -> {
      assertEquals("?foo=bar&&", queryString.queryString());
      assertEquals("bar", queryString.get("foo").value());
      assertEquals(1, queryString.size());
    });

    queryString("?foo=bar", queryString -> {
      assertEquals("?foo=bar", queryString.queryString());
      assertEquals("bar", queryString.get("foo").value());
      assertEquals(1, queryString.size());
    });
    queryString("/path?foo=bar", queryString -> {
      assertEquals("?foo=bar", queryString.queryString());
      assertEquals("bar", queryString.get("foo").value());
      assertEquals(1, queryString.size());
    });
    queryString("/path?a=1&b=2", queryString -> {
      assertEquals("?a=1&b=2", queryString.queryString());
      assertEquals(1, queryString.get("a").intValue());
      assertEquals(2, queryString.get("b").intValue());
      assertEquals(2, queryString.size());
    });
    queryString("/path?a=1&b=2&", queryString -> {
      assertEquals("?a=1&b=2&", queryString.queryString());
      assertEquals(1, queryString.get("a").intValue());
      assertEquals(2, queryString.get("b").intValue());
      assertEquals(2, queryString.size());
    });
    queryString("/path?a=1&&b=2&", queryString -> {
      assertEquals("?a=1&&b=2&", queryString.queryString());
      assertEquals(1, queryString.get("a").intValue());
      assertEquals(2, queryString.get("b").intValue());
      assertEquals(2, queryString.size());
    });
    queryString("/path?a=1&a=2", queryString -> {
      assertEquals("?a=1&a=2", queryString.queryString());
      assertEquals(1, queryString.get("a").get(0).intValue());
      assertEquals(2, queryString.get("a").get(1).intValue());
      assertEquals(2, queryString.get("a").size());
      assertEquals(1, queryString.size());
    });
    queryString("/path?a=1;a=2", queryString -> {
      assertEquals("?a=1;a=2", queryString.queryString());
      assertEquals(1, queryString.get("a").get(0).intValue());
      assertEquals(2, queryString.get("a").get(1).intValue());
      assertEquals(2, queryString.get("a").size());
      assertEquals(1, queryString.size());
    });
    queryString("/path?a=", queryString -> {
      assertEquals("?a=", queryString.queryString());
      assertEquals("", queryString.get("a").value());
      assertEquals(1, queryString.size());
    });
    queryString("/path?a=&", queryString -> {
      assertEquals("?a=&", queryString.queryString());
      assertEquals("", queryString.get("a").value());
      assertEquals(1, queryString.size());
    });
    queryString("/path?a=&&", queryString -> {
      assertEquals("?a=&&", queryString.queryString());
      assertEquals("", queryString.get("a").value());
      assertEquals(1, queryString.size());
    });
    queryString("/path?", queryString -> {
      assertEquals("?", queryString.queryString());
      assertEquals(0, queryString.size());
    });
    queryString("/path", queryString -> {
      assertEquals("", queryString.queryString());
      assertEquals(0, queryString.size());
    });
  }

  @Test
  public void dotNotation() {
    queryString("?user.name=root&user.pwd=pass", queryString -> {
      assertEquals("?user.name=root&user.pwd=pass", queryString.queryString());
      assertEquals(1, queryString.size());
      assertEquals(2, queryString.get("user").size());
      assertEquals("root", queryString.get("user").get("name").value());
      assertEquals("pass", queryString.get("user").get("pwd").value());
    });

    queryString("?0.name=root&0.pwd=pass", queryString -> {
      assertEquals("?0.name=root&0.pwd=pass", queryString.queryString());
      assertEquals(1, queryString.size());
      assertEquals(2, queryString.get(0).size());
      assertEquals("root", queryString.get(0).get("name").value());
      assertEquals("pass", queryString.get(0).get("pwd").value());
    });

    queryString("?user.name=edgar&user.address.street=Street&user.address.number=55&user.type=dev",
        queryString -> {
          assertEquals(
              "?user.name=edgar&user.address.street=Street&user.address.number=55&user.type=dev",
              queryString.queryString());
          assertEquals(1, queryString.size());
          assertEquals(3, queryString.get("user").size());
          assertEquals("edgar", queryString.get("user").get("name").value());
          assertEquals("dev", queryString.get("user").get("type").value());
          assertEquals(2, queryString.get("user").get("address").size());
          assertEquals("Street", queryString.get("user").get("address").get("street").value());
          assertEquals("55", queryString.get("user").get("address").get("number").value());
        });
  }

  @Test
  public void bracketNotation() {
    queryString("?a[b]=1&a[c]=2", queryString -> {
      assertEquals("?a[b]=1&a[c]=2", queryString.queryString());
      assertEquals(1, queryString.size());
      assertEquals(1, queryString.get("a").get("b").intValue());
      assertEquals(2, queryString.get("a").get("c").intValue());
    });

    queryString(
        "?username=xyz&address[country][name]=AR&address[line1]=Line1&address[country][city]=BA",
        queryString -> {
          assertEquals(
              "?username=xyz&address[country][name]=AR&address[line1]=Line1&address[country][city]=BA",
              queryString.queryString());
          assertEquals(2, queryString.size());
          assertEquals("xyz", queryString.get("username").value());
          assertEquals("AR", queryString.get("address").get("country").get("name").value());
          assertEquals("BA", queryString.get("address").get("country").get("city").value());
          assertEquals("Line1", queryString.get("address").get("line1").value());
          assertEquals("{username=xyz, address={country={name=AR, city=BA}, line1=Line1}}",
              queryString.toString());
        });

    //    queryString("?list=1,2,3", queryString -> {
    //      assertEquals("?list=1,2,3", queryString.queryString());
    //      assertEquals(1, queryString.size());
    //      assertEquals(1, queryString.get("list").get(0).intValue());
    //      assertEquals(2, queryString.get("list").get(1).intValue());
    //      assertEquals(3, queryString.get("list").get(2).intValue());
    //      assertEquals("{list=[1, 2, 3]}", queryString.toString());
    //    });
  }

  @Test
  public void arrayArity() {
    assertEquals("1", Value.value("a", "1").value());
    assertEquals("1", Value.value("a", "1").get(0).value());
    assertEquals(1, Value.value("a", "1").size());
    queryString("?a=1&a=2", queryString -> {
      assertEquals("1", queryString.get("a").get(0).value());
      assertEquals("2", queryString.get("a").get(1).value());
    });
  }

  @Test
  public void valueToMap() {
    queryString("?foo=bar", queryString -> {
      assertEquals("{foo=[bar]}", queryString.toMap().toString());
    });
    queryString("?a=1;a=2", queryString -> {
      assertEquals("{a=[1, 2]}", queryString.toMap().toString());
    });
    queryString(
        "?username=xyz&address[country][name]=AR&address[line1]=Line1&address[country][city]=BA",
        queryString -> {
          assertEquals(
              "{username=[xyz], address.country.name=[AR], address.country.city=[BA], address.line1=[Line1]}",
              queryString.toMap().toString());
          assertEquals(
              "{address.country.name=[AR], address.country.city=[BA], address.line1=[Line1]}",
              queryString.get("address").toMap().toString());
          assertEquals("{country.name=[AR], country.city=[BA]}",
              queryString.get("address").get("country").toMap().toString());
          assertEquals("{city=[BA]}",
              queryString.get("address").get("country").get("city").toMap().toString());
        });
  }

  @Test
  public void verifyIllegalAccess() {
    /** Object: */
    queryString("?foo=bar", queryString -> {
      assertThrows(Err.BadRequest.class, () -> queryString.value());
      assertThrows(Err.BadRequest.class, () -> queryString.value(""));
      assertThrows(Err.Missing.class, () -> queryString.get("a").get("a").get("a").value());
      assertThrows(Err.Missing.class, () -> queryString.get("missing").value());
      assertThrows(Err.Missing.class, () -> queryString.get(0).value());
      assertEquals("missing", queryString.get("missing").value("missing"));
      assertEquals("a", queryString.get("a").get("a").get("a").value("a"));
    });

    /** Array: */
    queryString("?a=1;a=2", queryString -> {
      assertThrows(Err.BadRequest.class, () -> queryString.get("a").value());
      assertEquals("1", queryString.get("a").get(0).value());
      assertEquals("2", queryString.get("a").get(1).value());
      assertThrows(Err.Missing.class, () -> queryString.get("a").get("b").value());
      assertThrows(Err.Missing.class, () -> queryString.get("a").get(3).value());
      assertEquals("missing", queryString.get("a").get(3).value("missing"));
    });

    /** Single Property: */
    queryString("?foo=bar", queryString -> {
      assertThrows(Err.Missing.class, () -> queryString.get("foo").get("missing").value());
      assertEquals("bar", queryString.get("foo").get(0).value());
    });

    /** Missing Property: */
    queryString("?", queryString -> {
      assertThrows(Err.Missing.class, () -> queryString.get("foo").get("missing").value());
      assertThrows(Err.Missing.class, () -> queryString.get("foo").get(0).value());
    });
  }

  @Test
  public void decode() {
    queryString("/?name=Pedro%20Picapiedra", queryString -> {
      assertEquals("Pedro Picapiedra", queryString.get("name").value());
    });

    queryString("/?file=js%2Findex.js", queryString -> {
      assertEquals("js/index.js", queryString.get("file").value());
    });

    queryString("/?25=%20%25", queryString -> {
      assertEquals(" %", queryString.get("25").value());
    });

    queryString("/?plus=a+b", queryString -> {
      assertEquals("a b", queryString.get("plus").value());
    });
    queryString("/?tail=a%20%2B", queryString -> {
      assertEquals("a +", queryString.get("tail").value());
    });

  }

  @Test
  public void customMapper() {
    assertEquals(new BigDecimal("3.14"), Value.value("n", "3.14").value(BigDecimal::new));
    Throwing.Function<String, BigDecimal> toBigDecimal = BigDecimal::new;
    assertEquals(BigDecimal.ONE, Value.value("n", "x").value(toBigDecimal.orElse(BigDecimal.ONE)));

    assertMessage(Err.BadRequest.class,
        () -> Value.value("n", "x").value(toBigDecimal.onFailure(NumberFormatException.class, x -> {
          throw new Err.BadRequest("Type mismatch: cannot convert to decimal", x);
        })), "Type mismatch: cannot convert to decimal");
  }

  @Test
  public void verifyExceptionMessage() {
    /** Object: */
    queryString("?foo=bar", queryString -> {
      assertMessage(Err.BadRequest.class, () -> queryString.value(),
          "Type mismatch: cannot convert object to string");
      assertMessage(Err.BadRequest.class, () -> queryString.get("foo").intValue(),
          "Type mismatch: cannot convert to number");
      assertMessage(Err.BadRequest.class, () -> queryString.get("foo").intValue(0),
          "Type mismatch: cannot convert to number");
      assertMessage(Err.Missing.class, () -> queryString.get("foo").get("bar").value(),
          "Required value is not present: 'foo.bar'");
      assertMessage(Err.Missing.class, () -> queryString.get("foo").get(1).value(),
          "Required value is not present: 'foo.1'");
      assertMessage(Err.Missing.class, () -> queryString.get("r").longValue(),
          "Required value is not present: 'r'");
      assertEquals(1, queryString.get("a").intValue(1));
    });

    /** Array: */
    queryString("?a=b;a=c", queryString -> {
      assertMessage(Err.BadRequest.class, () -> queryString.get("a").value(),
          "Type mismatch: cannot convert array to string");
      assertMessage(Err.BadRequest.class, () -> queryString.get("a").get(0).longValue(),
          "Type mismatch: cannot convert to number");
      assertMessage(Err.Missing.class, () -> queryString.get("a").get(3).longValue(),
          "Required value is not present: 'a[3]'");
      assertMessage(Err.Missing.class, () -> queryString.get("a").get("b").value(),
          "Required value is not present: 'a.b'");
      assertMessage(Err.Missing.class, () -> queryString.get("a").get("b").get(3).longValue(),
          "Required value is not present: 'a.b[3]'");
    });

    /** Single: */
    assertMessage(Err.BadRequest.class, () -> Value.value("foo", "bar").intValue(),
        "Type mismatch: cannot convert to number");

    assertMessage(Err.Missing.class, () -> Value.value("foo", "bar").get("foo").value(),
        "Required value is not present: 'foo.foo'");

    assertMessage(Err.Missing.class, () -> Value.value("foo", "bar").get(1).value(),
        "Required value is not present: 'foo.1'");

  }

  public static <T extends Throwable> void assertMessage(Class<T> expectedType,
      Executable executable, String message) {
    T x = assertThrows(expectedType, executable);
    assertEquals(message, x.getMessage());
  }

  private void queryString(String queryString, Consumer<QueryString> consumer) {
    consumer.accept(UrlParser.queryString(queryString));
  }
}