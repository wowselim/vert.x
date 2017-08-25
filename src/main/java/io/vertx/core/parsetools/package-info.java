/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

/**
 * == Record Parser
 *
 * The record parser allows you to easily parse protocols which are delimited by a sequence of bytes, or fixed
 * size records. It transforms a sequence of input buffer to a sequence of buffer structured as configured (either
 * fixed size or separated records).
 *
 * For example, if you have a simple ASCII text protocol delimited by '\n' and the input is the following:
 *
 * [source]
 * ----
 * buffer1:HELLO\nHOW ARE Y
 * buffer2:OU?\nI AM
 * buffer3: DOING OK
 * buffer4:\n
 * ----
 *
 * The record parser would produce
 *[source]
 * ----
 * buffer1:HELLO
 * buffer2:HOW ARE YOU?
 * buffer3:I AM DOING OK
 * ----
 *
 * Let's see the associated code:
 *
 * [source, $lang]
 * ----
 * {@link examples.ParseToolsExamples#recordParserExample1()}
 * ----
 *
 * You can also produce fixed sized chunks as follows:
 *
 * [source, $lang]
 * ----
 * {@link examples.ParseToolsExamples#recordParserExample2()}
 * ----
 *
 * For more details, check out the {@link io.vertx.core.parsetools.RecordParser} class.
 *
 * == Json Parser
 *
 * The json parser provides a good alternative when you need to deal with large json structures. It transforms a sequence
 * of input buffer to a sequence of json parse events.
 *
 * [source, $lang]
 * ----
 * {@link examples.ParseToolsExamples#jsonParserExample1()}
 * ----
 *
 * The json parser is non-blocking and emitted events are driven by the input buffers
 *
 * [source, $lang]
 * ----
 * {@link examples.ParseToolsExamples#jsonParserExample2}
 * ----
 *
 * Event driven parsing provides more control but comes at the price of flexibility. The json parser allows you
 * to handle json structures when it is desired:
 *
 * [source, $lang]
 * ----
 * {@link examples.ParseToolsExamples#jsonParserExample3}
 * ----
 *
 * The object and array handlers can be set and unset during the parsing allowing you to switch between fine grained
 * events or json object/array events.
 *
 * [source, $lang]
 * ----
 * {@link examples.ParseToolsExamples#jsonParserExample4}
 * ----
 *
 * You can also decode POJOs
 *
 * [source, $lang]
 * ----
 * {@link examples.ParseToolsExamples#jsonParserExample5}
 * ----
 *
 * Whenever the parser fails to process a buffer, an exception will be thrown unless you set an exception handler:
 *
 * [source, $lang]
 * ----
 * {@link examples.ParseToolsExamples#jsonParserExample5}
 * ----
 *
 * For more details, check out the {@link io.vertx.core.parsetools.JsonParser} class.
 */
@Document(fileName = "parsetools.adoc")
package io.vertx.core.parsetools;

import io.vertx.docgen.Document;

