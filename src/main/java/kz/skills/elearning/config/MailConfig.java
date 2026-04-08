package kz.skills.elearning.config;

import kz.skills.elearning.service.email.InMemoryMailDeliveryService;
import kz.skills.elearning.service.email.LoggingMailDeliveryService;
import kz.skills.elearning.service.email.MailDeliveryService;
import kz.skills.elearning.service.email.SmtpMailDeliveryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@EnableConfigurationProperties({AppMailProperties.class, EmailVerificationProperties.class})
public class MailConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "smtp")
    public MailDeliveryService smtpMailDeliveryService(JavaMailSender javaMailSender,
                                                       AppMailProperties appMailProperties,
                                                       MailProperties mailProperties) {
        return new SmtpMailDeliveryService(javaMailSender, appMailProperties, mailProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "in-memory")
    public InMemoryMailDeliveryService inMemoryMailDeliveryService() {
        return new InMemoryMailDeliveryService();
    }

    @Bean
    @ConditionalOnMissingBean(MailDeliveryService.class)
    public MailDeliveryService loggingMailDeliveryService() {
        return new LoggingMailDeliveryService();
    }
}
