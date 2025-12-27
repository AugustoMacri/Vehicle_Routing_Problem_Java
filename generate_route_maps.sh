#!/bin/bash

# Script para gerar mapas de rotas após executar instâncias
# Uso: ./generate_route_maps.sh [instance_name]

INSTANCES_DIR="src/instances/solomon"

if [ $# -eq 0 ]; then
    echo "=========================================="
    echo "Gerador de Mapas de Rotas VRP"
    echo "=========================================="
    echo ""
    echo "Uso: ./generate_route_maps.sh <instance_name>"
    echo ""
    echo "Exemplos:"
    echo "  ./generate_route_maps.sh c101     # Gera mapas para C101"
    echo "  ./generate_route_maps.sh r101     # Gera mapas para R101"
    echo "  ./generate_route_maps.sh all_c1   # Gera mapas para todas as C1 (C101-C109)"
    echo ""
    exit 1
fi

INSTANCE_PARAM=$1

generate_single_map() {
    local instance_name=$1
    local instance_upper=$(echo "$instance_name" | tr '[:lower:]' '[:upper:]')
    
    echo ""
    echo "Gerando mapas para instância: $instance_upper"
    echo "----------------------------------------"
    
    if [ -f "resultsMulti/evo_${instance_name}.txt" ]; then
        python3 scripts/plot_route_maps.py \
            --instance "$instance_upper" \
            --results-dir resultsMulti \
            --instances-dir "$INSTANCES_DIR" \
            --output-dir resultsMulti/route_maps
        
        if [ $? -eq 0 ]; then
            echo "✓ Mapas gerados com sucesso!"
            echo "  - Inicial: resultsMulti/route_maps/${instance_upper}/route_maps/route_map_${instance_name}_initial.png"
            echo "  - Final:   resultsMulti/route_maps/${instance_upper}/route_maps/route_map_${instance_name}_final.png"
        else
            echo "✗ Erro ao gerar mapas para $instance_upper"
        fi
    else
        echo "✗ Arquivo resultsMulti/evo_${instance_name}.txt não encontrado!"
        echo "  Execute primeiro: ./run_single_instance.sh <numero_instancia>"
    fi
}

# Processar comando
case "$INSTANCE_PARAM" in
    all_c1)
        echo "=========================================="
        echo "Gerando mapas para TODAS as instâncias C1"
        echo "=========================================="
        
        for i in {101..109}; do
            generate_single_map "c${i}"
        done
        
        echo ""
        echo "=========================================="
        echo "Geração de mapas concluída!"
        echo "=========================================="
        ;;
    
    all_r1)
        echo "=========================================="
        echo "Gerando mapas para TODAS as instâncias R1"
        echo "=========================================="
        
        for i in {101..109}; do
            generate_single_map "r${i}"
        done
        
        echo ""
        echo "=========================================="
        echo "Geração de mapas concluída!"
        echo "=========================================="
        ;;
    
    all_rc1)
        echo "=========================================="
        echo "Gerando mapas para TODAS as instâncias RC1"
        echo "=========================================="
        
        for i in {101..108}; do
            generate_single_map "rc${i}"
        done
        
        echo ""
        echo "=========================================="
        echo "Geração de mapas concluída!"
        echo "=========================================="
        ;;
    
    *)
        # Instância individual
        generate_single_map "$INSTANCE_PARAM"
        ;;
esac
