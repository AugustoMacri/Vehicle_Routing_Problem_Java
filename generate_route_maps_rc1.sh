#!/bin/bash

# Script para gerar mapas de rotas das execuções RC1 (rotas iniciais e finais)
# Similar ao usado para C1

echo "=========================================="
echo "Gerador de Mapas de Rotas - RC1"
echo "=========================================="

RESULTS_DIR="results_validation_RC1"
INSTANCES_DIR="src/instances/solomon"

# Verificar se o diretório existe
if [ ! -d "$RESULTS_DIR" ]; then
    echo "Erro: Diretório não encontrado: $RESULTS_DIR"
    echo "Execute primeiro: python3 scripts/run_validation_rc1.py"
    exit 1
fi

echo ""
total_maps=0

# Para cada instância RC1
for instance in RC101 RC102 RC103 RC104 RC105 RC106 RC107 RC108; do
    instance_dir="$RESULTS_DIR/$instance"
    
    if [ ! -d "$instance_dir" ]; then
        echo "⚠ Diretório não encontrado: $instance"
        continue
    fi
    
    echo "Gerando mapas para $instance..."
    instance_maps=0
    
    # Para cada execução (01 a 10)
    for exec_num in $(seq -f "%02g" 1 10); do
        evo_file="$instance_dir/evo_${instance,,}_exec${exec_num}.txt"
        
        if [ -f "$evo_file" ]; then
            # Criar diretório para mapas
            map_dir="$instance_dir/route_maps"
            mkdir -p "$map_dir"
            
            # Gerar mapas (inicial e final)
            python3 scripts/plot_route_maps.py \
                --instance "$instance" \
                --results-file "$evo_file" \
                --instances-dir "$INSTANCES_DIR" \
                --output-dir "$map_dir" >/dev/null 2>&1
            
            if [ $? -eq 0 ]; then
                # Contar mapas gerados
                map_count=$(ls -1 "$map_dir"/*exec${exec_num}*.png 2>/dev/null | wc -l)
                if [ $map_count -gt 0 ]; then
                    instance_maps=$((instance_maps + map_count))
                    total_maps=$((total_maps + map_count))
                fi
            fi
        fi
    done
    
    if [ $instance_maps -gt 0 ]; then
        echo "  ✓ $instance: $instance_maps mapas gerados"
    else
        echo "  ⚠ $instance: nenhum mapa gerado"
    fi
done

echo ""
echo "=========================================="
echo "CONCLUÍDO!"
echo "Total de mapas gerados: $total_maps"
echo "Mapas salvos em: results_validation_RC1/[INSTANCE]/route_maps/"
echo "=========================================="
