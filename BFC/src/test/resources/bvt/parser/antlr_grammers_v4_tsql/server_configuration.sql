ALTER SERVER CONFIGURATION   
SET PROCESS AFFINITY CPU=0 TO 63, 128 TO 191; 
ALTER SERVER CONFIGURATION
SET PROCESS AFFINITY NUMANODE=0, 7;
ALTER SERVER CONFIGURATION
SET PROCESS AFFINITY NUMANODE=0, 7;
ALTER SERVER CONFIGURATION SET PROCESS AFFINITY CPU=0;
ALTER SERVER CONFIGURATION
SET PROCESS AFFINITY CPU=AUTO;
ALTER SERVER CONFIGURATION SET DIAGNOSTICS LOG ON;
ALTER SERVER CONFIGURATION SET DIAGNOSTICS LOG OFF;
ALTER SERVER CONFIGURATION
SET DIAGNOSTICS LOG PATH = 'C:\logs';
ALTER SERVER CONFIGURATION
SET DIAGNOSTICS LOG MAX_SIZE = 10 MB;
ALTER SERVER CONFIGURATION
SET FAILOVER CLUSTER PROPERTY HealthCheckTimeout = 15000;
ALTER SERVER CONFIGURATION SET HADR CLUSTER CONTEXT = 'clus01.xyz.com';
ALTER SERVER CONFIGURATION
SET BUFFER POOL EXTENSION ON
    (FILENAME = 'F:\SSDCACHE\Example.BPE', SIZE = 50 GB);
ALTER SERVER CONFIGURATION
SET BUFFER POOL EXTENSION OFF;
GO
ALTER SERVER CONFIGURATION
SET BUFFER POOL EXTENSION ON
    (FILENAME = 'F:\SSDCACHE\Example.BPE', SIZE = 60 GB);
GO

