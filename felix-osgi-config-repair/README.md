# Find Duplicate Configurations
This is a java program that finds all duplicate OSGi Factory configurations on the file system.

```
Usage:
    java -jar felix-osgi-config-repair.jar /path/to/config/directory [--delete]

  e.g. output duplicate config files without deletion:
    java -jar felix-osgi-config-repair.jar launchpad/config

  e.g. output duplicate config files with deletion:
    java -jar felix-osgi-config-repair.jar launchpad/config --delete
```

This is to address some scenarios with AEM and Apache Sling installations where factory configs get duplicated when instances are copied without preserving timestamps.  In the past there were also some bugs that caused duplicate factory configurations. For example: https://jira.apache.org/jira/browse/SLING-6313
