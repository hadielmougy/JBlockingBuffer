# JBlockingBuffer

Concurrent buffer

```xml
<dependency>
  <groupId>io.github</groupId>
  <artifactId>jblockingbuffer</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>

```

## Limit buffer only by time 
```java
// buffer size -1 means unlimited size
BlockingBuffer<Integer> buffer = new BlockingBuffer<>(-1, Duration.ofMillis(500));
```

## Limit buffer by size and time
```java
BlockingBuffer<Integer> buffer = new BlockingBuffer<>(10, Duration.ofMillis(500));
```

### defaults
-  size is 10
-  time is 100 millis
