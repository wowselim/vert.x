package io.vertx.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TreeCodec;
import com.fasterxml.jackson.jr.ob.impl.BeanPropertyWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;
import com.fasterxml.jackson.jr.ob.impl.TypeDetector;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

class VertxJSONWriter extends JSONWriter {
  public VertxJSONWriter(int features, TypeDetector td, TreeCodec tc) {
    super(features, td, tc);
  }
  public VertxJSONWriter(VertxJSONWriter base, int features, TypeDetector td, JsonGenerator g) {
    super(base, features, td, g);
  }
  @Override
  protected void writeBeanValue(BeanPropertyWriter[] props, Object bean) throws IOException {
    super.writeBeanValue(props, bean);
  }
  @Override
  public JSONWriter perOperationInstance(int features, JsonGenerator g) {
    return new VertxJSONWriter(this, features,
      _typeDetector.perOperationInstance(features), g);
  }
  @Override
  protected void writeUnknownValue(Object data) throws IOException {
    if (data instanceof JsonObject) {
      super.writeMapValue(((JsonObject)data).getMap());
    } else if (data instanceof Instant) {
      super.writeStringValue(ISO_INSTANT.format((TemporalAccessor) data));
    } else {
      super.writeUnknownValue(data);
    }
  }
  @Override
  protected void writeUnknownField(String fieldName, Object data) throws IOException {
    if (data instanceof JsonObject) {
      super.writeMapField(fieldName, ((JsonObject)data).getMap());
    } else if (data instanceof Instant) {
      super.writeStringField(fieldName, ISO_INSTANT.format((TemporalAccessor) data));
    } else {
      super.writeUnknownField(fieldName, data);
    }
  }
}
