#!/bin/bash
# Teste rápido da inicialização Solomon

echo "Compilando..."
javac -d bin -sourcepath src src/main/App.java

echo ""
echo "Executando teste curto (apenas geração 0)..."
timeout 30 java -cp bin main.App 1 2>&1 | grep -E "(Number of|População|Melhor Fitness|clientes|Solomon)" | head -30

echo ""
echo "Verificando solução R101..."
if [ -f "results_validation_R1/R101/evo_r101_exec01.txt" ]; then
    python3 scripts/validate_solution_rigorous.py src/instances/solomon/R101.txt results_validation_R1/R101/evo_r101_exec01.txt | grep -E "(VÁLIDA|INVÁLIDA|clientes|Distância|Veículos)"
fi
