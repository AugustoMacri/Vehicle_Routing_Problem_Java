#!/usr/bin/env python3
"""
Relatório de validação rápida de TODAS as soluções existentes
"""

import subprocess
import re
from pathlib import Path
from collections import defaultdict


def validate_file(instance_file, solution_file):
    """Valida um arquivo e retorna status"""
    result = subprocess.run(
        f"python3 scripts/validate_solution_rigorous.py {instance_file} {solution_file}",
        shell=True, capture_output=True, text=True, timeout=10
    )

    output = result.stdout
    return {
        'valid': "✅ SOLUÇÃO VÁLIDA" in output,
        'missing_clients': "clientes NÃO visitados" in output,
        'capacity_violation': "excede capacidade" in output,
        'time_violation': "Janelas de tempo violadas" in output or "violações de janela" in output.lower(),
        'output': output
    }


def main():
    groups = {
        'C1': ([f'C{i}' for i in range(101, 110)], 'results_validation_C1'),
        'R1': ([f'R{i}' for i in range(101, 110)], 'results_validation_R1'),
        'RC1': ([f'RC{i}' for i in range(101, 109)], 'results_validation_RC1')
    }

    print("=" * 80)
    print("RELATÓRIO DE VALIDAÇÃO COMPLETA")
    print("=" * 80)
    print()

    summary = defaultdict(lambda: {
        'total': 0, 'valid': 0, 'missing_clients': 0,
        'capacity': 0, 'time': 0, 'not_found': 0
    })

    for group_name, (instances, base_dir) in groups.items():
        print(f"\n{'='*80}")
        print(f" {group_name} - Validando {len(instances)} instâncias")
        print(f"{'='*80}\n")

        for inst in instances:
            inst_path = Path(base_dir) / inst

            if not inst_path.exists():
                print(f"{inst}: ⚠️  Diretório não existe")
                summary[group_name]['not_found'] += 1
                continue

            files = sorted(inst_path.glob(f"evo_{inst.lower()}_exec*.txt"))

            if not files:
                print(f"{inst}: ⚠️  Nenhum arquivo de resultado")
                summary[group_name]['not_found'] += 1
                continue

            # Validar primeira execução de cada instância
            first_file = files[0]
            inst_file = f"src/instances/solomon/{inst}.txt"

            try:
                result = validate_file(inst_file, str(first_file))
                summary[group_name]['total'] += 1

                if result['valid']:
                    print(f"{inst}: ✅ VÁLIDA ({len(files)} execuções)")
                    summary[group_name]['valid'] += 1
                else:
                    issues = []
                    if result['missing_clients']:
                        issues.append("clientes faltando")
                        summary[group_name]['missing_clients'] += 1
                    if result['capacity_violation']:
                        issues.append("violação capacidade")
                        summary[group_name]['capacity'] += 1
                    if result['time_violation']:
                        issues.append("violação janelas")
                        summary[group_name]['time'] += 1

                    print(
                        f"{inst}: ❌ INVÁLIDA - {', '.join(issues)} ({len(files)} execuções)")

            except Exception as e:
                print(f"{inst}: ⚠️  Erro ao validar: {e}")

    # Resumo final
    print(f"\n{'='*80}")
    print("RESUMO FINAL")
    print(f"{'='*80}\n")

    for group_name in ['C1', 'R1', 'RC1']:
        stats = summary[group_name]
        if stats['total'] == 0:
            continue

        print(f"{group_name}:")
        print(f"  Total verificadas: {stats['total']}")
        print(
            f"  ✅ Válidas: {stats['valid']} ({stats['valid']*100/stats['total']:.1f}%)")

        if stats['valid'] < stats['total']:
            print(f"  ❌ Inválidas: {stats['total'] - stats['valid']}")
            if stats['missing_clients'] > 0:
                print(
                    f"     - Clientes não visitados: {stats['missing_clients']}")
            if stats['capacity'] > 0:
                print(f"     - Violação de capacidade: {stats['capacity']}")
            if stats['time'] > 0:
                print(f"     - Violação de janelas: {stats['time']}")

        if stats['not_found'] > 0:
            print(f"  ⚠️  Não encontradas: {stats['not_found']}")
        print()

    # Totais gerais
    total_all = sum(summary[g]['total'] for g in ['C1', 'R1', 'RC1'])
    valid_all = sum(summary[g]['valid'] for g in ['C1', 'R1', 'RC1'])

    if total_all > 0:
        print(
            f"TOTAL GERAL: {valid_all}/{total_all} válidas ({valid_all*100/total_all:.1f}%)")

    if valid_all == total_all and total_all > 0:
        print("\n🎉 SUCESSO! Todas as soluções validadas são completamente válidas!")
    elif valid_all > 0:
        print(f"\n⚠️  {total_all - valid_all} instâncias ainda têm problemas")


if __name__ == "__main__":
    main()
