#!/bin/bash

# Script para executar uma única instância do VRP
# Uso: ./run_single_instance.sh <numero_da_instancia>
# Exemplo: ./run_single_instance.sh 1  (executa C101)

if [ $# -eq 0 ]; then
    echo "Erro: Nenhuma instância especificada!"
    echo ""
    echo "Uso: ./run_single_instance.sh <numero_da_instancia>"
    echo ""
    echo "Instâncias disponíveis:"
    echo "  1-9   : C101 a C109"
    echo "  18-26 : R101 a R109"
    echo "  41-48 : RC101 a RC108"
    echo ""
    echo "Exemplo: ./run_single_instance.sh 1  (executa C101)"
    exit 1
fi

INSTANCE_NUM=$1

echo "=========================================="
echo "Executor de Instância VRP"
echo "=========================================="
echo ""

# Compilar o projeto se necessário
if [ ! -d "bin" ] || [ ! -f "bin/main/App.class" ]; then
    echo "Compilando o projeto..."
    javac -d bin -sourcepath src src/main/App.java
    
    if [ $? -ne 0 ]; then
        echo "Erro na compilação! Abortando execução."
        exit 1
    fi
    
    echo "Compilação concluída com sucesso!"
    echo ""
fi

echo "Executando instância número: $INSTANCE_NUM"
echo ""

# Executar o programa Java
java -cp bin main.App $INSTANCE_NUM

if [ $? -ne 0 ]; then
    echo ""
    echo "ERRO: Falha ao executar a instância"
    exit 1
fi

echo ""
echo "✓ Execução concluída com sucesso!"
echo ""
