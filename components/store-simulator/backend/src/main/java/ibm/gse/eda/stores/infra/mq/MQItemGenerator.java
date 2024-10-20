package ibm.gse.eda.stores.infra.mq;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import ibm.gse.eda.stores.domain.Item;
import ibm.gse.eda.stores.infra.SimulatorGenerator;
import io.smallrye.mutiny.Multi;

@ApplicationScoped
public class MQItemGenerator extends SimulatorGenerator {
  private static Logger logger = Logger.getLogger(MQItemGenerator.class.getName());

  @Inject
  @ConfigProperty(name = "mq.host")
  public String mqHostname;

  @Inject
  @ConfigProperty(name = "mq.port")
  public int mqHostport;

  @Inject
  @ConfigProperty(name = "mq.qmgr", defaultValue = "QM1")
  public String mqQmgr;

  @Inject
  @ConfigProperty(name = "mq.channel", defaultValue = "DEV.APP.SVRCONN")
  public String mqChannel;

  @Inject
  @ConfigProperty(name = "mq.app_user", defaultValue = "app")
  public String mqAppUser;

  @Inject
  @ConfigProperty(name = "mq.app_password", defaultValue = "passw0rd")
  public String mqPassword;

  @Inject
  @ConfigProperty(name = "mq.queue_name", defaultValue = "DEV.QUEUE.1")
  public String mqQueueName;

  @Inject
  @ConfigProperty(name = "app.name", defaultValue = "TestApp")
  public String appName;

  @Inject
  @ConfigProperty(name = "mq.cipher_suite")
  public Optional<String> mqCipherSuite;

  @Inject
  @ConfigProperty(name = "mq.ccdt_url")
  public Optional<String> mqCcdtUrl;

  private Jsonb parser = JsonbBuilder.create();

  private JMSProducer producer = null;
  private JMSContext jmsContext = null;
  private Destination destination = null;
  private JmsConnectionFactory cf = null;
  protected Jsonb jsonb = null;

  private static void processJMSException(JMSException jmsex) {
    logger.info(jmsex.getMessage());
    Throwable innerException = jmsex.getLinkedException();
    logger.error("Exception is: " + jmsex);
    if (innerException != null) {
      logger.error("Inner exception(s):");
    }
    while (innerException != null) {
      logger.error(innerException.getMessage());
      innerException = innerException.getCause();
    }
    return;
  }

  @Override
  public void sendMessages(List<Item> items) {
    try {
      jmsContext = buildJMSConnectionSession();
      producer = jmsContext.createProducer();
      Multi.createFrom().items(items.stream()).subscribe().with(item -> {
        sendMessage(item);
      }, failure -> logger.error("Failed with " + failure.getMessage()));
    } catch (Exception e) {
      if (e != null) {
        if (e instanceof JMSException) {
          processJMSException((JMSException) e);
        } else {
          logger.error(e.getMessage());
          logger.error(e.getCause());
        }
      }
    } finally {
      if (jmsContext != null)
        jmsContext.close();
    }

  }

  @Override
  public void sendMessage(Item item) {
    try {
      String msg = parser.toJson(item);
      TextMessage message = jmsContext.createTextMessage(msg);
      message.setJMSCorrelationID(item.storeName);
      producer.send(destination, message);
      logger.info("sent to MQ:" + msg);
    } catch (Exception e) {
      if (e != null) {
        if (e instanceof JMSException) {
          processJMSException((JMSException) e);
        } else {
          logger.error(e.getMessage());
          logger.error(e.getCause());
        }
      }
    }
  }

  private String validateCcdtFile() {
    /*
     * Modeled after
     * github.com/ibm-messaging/mq-dev-patterns/blob/master/JMS/com/ibm/mq/samples/
     * jms/SampleEnvSetter.java
     */
    String value = mqCcdtUrl.orElse("");
    String filePath = null;
    if (value != null && !value.isEmpty()) {
      logger.info("Checking for existence of file " + value);
      File tmp = new File(value);
      if (!tmp.exists()) {
        logger.info(value + " does not exist...");
        filePath = null;
      } else {
        logger.info(value + " exists!");
        filePath = value;
      }
    }
    return filePath;
  }

  private JMSContext buildJMSConnectionSession() throws JMSException {
    JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
    cf = ff.createConnectionFactory();
    cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
    cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, this.mqQmgr);
    cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, this.appName);

    cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
    cf.setStringProperty(WMQConstants.USERID, this.mqAppUser);
    cf.setStringProperty(WMQConstants.PASSWORD, this.mqPassword);

    String ccdtFilePath = validateCcdtFile();
    if (ccdtFilePath == null) {
      logger.info("No valid CCDT file detected. Using host, port, and channel properties instead.");
      cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, this.mqHostname);
      cf.setIntProperty(WMQConstants.WMQ_PORT, this.mqHostport);
      cf.setStringProperty(WMQConstants.WMQ_CHANNEL, this.mqChannel);
    } else {
      logger.info("Setting CCDTURL to 'file://" + ccdtFilePath + "'");
      cf.setStringProperty(WMQConstants.WMQ_CCDTURL, "file://" + ccdtFilePath);
    }

    if (this.mqCipherSuite != null && !("".equalsIgnoreCase(this.mqCipherSuite.orElse("")))) {
      cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, this.mqCipherSuite.orElse(""));
    }

    // Create JMS objects
    jmsContext = cf.createContext();
    destination = jmsContext.createQueue("queue:///" + this.mqQueueName);
    logger.info(cf.toString());
    return jmsContext;
  }

  @Override
  public boolean preProcessing() {
    try {
      jmsContext = buildJMSConnectionSession();
    } catch (JMSException e) {
      e.printStackTrace();
      return false;
    }
    producer = jmsContext.createProducer();
    return true;
  }
}
