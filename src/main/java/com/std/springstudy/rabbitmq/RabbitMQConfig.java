package com.std.springstudy.rabbitmq;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.time.Duration;

@Configuration
public class RabbitMQConfig {

    @Autowired
    private Environment environment;

    private String stdMqTemplate = "spring.rabbitmq.stdmq";

    private String zzqqMqTemplate = "spring.rabbitmq.zzqqmq";

    @Bean(name = "stdMqTemplate")
    @Primary
    public AmqpTemplate stdRabbitMqTemplate(@Qualifier("stdMqConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean(name = "zzqqMqTemplate")
    public AmqpTemplate jhjjRabbitMqTemplate(@Qualifier("zzqqMqConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean(name = "stdMqConnectionFactory")
    @Primary
    public ConnectionFactory stdMqConnectionFactory() throws Exception {
        RabbitProperties rabbitProperties = Binder.get(environment).bind(stdMqTemplate, RabbitProperties.class).get();
        return rabbitConnectionFactory(rabbitProperties);
    }

    @Bean(name = "zzqqMqConnectionFactory")
    public ConnectionFactory jhjjMqConnectionFactory() throws Exception {
        RabbitProperties rabbitProperties = Binder.get(environment).bind(zzqqMqTemplate, RabbitProperties.class).get();
        return rabbitConnectionFactory(rabbitProperties);
    }

    @Bean(name="stdListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory stdListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("stdMqConnectionFactory") ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean(name="jhjjListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory jhjjListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("zzqqMqConnectionFactory") ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    public CachingConnectionFactory rabbitConnectionFactory(RabbitProperties properties) throws Exception {
        PropertyMapper map = PropertyMapper.get();
        CachingConnectionFactory factory = new CachingConnectionFactory(
                getRabbitConnectionFactoryBean(properties).getObject());
        map.from(properties::determineAddresses).to(factory::setAddresses);
        map.from(properties::isPublisherConfirms).to(factory::setPublisherConfirms);
        map.from(properties::isPublisherReturns).to(factory::setPublisherReturns);
        RabbitProperties.Cache.Channel channel = properties.getCache().getChannel();
        map.from(channel::getSize).whenNonNull().to(factory::setChannelCacheSize);
        map.from(channel::getCheckoutTimeout).whenNonNull().as(Duration::toMillis)
                .to(factory::setChannelCheckoutTimeout);
        RabbitProperties.Cache.Connection connection = properties.getCache()
                .getConnection();
        map.from(connection::getMode).whenNonNull().to(factory::setCacheMode);
        map.from(connection::getSize).whenNonNull()
                .to(factory::setConnectionCacheSize);
        return factory;
    }

    private RabbitConnectionFactoryBean getRabbitConnectionFactoryBean(
            RabbitProperties properties) throws Exception {
        PropertyMapper map = PropertyMapper.get();
        RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
        map.from(properties::determineHost).whenNonNull().to(factory::setHost);
        map.from(properties::determinePort).to(factory::setPort);
        map.from(properties::determineUsername).whenNonNull()
                .to(factory::setUsername);
        map.from(properties::determinePassword).whenNonNull()
                .to(factory::setPassword);
        map.from(properties::determineVirtualHost).whenNonNull()
                .to(factory::setVirtualHost);
        map.from(properties::getRequestedHeartbeat).whenNonNull()
                .asInt(Duration::getSeconds).to(factory::setRequestedHeartbeat);
        RabbitProperties.Ssl ssl = properties.getSsl();
        if (ssl.isEnabled()) {
            factory.setUseSSL(true);
            map.from(ssl::getAlgorithm).whenNonNull().to(factory::setSslAlgorithm);
            map.from(ssl::getKeyStoreType).to(factory::setKeyStoreType);
            map.from(ssl::getKeyStore).to(factory::setKeyStore);
            map.from(ssl::getKeyStorePassword).to(factory::setKeyStorePassphrase);
            map.from(ssl::getTrustStoreType).to(factory::setTrustStoreType);
            map.from(ssl::getTrustStore).to(factory::setTrustStore);
            map.from(ssl::getTrustStorePassword).to(factory::setTrustStorePassphrase);
        }
        map.from(properties::getConnectionTimeout).whenNonNull()
                .asInt(Duration::toMillis).to(factory::setConnectionTimeout);
        factory.afterPropertiesSet();
        return factory;
    }

}
