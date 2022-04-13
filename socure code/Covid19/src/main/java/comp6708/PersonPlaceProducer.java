/**
 * Copyright 2020 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package comp6708;

import comp6708.model.*;
import comp6708.service.GeneratePersonData;
import comp6708.service.GeneratePlaceData;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.Producer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.common.errors.TopicExistsException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PersonPlaceProducer {

  // Create topic in Confluent Cloud
  public static void createTopic(final String topic,
                          final Properties cloudConfig) {
      final NewTopic newTopic = new NewTopic(topic, Optional.empty(), Optional.empty());
      try (final AdminClient adminClient = AdminClient.create(cloudConfig)) {
          adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
      } catch (final InterruptedException | ExecutionException e) {
          // Ignore if TopicExistsException, which may be valid if topic exists
          if (!(e.getCause() instanceof TopicExistsException)) {
              throw new RuntimeException(e);
          }
      }
  }

  public static void main(final String[] args) throws IOException, InterruptedException {
//    if (args.length != 2) {
//      System.out.println("Please provide command line arguments: configPath topic");
//      System.exit(1);
//    }

    // Load properties from a local configuration file
    // Create the configuration file (e.g. at '$HOME/.confluent/java.config') with configuration parameters
    // to connect to your Kafka cluster, which can be on your local host, Confluent Cloud, or any other cluster.
    // Follow these instructions to create this file: https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/java.html
    final Properties props = loadConfig("/java.config");

    // Create topic if needed
    final String topic = "visited";
    createTopic(topic, props);

    // Add additional properties.
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaJsonSerializer");

    Producer<String, Visited> producer = new KafkaProducer<>(props);

      for (int i = 0; i < 50; i++) {
          Person person = GeneratePersonData.generate();
          Place place = GeneratePlaceData.generate();
          Visited visited = new Visited();
          visited.setPerson_id(person.getPersonID());
          visited.setPerson_age(person.getPersonAge());
          visited.setPerson_gender(person.getPersonGender());
          visited.setPlace_name(place.getPlaceName());
          visited.setPlace_district(place.getPlaceDistrict());
          visited.setVisited(true);
          DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
          LocalDateTime now = LocalDateTime.now();
          visited.setTime(dtf.format(now));
          System.out.println(visited.toString());
          ProducerRecord<String, Visited> producerRecord = new ProducerRecord<String, Visited>(topic, UUID.randomUUID().toString().substring(0,8), visited);
          producer.send(producerRecord,new DemoProducerCallBack());
          Thread.sleep(1000);
      }


    producer.flush();

    producer.close();
  }

    static class DemoProducerCallBack implements Callback {

        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if(exception != null){
                exception.printStackTrace();;
            }
        }
    }

  public static Properties loadConfig(final String configFile) throws IOException {
//    if (!Files.exists(Paths.get(configFile))) {
//      throw new IOException(configFile + " not found.");
//    }
    final Properties cfg = new Properties();
    try (InputStream inputStream = PersonPlaceProducer.class.getResourceAsStream(configFile)) {
      cfg.load(inputStream);
    }
    return cfg;
  }

}
