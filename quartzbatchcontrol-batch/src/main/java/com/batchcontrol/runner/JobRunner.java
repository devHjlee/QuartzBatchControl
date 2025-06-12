package com.batchcontrol.runner;

import com.batchcontrol.application.JobParameterLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
@Profile("!sync-only")
@RequiredArgsConstructor
public class JobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job job;
    private final JobParameterLoader jobParameterLoader;

    @Override
    public void run(String... args) throws Exception {
        Properties props = new Properties();
        for (String arg : args) {
            if (arg.contains("=")) {
                String[] split = arg.split("=", 2);
                props.setProperty(split[0], split[1]);
            }
        }

        JobParameters jobParameters = new DefaultJobParametersConverter().getJobParameters(props);
        JobParameters finalParams = jobParameterLoader.buildJobParameters(jobParameters);
        jobLauncher.run(job, finalParams);
    }
}
