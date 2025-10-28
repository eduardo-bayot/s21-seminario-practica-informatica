# Compilar y ejecutar
mvn clean compile exec:java -Dexec.mainClass="com.moderacion.Main"

# O solo ejecutar (si ya compiló)
mvn exec:java -Dexec.mainClass="com.moderacion.Main"

# Iniciar la base de datos si no está corriendo
docker compose up -d

# Detener la base de datos
docker compose down