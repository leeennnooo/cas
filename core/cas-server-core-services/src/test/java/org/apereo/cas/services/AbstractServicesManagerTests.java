package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AbstractServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class AbstractServicesManagerTests<T extends ServicesManager> {
    private static final String TEST = "test";

    protected final List<RegisteredService> listOfDefaultServices = new ArrayList<>();

    protected ServiceRegistry serviceRegistry;

    protected ServicesManager servicesManager;

    public AbstractServicesManagerTests() {
        val r = new RegexRegisteredService();
        r.setId(2500);
        r.setServiceId("serviceId");
        r.setName("serviceName");
        r.setEvaluationOrder(1000);
        listOfDefaultServices.add(r);
    }

    @BeforeEach
    public void initialize() {
        this.serviceRegistry = getServiceRegistryInstance();
        this.servicesManager = getServicesManagerInstance();
        this.servicesManager.load();
    }

    protected ServicesManager getServicesManagerInstance() {
        var chain = new ChainingServicesManager();
        var manager = new DefaultServicesManager(serviceRegistry, mock(ApplicationEventPublisher.class), new HashSet<>());
        chain.registerServiceManager(manager);
        return chain;
    }

    protected ServiceRegistry getServiceRegistryInstance() {
        return new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class), registeredServicesFixture().get(), new ArrayList<>());
    }

    protected Supplier<List<RegisteredService>> registeredServicesFixture() {
        return () -> listOfDefaultServices;
    }

    @Test
    public void verifySaveAndGet() {
        val services = new RegexRegisteredService();
        services.setId(1000);
        services.setName(TEST);
        services.setServiceId(TEST);
        servicesManager.save(services);
        assertNotNull(this.servicesManager.findServiceBy(1000));
        assertTrue(this.servicesManager.count() > 0);
    }

    @Test
    public void verifyDelete() {
        val r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);
        this.servicesManager.save(r);
        assertNotNull(this.servicesManager.findServiceBy(r.getServiceId()));
        this.servicesManager.delete(r);
        assertNull(this.servicesManager.findServiceBy(r.getId()));
    }

    @Test
    public void verifyExpiredNotify() {
        val r = new RegexRegisteredService();
        r.setId(2000);
        r.setName(TEST);
        r.setServiceId(TEST);
        val expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        expirationPolicy.setNotifyWhenExpired(true);
        expirationPolicy.setExpirationDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        r.setExpirationPolicy(expirationPolicy);
        this.servicesManager.save(r);
        assertNotNull(this.servicesManager.findServiceBy(r.getServiceId()));
    }

    @Test
    public void verifyExpiredNotifyAndDelete() {
        val r = new RegexRegisteredService();
        r.setId(2001);
        r.setName(TEST);
        r.setServiceId(TEST);
        val expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        expirationPolicy.setNotifyWhenExpired(true);
        expirationPolicy.setExpirationDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        expirationPolicy.setDeleteWhenExpired(true);
        expirationPolicy.setNotifyWhenDeleted(true);
        r.setExpirationPolicy(expirationPolicy);
        this.servicesManager.save(r);
        assertNull(this.servicesManager.findServiceBy(r.getServiceId()));
    }


}
