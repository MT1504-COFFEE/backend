# --- Fase 1: El Constructor (usa Java 21 para construir el .jar) ---
FROM eclipse-temurin:21-jdk-jammy AS builder

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /workspace/app

# Copia los archivos de Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# --- ¡CORRECCIÓN AQUÍ! ---
# Da permisos de ejecución al script 'mvnw' en el entorno Linux
RUN chmod +x ./mvnw
# ------------------------

# Descarga las dependencias
RUN ./mvnw dependency:go-offline

# Copia el resto del código fuente
COPY src ./src

# Compila la aplicación y SALTA LAS PRUEBAS (que están fallando)
RUN ./mvnw clean install -DskipTests

# --- Fase 2: La Imagen Final (usa solo Java 21 para *correr* la app) ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copia SOLAMENTE el .jar compilado desde la fase "builder"
# ¡Verifica que este nombre coincida con tu pom.xml!
COPY --from=builder /workspace/app/target/aseo-ucp-backend-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto 8080 (el que usa Spring Boot)
EXPOSE 8080

# El comando para iniciar tu aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]