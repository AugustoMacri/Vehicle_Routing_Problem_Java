#!/bin/bash
# filepath: run_multiple.sh

# Número de execuções desejadas
RUNS=10

# Diretório onde o projeto está localizado
PROJECT_DIR="/home/augusto/Desktop/VRPs/Vehicle_Routing_Problem_Java"

# Entrar no diretório do projeto
cd "$PROJECT_DIR"

# Verificar se o diretório bin existe, caso contrário, criar
if [ ! -d "bin" ]; then
    mkdir -p bin
fi

# Compilar o projeto
echo "Compilando o projeto..."
javac -d bin -cp src src/main/App.java src/genetic/*.java src/vrp/*.java src/configuration/*.java

# Executar o programa várias vezes
echo "Executando o programa $RUNS vezes..."

for ((i=1; i<=$RUNS; i++)); do
    echo "=== Execução $i de $RUNS ==="
    
    # Executar o programa
    java -cp bin main.App
    
    echo "Execução $i concluída."
    echo ""
done

echo "Todas as execuções concluídas!"