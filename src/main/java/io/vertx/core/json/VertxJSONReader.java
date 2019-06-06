package io.vertx.core.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeCodec;
import com.fasterxml.jackson.jr.ob.impl.CollectionBuilder;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.MapBuilder;
import com.fasterxml.jackson.jr.ob.impl.TypeDetector;

class VertxJSONReader extends JSONReader {

  public VertxJSONReader(int features, TypeDetector td, TreeCodec treeCodec, CollectionBuilder lb, MapBuilder mb) {
    super(features, td, treeCodec, lb, mb);
  }

  public VertxJSONReader(JSONReader base, int features, TypeDetector td, JsonParser p) {
    super(base, features, td, p);
  }

  @Override
  public JSONReader perOperationInstance(int features, JsonParser p) {
    return new VertxJSONReader(this, features,
      _typeDetector.perOperationInstance(features), p);
  }
}
