#!/usr/bin/env python3
"""
Script para validar todas as soluções das instâncias C1, R1 e RC1
Gera relatório completo com comparação aos best-known values
"""

import os
import subprocess
from pathlib import Path

# Best-known values (vehicles, distance) from Solomon benchmarks
BEST_KNOWN = {
    'C101': (10, 828.94),
    'C102': (10, 828.94),
    'C103': (10, 828.06),
    'C104': (10, 824.78),
    'C105': (10, 828.94),
    'C106': (10, 828.94),
    'C107': (10, 828.94),
    'C108': (10, 828.94),
    'C109': (10, 828.94),
    'R101': (19, 1650.80),
    'R102': (17, 1486.12),
    'R103': (13, 1292.68),
    'R104': (9, 1007.31),
    'R105': (14, 1377.11),
    'R106': (12, 1252.03),
    'R107': (10, 1104.66),
    'R108': (9, 960.88),
    'R109': (11, 1194.73),
    'RC101': (14, 1696.95),
    'RC102': (12, 1554.75),
    'RC103': (11, 1261.67),
    'RC104': (10, 1135.48),
    'RC105': (13, 1629.44),
    'RC106': (11, 1424.73),
    'RC107': (11, 1230.48),
    'RC108': (10, 1139.82),
}


def validate_instance(instance_name, instance_file, solution_dir):
    """Valida todas as execuções de uma instância"""
    results = []

    for i in range(1, 11):
        exec_file = solution_dir / \
            f"evo_{instance_name.lower()}_exec{i:02d}.txt"

        if not exec_file.exists():
            continue

        # Validar solução
        cmd = [
            'python3', 'scripts/validate_solution_rigorous.py',
            instance_file, str(exec_file)
        ]

        try:
            output = subprocess.run(
                cmd, capture_output=True, text=True, timeout=30)

            # Extrair informações
            is_valid = '✅ SOLUÇÃO VÁLIDA!' in output.stdout

            distance = None
            vehicles = None

            for line in output.stdout.split('\n'):
                if 'Distância total:' in line:
                    distance = float(line.split(':')[1].strip())
                elif 'Veículos usados:' in line:
                    vehicles = int(line.split(':')[1].strip())

            results.append({
                'exec': i,
                'valid': is_valid,
                'distance': distance,
                'vehicles': vehicles
            })

        except Exception as e:
            print(f"Erro ao validar {instance_name} exec{i:02d}: {e}")

    return results


def generate_report():
    """Gera relatório completo de validação"""

    base_dir = Path('.')

    print("="*100)
    print("RELATÓRIO DE VALIDAÇÃO COMPLETO - INSTÂNCIAS SOLOMON C1, R1 e RC1")
    print("="*100)
    print()

    all_valid = True
    summary = []

    # Processa cada classe de instâncias
    for class_name in ['C1', 'R1', 'RC1']:
        print(f"\n{'='*100}")
        print(f"CLASSE {class_name}")
        print(f"{'='*100}\n")

        result_dir = base_dir / f'results_validation_{class_name}'

        if not result_dir.exists():
            print(f"❌ Diretório {result_dir} não encontrado!")
            continue

        # Lista instâncias desta classe
        instances = sorted(
            [d.name for d in result_dir.iterdir() if d.is_dir()])

        for instance in instances:
            instance_file = base_dir / 'src' / \
                'instances' / 'solomon' / f'{instance}.txt'
            solution_dir = result_dir / instance

            if not instance_file.exists():
                print(
                    f"⚠️  Arquivo de instância {instance_file} não encontrado")
                continue

            print(f"\n{instance}:")
            print("-" * 80)

            # Validar todas as execuções
            results = validate_instance(
                instance, str(instance_file), solution_dir)

            if not results:
                print(f"❌ Nenhuma solução encontrada para {instance}")
                all_valid = False
                continue

            # Calcular estatísticas
            valid_count = sum(1 for r in results if r['valid'])
            distances = [r['distance']
                         for r in results if r['distance'] is not None]
            vehicles = [r['vehicles']
                        for r in results if r['vehicles'] is not None]

            if distances:
                best_dist = min(distances)
                worst_dist = max(distances)
                avg_dist = sum(distances) / len(distances)

                # Comparar com best-known
                bk_vehicles, bk_distance = BEST_KNOWN.get(
                    instance, (None, None))

                if bk_distance:
                    gap = ((best_dist - bk_distance) / bk_distance) * 100
                else:
                    gap = None

                print(f"  ✅ Válidas: {valid_count}/{len(results)}")
                print(
                    f"  📏 Distâncias: Melhor={best_dist:.2f}, Pior={worst_dist:.2f}, Média={avg_dist:.2f}")
                print(f"  🚚 Veículos: {vehicles[0]}")

                if bk_distance:
                    print(
                        f"  📊 Best-known: {bk_distance:.2f} ({bk_vehicles} veículos)")
                    print(f"  📈 Gap: {gap:.2f}%")

                summary.append({
                    'instance': instance,
                    'valid': valid_count == len(results),
                    'best': best_dist,
                    'avg': avg_dist,
                    'vehicles': vehicles[0],
                    'gap': gap
                })

                if valid_count < len(results):
                    all_valid = False
                    print(
                        f"  ⚠️  ATENÇÃO: {len(results) - valid_count} soluções inválidas!")
            else:
                print(f"  ❌ Não foi possível extrair distâncias")
                all_valid = False

    # Resumo final
    print(f"\n\n{'='*100}")
    print("RESUMO GERAL")
    print(f"{'='*100}\n")

    if all_valid:
        print("✅ TODAS AS SOLUÇÕES SÃO VÁLIDAS!")
    else:
        print("⚠️  EXISTEM SOLUÇÕES INVÁLIDAS - VEJA DETALHES ACIMA")

    print(f"\nTotal de instâncias validadas: {len(summary)}")

    # Estatísticas de gap
    gaps_c1 = [s['gap'] for s in summary if s['instance'].startswith(
        'C') and s['gap'] is not None]
    gaps_r1 = [s['gap'] for s in summary if s['instance'].startswith(
        'R') and s['gap'] is not None]
    gaps_rc1 = [s['gap'] for s in summary if s['instance'].startswith(
        'RC') and s['gap'] is not None]

    if gaps_c1:
        print(f"\nGap médio C1: {sum(gaps_c1)/len(gaps_c1):.2f}%")
    if gaps_r1:
        print(f"Gap médio R1: {sum(gaps_r1)/len(gaps_r1):.2f}%")
    if gaps_rc1:
        print(f"Gap médio RC1: {sum(gaps_rc1)/len(gaps_rc1):.2f}%")

    print("\n" + "="*100)


if __name__ == "__main__":
    generate_report()
