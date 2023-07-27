# JBlockingBuffer

Concurrent blocking buffer

```xml
<dependency>
  <groupId>io.github</groupId>
  <artifactId>jblockingbuffer</artifactId>
  <version>1.0</version>
</dependency>

```

## Buffer by time 
```java
// buffer size -1 means unlimited size
BlockingBuffer<Integer> buffer = new BlockingBuffer<>(-1, Duration.ofMillis(500));
buffer.add(1);
var list = buffer.get();
```

## Buffer by size and time (whatever happens first)
```java
BlockingBuffer<Integer> buffer = new BlockingBuffer<>(10, Duration.ofMillis(500));
buffer.add(1);
// will release after 500 millis
var list = buffer.get();
```

### defaults
-  size is 10
-  time is 100 millis
