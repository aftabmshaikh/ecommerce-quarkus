# 🛍️ E-Commerce Microservices with Quarkus

A modern, cloud-native e-commerce platform built with Quarkus, a Kubernetes Native Java framework optimized for GraalVM and HotSpot. This project demonstrates a full-stack microservices architecture with best practices in distributed systems, containerization, and cloud deployment using Quarkus.

## 🚀 Features

- **Reactive Architecture**: Built with Quarkus Reactive for non-blocking I/O
- **Fast Startup Time**: Optimized for Kubernetes with GraalVM Native Image support
- **Container-First**: Optimized for containerized environments
- **Developer Joy**: Live coding with dev mode
- **Kubernetes-Native**: First-class Kubernetes integration
- **Event-Driven**: Built-in support for Apache Kafka and Reactive Messaging
- **Resilient**: Circuit breakers, retries, and fallbacks with SmallRye Fault Tolerance
- **Observability**: Built-in metrics, health checks, and distributed tracing
- **API Documentation**: OpenAPI/Swagger for all services

## 🛠️ Tech Stack

### Backend (Quarkus Microservices)
- **Quarkus 3.x** - Supersonic Subatomic Java framework
- **RESTEasy Reactive** - Non-blocking REST endpoints
- **Hibernate Reactive with Panache** - Reactive data access
- **SmallRye Reactive Messaging** - Event-driven architecture
- **SmallRye Fault Tolerance** - Circuit breaking and resilience patterns
- **Quarkus REST Client** - Type-safe service communication
- **Quarkus Config** - Unified configuration
- **Lombok** - Boilerplate reduction
- **MapStruct** - Object mapping
- **Testcontainers** - Integration testing

### Database
- **PostgreSQL** - Primary database
- **MongoDB** - Document storage
- **Redis** - Caching and session management

### Message Broker
- **Apache Kafka** - Event streaming platform

### Observability
- **Micrometer** - Application metrics
- **OpenTelemetry** - Distributed tracing
- **Prometheus** - Metrics collection
- **Grafana** - Metrics visualization
- **Jaeger** - Distributed tracing

### Security
- **Quarkus Security** - Authentication and authorization
- **JWT** - Stateless authentication
- **OAuth 2.0** - Authorization framework

## 📋 Prerequisites

### Local Development
- **Docker** (v20.10+) and **Docker Compose** (v2.0+)
- **Java 17** or later
- **Maven** 3.9+ or **Gradle** 7.6+
- **Node.js** 18+ and **npm** 9+ (for frontend)
- **Git**

### Optional
- **GraalVM** (for native compilation)
- **kubectl** (for Kubernetes deployment)
- **Minikube** or **kind** (for local Kubernetes)

## 🏗️ Project Structure

```
ecommerce-quarkus/
├── api-gateway/           # API Gateway service
├── cart-service/          # Shopping cart service
├── inventory-service/     # Product inventory service
├── notification-service/  # Email and notifications
├── order-service/         # Order processing
├── payment-service/       # Payment processing
├── product-service/       # Product catalog
└── user-service/          # User management and auth
```

## 🚀 Quick Start

### 1. Start Infrastructure

```bash
# Start required infrastructure (PostgreSQL, Redis, Kafka, etc.)
docker-compose -f docker-compose-infra.yml up -d
```

### 2. Build and Run Services

#### Option 1: Development Mode (Hot Reload)

```bash
# In separate terminal windows
cd api-gateway && mvn quarkus:dev
cd cart-service && mvn quarkus:dev
# Repeat for other services
```

#### Option 2: Build and Run with Maven

```bash
# Build all services
mvn clean install

# Run a service
java -jar target/quarkus-app/quarkus-run.jar
```

### 3. Access Services

- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Health Checks**: http://localhost:8080/q/health
- **Metrics**: http://localhost:8080/q/metrics
- **Dev UI**: http://localhost:8080/q/dev

## 🛠️ Development

### Live Coding

Quarkus provides a development mode that supports live coding:

```bash
mvn compile quarkus:dev
```

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn verify -DskipUnitTests

# Native tests (requires GraalVM)
mvn verify -Pnative
```

### Building Native Executables

```bash
# Install GraalVM and set GRAALVM_HOME
mvn package -Pnative -Dquarkus.native.container-build=true
```

## 📦 Containerization

### Building Container Images

```bash
# Build JVM image
mvn package -Dquarkus.container-image.build=true

# Build native image (requires GraalVM)
mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
```

### Docker Compose

```bash
# Start all services
docker-compose up -d

# Stop services
docker-compose down
```

## ☁️ Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (Minikube, EKS, GKE, AKS, etc.)
- kubectl configured to access your cluster
- Helm (for package management)

### Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace ecommerce

# Deploy infrastructure (Kafka, PostgreSQL, etc.)
helm install kafka bitnami/kafka -n ecommerce
helm install postgresql bitnami/postgresql -n ecommerce

# Deploy microservices
kubectl apply -f k8s/
```

## 📊 Monitoring and Observability

### Access Monitoring Tools

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Jaeger**: http://localhost:16686

### View Logs

```bash
# View logs for a specific service
kubectl logs -f deployment/api-gateway -n ecommerce

# View logs for all services
kubectl logs -f -l app.kubernetes.io/part-of=ecommerce -n ecommerce
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Quarkus Team for the amazing framework
- Red Hat for their support of open source
- All contributors who have helped improve this project
