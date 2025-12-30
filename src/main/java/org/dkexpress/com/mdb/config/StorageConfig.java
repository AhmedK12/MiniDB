package org.dkexpress.com.mdb.config;

import org.dkexpress.com.mdb.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;

@Configuration
public class StorageConfig {

    @Value("${storage.segment-size}")
    private long segmentSize;

    @Value("${storage.dir}")
    private String storageDir;

    @Bean
    public LogWriter logWriter() {
        return new LogWriter();
    }

    @Bean
    public LogReader logReader() {
        return new LogReader();
    }

    @Bean(destroyMethod = "close")
    @Scope("singleton")
    public MiniDB miniDBService(SegmentManagerService segmentManagerService,
                                LogWriter logWriter,
                                LogReader logReader, StartupRecovery startupRecovery) throws IOException {
        return new MiniDB(segmentManagerService, logWriter, logReader, startupRecovery);
    }

    @Bean
    public SegmentManagerService segmentManagerService() throws IOException {
        return new SegmentManagerService(segmentSize, storageDir);
    }


}