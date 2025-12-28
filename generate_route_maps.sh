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
    echo "  ./generate_route_maps.sh c101              # Gera mapas para C101 (resultsMulti)"
    echo "  ./generate_route_maps.sh r101              # Gera mapas para R101 (resultsMulti)"
    echo "  ./generate_route_maps.sh all_c1            # Gera mapas para todas as C1 (C101-C109) - 10 execuções cada"
    echo "  ./generate_route_maps.sh all_c1_validation # Gera mapas para todas as C1 do results_validation_C1"
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

generate_validation_maps() {
    local instance_name=$1
    local instance_upper=$(echo "$instance_name" | tr '[:lower:]' '[:upper:]')
    local validation_dir="results_validation_C1/${instance_upper}"
    
    if [ ! -d "$validation_dir" ]; then
        echo "✗ Diretório $validation_dir não encontrado!"
        return
    fi
    
    echo ""
    echo "=========================================="
    echo "Gerando mapas para: $instance_upper"
    echo "=========================================="
    
    local maps_generated=0
    local maps_failed=0
    
    for exec_num in {01..10}; do
        local evo_file="$validation_dir/evo_${instance_name}_exec${exec_num}.txt"
        
        if [ -f "$evo_file" ]; then
            echo ""
            echo "Execução ${exec_num}/10..."
            
            python3 scripts/plot_route_maps.py \
                --instance "$instance_upper" \
                --results-file "$evo_file" \
                --instances-dir "$INSTANCES_DIR" \
                --output-dir "$validation_dir/route_maps_exec${exec_num}" \
                2>/dev/null
            
            if [ $? -eq 0 ]; then
                echo "  ✓ Mapas gerados: $validation_dir/route_maps_exec${exec_num}/"
                ((maps_generated++))
            else
                echo "  ✗ Erro ao gerar mapas"
                ((maps_failed++))
            fi
        else
            echo "  ✗ Arquivo não encontrado: $evo_file"
            ((maps_failed++))
        fi
    done
    
    echo ""
    echo "Resumo $instance_upper: ✓ $maps_generated gerados | ✗ $maps_failed falhas"
}

# Processar comando
case "$INSTANCE_PARAM" in
    all_c1)
        echo "=========================================="
        echo "Gerando mapas para TODAS as instâncias C1"
        echo "Validação: 10 execuções por instância"
        echo "=========================================="
        
        for i in {101..109}; do
            generate_validation_maps "c${i}"
        done
        
        echo ""
        echo "=========================================="
        echo "Geração de mapas concluída!"
        echo "Mapas salvos em: results_validation_C1/CXXX/route_maps_execYY/"
        echo "=========================================="
        ;;
    
    all_c1_validation)
        echo "=========================================="
        echo "Gerando mapas de VALIDAÇÃO C1"
        echo "10 execuções por instância"
        echo "=========================================="
        
        for i in {101..109}; do
            generate_validation_maps "c${i}"
        done
        
        echo ""
        echo "=========================================="
        echo "Geração de mapas concluída!"
        echo "Mapas salvos em: results_validation_C1/CXXX/route_maps_execYY/"
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
    
    c[0-9]*)
        # Instância C específica - verificar se é de validação
        instance_upper=$(echo "$INSTANCE_PARAM" | tr '[:lower:]' '[:upper:]')
        if [ -d "results_validation_C1/${instance_upper}" ]; then
            echo "Detectado diretório de validação. Gerando mapas para 10 execuções..."
            generate_validation_maps "$INSTANCE_PARAM"
        else
            generate_single_map "$INSTANCE_PARAM"
        fi
        ;;
    
    *)
        # Instância individual
        generate_single_map "$INSTANCE_PARAM"
        ;;
esac
