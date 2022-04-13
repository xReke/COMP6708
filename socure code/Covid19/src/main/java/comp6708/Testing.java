package comp6708;

import comp6708.model.Person;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicExistsException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Testing {

  public static void main(final String[] args) throws Exception {
//    if (args.length != 2) {
//      System.out.println("Please provide command line arguments: configPath topic");
//      System.exit(1);
//    }

    //Consumer
    final String consumerTopic = "risks";

    // Load properties from a local configuration file
    // Create the configuration file (e.g. at '$HOME/.confluent/java.config') with configuration parameters
    // to connect to your Kafka cluster, which can be on your local host, Confluent Cloud, or any other cluster.
    // Follow these instructions to create this file: https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/java.html

    final Properties props = loadConfig("/java.config");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "demo-consumer-1");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    final Consumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Arrays.asList(consumerTopic));

    //producer
    final String producerTopic = "positive";
    createTopic(producerTopic, props);

    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

    Producer<String, String> producer = new KafkaProducer<>(props);

    int total_count = 0;
    List<String> potentialList = new ArrayList<>();

    try {
      while (true) {
        if (total_count > 0) {
          total_count = 0;
          Person person = new Person();
          person.setPersonID(test(potentialList));
          System.out.println(person.toString());
          ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(producerTopic, UUID.randomUUID().toString().substring(0,8), person.toString());
          producer.send(producerRecord,new PersonPlaceProducer.DemoProducerCallBack());
          potentialList = new ArrayList<>();
        }
        producer.flush();
        ConsumerRecords<String, String> records = consumer.poll(100);
        for (ConsumerRecord<String, String> record : records) {
            String jsonString = record.value();
            JSONObject obj = new JSONObject(jsonString);
            String id = obj.getJSONObject("payload").getString("id");
          potentialList.add(id);
          total_count = total_count + 1;
        }
      }
    } finally {
      consumer.close();
      producer.close();
    }
  }

  private static String test(List<String> potentialList) {
    return potentialList.get(0);
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

}
