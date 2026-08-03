package com.javarush.jira.bugtracking.task;

import com.javarush.jira.BaseTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Sql(scripts = {"classpath:db/changelog.sql", "classpath:data.sql"}, config = @SqlConfig(encoding = "UTF-8"))
class ActivityServiceTest extends BaseTests {

    @Autowired
    private ActivityService activityService;

    @Test
    void getTimeInWork_happyPath() {
        Optional<Duration> timeInWork = activityService.getTimeInWork(6L);
        assertTrue(timeInWork.isPresent());
        assertEquals(Duration.ofHours(4), timeInWork.get());
    }

    @Test
    void getTimeInTesting_happyPath() {
        Optional<Duration> timeInTesting = activityService.getTimeInTesting(6L);
        assertTrue(timeInTesting.isPresent());
        assertEquals(Duration.ofHours(4), timeInTesting.get());
    }

    @Test
    void getTimeInWork_missingReadyForReview() {
        Optional<Duration> timeInWork = activityService.getTimeInWork(2L);
        assertTrue(timeInWork.isEmpty());
    }

    @Test
    void getTimeInTesting_missingDone() {
        Optional<Duration> timeInTesting = activityService.getTimeInTesting(1L);
        assertTrue(timeInTesting.isEmpty());
    }

    @Test
    void getTimeInWork_noActivities() {
        Optional<Duration> timeInWork = activityService.getTimeInWork(999L);
        assertTrue(timeInWork.isEmpty());
    }

    @Test
    void getTimeInTesting_noActivities() {
        Optional<Duration> timeInTesting = activityService.getTimeInTesting(999L);
        assertTrue(timeInTesting.isEmpty());
    }
}