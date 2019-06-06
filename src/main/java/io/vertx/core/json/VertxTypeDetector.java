package io.vertx.core.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.jr.ob.impl.ClassKey;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.TypeDetector;
import com.fasterxml.jackson.jr.ob.impl.ValueReader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;

class VertxTypeDetector extends TypeDetector {

  public VertxTypeDetector(int features) {
    super(features);
    init();
  }

  public VertxTypeDetector(TypeDetector base, int features) {
    super(base, features);
    init();
  }

  private void init() {
    _knownSerTypes.put(new ClassKey(JsonObject.class, _features), TypeDetector.SER_UNKNOWN);
    _knownSerTypes.put(new ClassKey(Instant.class, _features), TypeDetector.SER_UNKNOWN);
  }

  @Override
  protected ValueReader createReader(Class<?> contextType, Class<?> type, Type genericType) {
    if (type == Instant.class) {
      return new ValueReader() {
        @Override
        public Object read(JSONReader reader, JsonParser p) throws IOException {
          String str = p.getValueAsString();
          return str != null ? Json.readInstant(p.getValueAsString()) : null;
        }
        @Override
        public Object readNext(JSONReader reader, JsonParser p) throws IOException {
          String str = p.nextTextValue();
          str = str == null ? p.getValueAsString() : str;
          return str != null ? Json.readInstant(p.getValueAsString()) : null;
        }
      };
    }
    return super.createReader(contextType, type, genericType);
  }

  /*
      @Override
      protected BeanPropertyWriter[] resolveBeanForSer(Class<?> raw, POJODefinition classDef) {
        return super.resolveBeanForSer(raw, classDef);
      }
      @Override
      protected BeanReader _resolveBeanForDeser(Class<?> raw) {
        BeanReader br = super._resolveBeanForDeser(raw);
        Map<String, BeanPropertyReader> props = br.propertiesByName();
        props.entrySet().forEach(entry -> {
          BeanPropertyReader re = entry.getValue();
          if (re.genericSetterType() == Instant.class) {
            ValueReader valueReader = re.getReader();
            re = re.withReader(new ValueReader() {
              @Override
              public Object read(JSONReader reader, JsonParser p) throws IOException {
                Object text = valueReader.read(reader, p);
                if (text != null) {
                  text = readInstant(text.toString());
                }
                return text;
              }
              @Override
              public Object readNext(JSONReader reader, JsonParser p) throws IOException {
                Object text = valueReader.readNext(reader, p);
                if (text != null) {
                  text = readInstant(text.toString());
                }
                return text;
              }
            });
            entry.setValue(re);
          }
        });
        return br;
      }
  */
  @Override
  public TypeDetector perOperationInstance(int features) {
    return new VertxTypeDetector(this, features & CACHE_FLAGS);
  }
}
