package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    private static Connection cn;

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            Properties loadProperties = loadProperties();
            try (Connection connection = init(loadProperties)) {
                JobDataMap data = new JobDataMap();
                data.put("connect", connection);

                JobDetail job = newJob(Rabbit.class).usingJobData(data).build();
                int interval = Integer.parseInt(loadProperties.getProperty("rabbit.interval"));
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(interval)
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10000);
                scheduler.shutdown();
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private static Properties loadProperties() {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return config;
    }

    private static Connection init(Properties properties) throws SQLException, ClassNotFoundException {
        Class.forName(properties.getProperty("driver-class-name"));
        cn = DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password")
        );
        return cn;
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try (PreparedStatement statement = (
                    (Connection) context.getJobDetail().getJobDataMap().get("connect")
            )
                    .prepareStatement("insert into rabbit (created_date) values (?)")) {
                long millis = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(millis);
                LocalDateTime localDateTime = timestamp.toLocalDateTime();
                statement.setTimestamp(1, Timestamp.valueOf(localDateTime));
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Rabbit runs here ...");
        }
    }
}