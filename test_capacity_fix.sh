#!/bin/bash
# Script para testar a correção do bug de capacidade

echo "==================================================================="
echo "TESTE DO BUG DE CAPACIDADE - Pós-Correção"
echo "==================================================================="
echo ""
echo "Data: $(date)"
echo "Executando C101 com a correção aplicada..."
echo ""

cd /home/augusto/Desktop/VRPs/Vehicle_Routing_Problem_Java

# Recompilar
echo "1. Recompilando o projeto..."
javac -d bin -sourcepath src src/main/App.java 2>&1
if [ $? -ne 0 ]; then
    echo "ERRO: Falha na compilação!"
    exit 1
fi
echo "   ✓ Compilação OK"
echo ""

# Executar uma instância de teste
echo "2. Executando C101 (será interrompido após 30 segundos)..."
timeout 30 java -cp bin main.App <<EOF > /tmp/test_c101_output.txt 2>&1
4
2
100
C101
EOF

echo "   ✓ Execução completada"
echo ""

# Verificar o resultado
echo "3. Verificando violações de capacidade..."
if [ -f "resultsMulti/evo_c101.txt" ]; then
    violations=$(grep -E "Demanda: [0-9]+/200" resultsMulti/evo_c101.txt | \
                 awk -F'Demanda: |/200' '{if ($2 > 200) print "   ✗ Veículo com " $2 "/200 (+" ($2-200) " acima)"}')
    
    if [ -z "$violations" ]; then
        echo "   ✓ SUCESSO: Nenhuma violação de capacidade encontrada!"
    else
        echo "   ✗ FALHA: Violações encontradas:"
        echo "$violations"
    fi
else
    echo "   ⚠ Arquivo de resultado não encontrado"
fi

echo ""
echo "==================================================================="
echo "Para validação completa, execute:"
echo "  python3 scripts/validate_capacity.py"
echo "==================================================================="
