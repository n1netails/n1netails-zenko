# N1netails

<div align="center">
  <img src="https://raw.githubusercontent.com/n1netails/n1netails/refs/heads/main/n1netails_icon_transparent.png" alt="N1ne Tails" width="500" style="display: block; margin: auto;"/>
</div>

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Zenko — Real-time Log Tailing & Alerting Service

Zenko is a log tailing service that monitors your log files in real-time and sends alerts to the N1netails platform based on user-defined keywords and exception stack traces. It helps you detect critical issues quickly across various programming languages.

---

## Supported Languages

Zenko can identify and track exception logs for:

* Java
* Python
* C#
* PHP
* JavaScript / TypeScript
* Ruby
* Go

---

## Getting Started with Zenko

Zenko's main purpose is to tail log files and send alert data (called *tail alerts*) to the `n1netails-api`. These alerts are triggered when configured keywords appear in your logs.

### Default Keywords

If you don’t configure your own, Zenko uses these common keywords:

```
ERROR, Exception, CRITICAL, FATAL, WARN, Traceback, FAIL, Failure, PANIC
```

### Requirements

Zenko works best alongside the N1netails core system, but it can also run as a simple tailing service that you can plug into your own system for keyword-based alerts.
Learn more about the core system here: [N1netails](https://github.com/n1netails/n1netails)

---

## Configuration — Environment Variables

| Variable                           | Description                                       | Default                                                            |
| ---------------------------------- | ------------------------------------------------- | ------------------------------------------------------------------ |
| `PORT`                             | Application port                                  | 9902                                                               |
| `N1NETAILS_ZENKO_LOGTAIL_FILES`    | Comma-separated list of log file paths to monitor | *No default*                                                       |
| `N1NETAILS_ZENKO_LOGTAIL_KEYWORDS` | Comma-separated alert keywords                    | `ERROR,Exception,CRITICAL,FATAL,WARN,Traceback,FAIL,Failure,PANIC` |
| `N1NETAILS_API_ALERT_ENDPOINT`     | URL endpoint to send alert data                   | `http://localhost:9901/ninetails/alert`                            |
| `N1NETAILS_API_ALERT_TOKEN`        | Optional token for authentication (n1ne-token)    | *Optional*                                                         |

**Example log file paths:**

* Unix: `/logs/service1/app.log,/logs/service2/other.log`
* Windows: `C:\\logs\\service1\\app.log,C:\\logs\\service2\\log\\other.log`

---

## Deployment

### Using Docker

You can quickly deploy Zenko with Docker using the included [`docker-compose.yml`](./docker-compose.yml).

#### Quick Start

1. **Clone the repository** (if you haven’t already):

   ```bash
   git clone https://github.com/n1netails/n1netails-zenko.git
   cd n1netails
   ```

2. **Edit `docker-compose.yml`** to:
  - Set your actual log file paths under `N1NETAILS_ZENKO_LOGTAIL_FILES`.
  - Map your host log directories to the container using `volumes`.  
    Example:
    ```yaml
    volumes:
      - /path/to/your/service1/logs:/logs/service1:ro
      - /path/to/your/service2/logs:/logs/service2:ro
    ```
   This allows Zenko inside the container to access your log files for monitoring.

3. **Start Zenko service:**

   ```bash
   docker-compose up --build -d
   ```

4. **Verify Zenko is running:**

   ```bash
   docker-compose logs -f zenko
   ```

5. Zenko will start monitoring your specified logs and send alerts to the configured API endpoint.

To stop and remove containers:

```bash
docker-compose down -v
```

---

### Useful Docker Commands

Build and run the docker container

#### docker compose
```shell
docker-compose up --build
```

#### Remove docker containers
```bash
docker-compose down -v 
```

### Deployment with Jar file
Requires java 17 to be installed on your server
```bash
java \
-DPORT=9902 \
-DN1NETAILS_ZENKO_LOGTAIL_FILES=/logs/service1/app.log,/logs/service2/other.log \
-DN1NETAILS_ZENKO_LOGTAIL_KEYWORDS=ERROR,Exception,CRITICAL,FATAL,WARN,Traceback,FAIL,Failure,PANIC \
-DN1NETAILS_API_ALERT_ENDPOINT=http://localhost:9901/ninetails/alert \
-DN1NETAILS_API_ALERT_TOKEN=your-n1ne-token \
-jar target/n1netails-zenko.jar
```

---

## Development

### Build the project

```bash
mvn clean install
```

### Run with environment variables

```bash
mvn spring-boot:run \
  -DPORT=9902 \
  -DN1NETAILS_ZENKO_LOGTAIL_FILES=/projects/cyberchefai/cyberchefai-log.txt,/projects/cheflei-service/cheflei-service-log.txt \
  -DN1NETAILS_ZENKO_LOGTAIL_KEYWORDS=ERROR,Exception,CRITICAL,FATAL,WARN,Traceback,FAIL,Failure,PANIC \
  -DN1NETAILS_API_ALERT_ENDPOINT=http://localhost:9901/ninetails/alert \
  -DN1NETAILS_API_ALERT_TOKEN=your-n1ne-token
```

---

## How Zenko Detects Stack Traces

Zenko uses specific regular expressions tailored for different languages to identify exceptions and stack trace lines.

### Java

```regexp
^\s*at\s.+|^Caused by:.*
```

* Matches stack frames like `at com.example.Class.method(Class.java:123)`
* Also matches `Caused by:` lines in exceptions

### Python

```regexp
^\s*File ".+", line \d+, in .+
```

* Matches traceback lines like `File "/path/to/file.py", line 27, in function`

### C\#

```regexp
^\s*at\s.+
```

* Matches stack frames like `at Namespace.Class.Method() in File.cs:line 42`

### PHP

```regexp
^#\d+\s+.+\(.+\):\s?.*
```

* Matches trace entries like `#1 /var/www/html/index.php(15): my_function()`

### JavaScript / TypeScript

```regexp
^\s*at\s(?:.+\s\()?[^()]+\.js:\d+:\d+\)?$
```

* Matches stack frames with or without function names, e.g.
  `at Object.<anonymous> (/path/file.js:10:15)`

### Ruby

```regexp
^\s*from\s.+\.rb:\d+:in\s`.*'$
```

* Matches frames like `from my_script.rb:42:in 'my_method'`

### Go

```regexp
^\s*.+\.go:\d+\s\+0x[0-9a-fA-F]+$
```

* Matches frames like `/path/to/file.go:123 +0x45f`

---

## Custom Integration

You can also integrate Zenko with your own service. Just provide:

* An HTTP endpoint to receive alert requests
* Optional `N1ne-Token` header for authentication
* Support for the Alert Tail JSON payload (example below)

### Alert Tail JSON example

```json
{
  "title": "string",
  "description": "string",
  "details": "string",
  "timestamp": "2025-08-09T22:06:54.799Z",
  "level": "string",
  "type": "string",
  "metadata": {
    "your-additionalProp1": "string",
    "your-additionalProp2": "string",
    "your-additionalProp3": "string"
  }
}
```

---

## Support & Community

For help or to discuss:

* Open a GitHub issue
* Join our Discord community

[![Join our Discord](https://img.shields.io/badge/Join_Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/ma9CCw7F2x)

---

## Contributing

We welcome contributions!
Please follow our [CONTRIBUTING.md](./contributing.md) guidelines.

