package kr.pullgo.pullgoserver.service.cron;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Slf4j
@EnableScheduling
@Component
public class CronJob {

    private final TaskScheduler scheduler;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();


    @Autowired
    public CronJob(
        TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void register(Long id, Runnable runnable, LocalDateTime endTime, String msg) {
        ScheduledFuture<?> task = scheduler.schedule(getRunnable(id, runnable, msg),
            Timestamp.valueOf(endTime));
        scheduledTasks.put(id, task);
        log.info("regiser " + msg + " timer, id: [" + id + "], until :" + endTime + '\n');
        log.info("present time : " + LocalDateTime.now() + '\n');
    }


    private Runnable getRunnable(Long id, Runnable runnable, String msg) {
        return () -> {
            log.info(
                "start [" + msg + "] - [" + id + "] cronjob, at: " + LocalDateTime.now() + "\n");
            runnable.run();
            remove(id, msg);
        };
    }


    public void remove(Long id, String msg) {
        scheduledTasks.get(id).cancel(true);
        log.info("remove " + msg + " timer, id: [" + id + "]" + '\n');
    }
}


