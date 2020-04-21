package tech.kuiperbelt.lib.common.jpa;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tech.kuiperbelt.lib.common.IntegrationTest;
import tech.kuiperbelt.lib.common.domain.Foo;
import tech.kuiperbelt.lib.common.domain.FooRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

class BaseRepositoryTest extends IntegrationTest {

    @Autowired
    private FooRepository fooRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Transactional
    @Test
    void happyPath() {
        Foo newFoo = Foo.builder()
                .aaa(RandomStringUtils.random(10))
                .aDecimal(new BigDecimal(RandomUtils.nextDouble(0,100)))
                .bbb(RandomUtils.nextBoolean())
                .aFloat(RandomUtils.nextFloat())
                .aDouble(RandomUtils.nextDouble())
                .ccc(RandomUtils.nextInt())
                .aLocalDate(LocalDate.now())
                .aLocalDateTime(LocalDateTime.now())
                .build();
        Foo savedFoo = fooRepository.save(newFoo);
        assertThat(savedFoo.getId(), greaterThan(0L));
        assertThat(savedFoo.getAaa(), equalTo(newFoo.getAaa()));

        assertThat(fooRepository.findAll(), hasSize(1));

        Optional<Foo> byId = fooRepository.findById(savedFoo.getId());
        assertTrue(byId.isPresent());

        double newDouble = RandomUtils.nextDouble();
        byId.get().setADouble(newDouble);

        Optional<Foo> byIdAfterChanged = fooRepository.findById(savedFoo.getId());
        assertThat(byIdAfterChanged.get().getADouble(), equalTo(newDouble));
    }

    @Transactional
    @Test
    void findByFilterWithDataType() {
        Foo newFoo = Foo.builder()
                .aaa(RandomStringUtils.random(10))
                .aDecimal(new BigDecimal(RandomUtils.nextDouble(0,100)).setScale(2, RoundingMode.HALF_UP))
                .bbb(RandomUtils.nextBoolean())
                .aFloat(4.35F)
                .aDouble(4.35)
                .ccc(RandomUtils.nextInt())
                .aLocalDate(LocalDate.now())
                .aLocalDateTime(LocalDateTime.now())
                .status(Foo.Status.ENABLED)
                .build();
        fooRepository.save(newFoo);

        String filterStr = "aaa==" + newFoo.getAaa();
        Page<Foo> byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

        filterStr = "ccc==" + newFoo.getCcc().toString();
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));


        filterStr = "aDecimal==" + newFoo.getADecimal().toPlainString();
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

        filterStr = "bbb==" + newFoo.getBbb().toString();
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

        //TODO aFloat==4.35 will not work
        filterStr = "aFloat>=4.34";
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

        filterStr = "aDouble==" + newFoo.getADouble().toString();
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

        filterStr = "status==" + Foo.Status.ENABLED.name();
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

        filterStr = "aLocalDate==" + newFoo.getALocalDate().format(DateTimeFormatter.ISO_DATE);
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

        filterStr = "aLocalDateTime==" + newFoo.getALocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME);
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

    }

    @Transactional
    @Test
    void findByFilterWithLogicOperation() {
        Foo newFoo = Foo.builder()
                .aaa(RandomStringUtils.random(10))
                .aDecimal(new BigDecimal(RandomUtils.nextDouble(0,100)).setScale(2, RoundingMode.HALF_UP))
                .bbb(RandomUtils.nextBoolean())
                .aFloat(4.35F)
                .aDouble(4.35)
                .ccc(RandomUtils.nextInt())
                .aLocalDate(LocalDate.now())
                .aLocalDateTime(LocalDateTime.now())
                .status(Foo.Status.ENABLED)
                .build();
        fooRepository.save(newFoo);

        String filterStr = "aaa==" + newFoo.getAaa() + " and ccc==" + newFoo.getCcc().toString();
        Page<Foo> byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

        filterStr = "aaa!=" + newFoo.getAaa() + " or ccc==" + newFoo.getCcc().toString();
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));

        filterStr = "aaa==" + newFoo.getAaa() + " and (ccc!=" + newFoo.getCcc().toString() + " or status==" + newFoo.getStatus().name() + ")";
        byFilter = fooRepository.findByFilter(filterStr, PageRequest.of(0, 10));
        assertThat(byFilter.getTotalElements(), equalTo(1L));
    }

    /**
     * without @Transaction, we need control transaction to trigger Audit Listener
     */
    @Test
    void findVersion() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        Foo savedFoo = transactionTemplate.execute(transactionStatus -> {
            Foo newFoo = Foo.builder()
                    .aaa(RandomStringUtils.random(10))
                    .aDecimal(new BigDecimal(RandomUtils.nextDouble(0, 100)).setScale(2, RoundingMode.HALF_UP))
                    .bbb(RandomUtils.nextBoolean())
                    .aFloat(4.35F)
                    .aDouble(4.35)
                    .ccc(RandomUtils.nextInt())
                    .aLocalDate(LocalDate.now())
                    .aLocalDateTime(LocalDateTime.now())
                    .status(Foo.Status.ENABLED)
                    .build();
            return fooRepository.save(newFoo);
        });

        Page<Foo> allVersions = transactionTemplate.execute(transactionStatus ->
                fooRepository.findAllVersions(savedFoo.getId(), PageRequest.of(0, 100)));

        assertThat(allVersions.getTotalElements(), equalTo(1L));

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Foo byId = fooRepository.getById(savedFoo.getId());
            byId.setCcc(RandomUtils.nextInt());
        });

        allVersions = transactionTemplate.execute(transactionStatus ->
                fooRepository.findAllVersions(savedFoo.getId(), PageRequest.of(0, 100)));
        assertThat(allVersions.getTotalElements(), equalTo(2L));

        Optional<Foo> oldFoo = transactionTemplate.execute(transactionStatus -> fooRepository.findPriorVersion(savedFoo.getId()));
        assertTrue(oldFoo.isPresent());
        assertThat(oldFoo.get().getCcc(), equalTo(savedFoo.getCcc()));

        //clear DB
        transactionTemplate.executeWithoutResult(transactionStatus -> fooRepository.deleteAll());
    }
}