package examples;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.core.parsetools.RecordParser;

/**
 * Example using the record parser.
 */
public class ParseToolsExamples {


  public void recordParserExample1() {
    final RecordParser parser = RecordParser.newDelimited("\n", h -> {
      System.out.println(h.toString());
    });

    parser.handle(Buffer.buffer("HELLO\nHOW ARE Y"));
    parser.handle(Buffer.buffer("OU?\nI AM"));
    parser.handle(Buffer.buffer("DOING OK"));
    parser.handle(Buffer.buffer("\n"));
  }

  public void recordParserExample2() {
    RecordParser.newFixed(4, h -> {
      System.out.println(h.toString());
    });
  }

  public void jsonParserExample1() {

    JsonParser parser = JsonParser.newParser();

    // Set handlers for various events
    parser.startArrayHandler(v -> {
      // Start an array
    });
    parser.endArrayHandler(v -> {
      // End an array
    });
    parser.startObjectHandler(json -> {
      // Start an objet
    });
    parser.endObjectHandler(json -> {
      // End an objet
    });

    parser.valueHandler(value -> {
      // Handle a field
      String field = parser.currentField();
      if (field != null) {
        // In an object
      } else {
        // In an array or top level
      }
    });
  }

  public void jsonParserExample2() {

    JsonParser parser = JsonParser.newParser();

    // start array event
    // start object event
    // "firstName":"Bob" event
    parser.handle(Buffer.buffer("[{\"firstName\":\"Bob\"},"));

    // "lastName":"Morane" event
    // end object event
    parser.handle(Buffer.buffer("\"lastName\":\"Morane\")},"));

    // start object event
    // "firstName":"Luke" event
    // "lastName":"Lucky" event
    // end object event
    parser.handle(Buffer.buffer("{\"firstName\":\"Luke\",\"lastName\":\"Lucky\")}"));

    // end array event
    parser.handle(Buffer.buffer("]"));

    // Always call end
    parser.end();
  }

  public void jsonParserExample3() {

    JsonParser parser = JsonParser.newParser();

    parser.startArrayHandler(v -> {
      // Start the array
    });
    parser.endArrayHandler(v -> {
      // End the array
    });
    parser.objectHandler(json -> {
      // Handle each object
    });

    parser.handle(Buffer.buffer("[{\"firstName\":\"Bob\"},\"lastName\":\"Morane\"),...]"));
    parser.end();
  }

  public void jsonParserExample4() {

    JsonParser parser = JsonParser.newParser();

    parser.startObjectHandler(v -> {
      // Start the object

      // Set an object handler to handle each entry, from now on the parser won't emit start object events
      parser.objectHandler(json -> {
        // Handle each object
        // Get the field in which this object was parsed
        String id = parser.currentField();
        System.out.println("User with id " + id + " : " + json.encodePrettily());
      });
    });
    parser.endObjectHandler(v -> {
      // Clear the object handler so the parser emits start/end object events again
      parser.objectHandler(null);
    });

    parser.handle(Buffer.buffer("{\"39877483847\":{\"firstName\":\"Bob\"},\"lastName\":\"Morane\"),...}"));
    parser.end();
  }

  private static class User {
    private String firstName;
    private String lastName;
  }

  public void jsonParserExample5(JsonParser parser) {
    parser.objectHandler(User.class, user -> {
      // Handle each object
      // Get the field in which this object was parsed
      String id = parser.currentField();
      System.out.println("User with id " + id + " : " + user.firstName + " " + user.lastName);
    });
  }

  public void jsonParserExample6() {

    JsonParser parser = JsonParser.newParser();

    parser.exceptionHandler(err -> {
      // Catch any parsing or decoding error
    });

  }
}
