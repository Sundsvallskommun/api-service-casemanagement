package se.sundsvall.casemanagement.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
class EventPublisherConfiguration {

    @Bean(name = "applicationEventMulticaster")
    ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        var eventMulticaster = new SimpleApplicationEventMulticaster();
        var taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setTaskDecorator(new MDCTaskDecorator());
        eventMulticaster.setTaskExecutor(taskExecutor);
        return eventMulticaster;
    }
}
