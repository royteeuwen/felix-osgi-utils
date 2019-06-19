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
