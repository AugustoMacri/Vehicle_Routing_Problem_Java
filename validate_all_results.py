#!/usr/bin/env python3
"""
Script para validar TODAS as soluções existentes
Verifica: cobertura de clientes + janelas de tempo
"""

import subprocess
import os
from pathlib import Path


def validate_solution(instance_file, solution_file):
    """Executa o validador rigoroso e retorna o resultado"""
    cmd = f"python3 scripts/validate_solution_rigorous.py {instance_file} {solution_file}"
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)

    output = result.stdout

    # Extrair informações
    is_valid = "SOLUÇÃO VÁLIDA" in output
    clients_ok = "Todos os clientes visitados" in output
    time_windows_ok = "Janelas de tempo respeitadas" in output
    capacity_ok = "Capacidade respeitada" in output

    missing_clients = []
    if "clientes NÃO visitados:" in output:
        # Extrair lista de clientes não visitados
        for line in output.split('\n'):
            if "clientes NÃO visitados:" in line:
                import re
                match = re.search(r'\[(.+)\]', line)
                if match:
                    missing_clients = [int(x.strip())
                                       for x in match.group(1).split(',')]

    time_violations = 0
    if "violações de janela de tempo" in output.lower():
        import re
        match = re.search(
            r'(\d+)\s+violações de janela de tempo', output, re.IGNORECASE)
        if match:
            time_violations = int(match.group(1))

    return {
        'valid': is_valid,
        'clients_ok': clients_ok,
        'time_windows_ok': time_windows_ok,
        'capacity_ok': capacity_ok,
        'missing_clients': missing_clients,
        'time_violations': time_violations
    }


def main():
    base_dir = Path(".")

    # Definir instâncias
    instance_groups = {
        'C1': {
            'instances': [f'C{i}' for i in range(101, 110)],
            'results_dir': 'results_validation_C1',
            'instance_dir': 'src/instances/solomon'
        },
        'R1': {
            'instances': [f'R{i}' for i in range(101, 110)],
            'results_dir': 'results_validation_R1',
            'instance_dir': 'src/instances/solomon'
        },
        'RC1': {
            'instances': [f'RC{i}' for i in range(101, 109)],
            'results_dir': 'results_validation_RC1',
            'instance_dir': 'src/instances/solomon'
        }
    }

    print("=" * 80)
    print("VALIDAÇÃO COMPLETA DE TODAS AS SOLUÇÕES")
    print("=" * 80)
    print()

    total_solutions = 0
    valid_solutions = 0
    invalid_solutions = 0
    missing_solutions = 0

    problems_found = []

    for group_name, group_info in instance_groups.items():
        print(f"\n{'=' * 80}")
        print(f"GRUPO: {group_name}")
        print(f"{'=' * 80}\n")

        for instance in group_info['instances']:
            instance_file = f"{group_info['instance_dir']}/{instance}.txt"
            results_path = Path(group_info['results_dir']) / instance

            if not results_path.exists():
                print(f"❌ {instance}: Diretório não existe")
                continue

            # Procurar todos os arquivos de execução
            exec_files = sorted(results_path.glob(
                f"evo_{instance.lower()}_exec*.txt"))

            if not exec_files:
                print(f"⚠️  {instance}: Nenhum arquivo de resultado encontrado")
                missing_solutions += 1
                continue

            print(f"\n{instance}: {len(exec_files)} execuções encontradas")
            print("-" * 60)

            instance_valid = 0
            instance_invalid = 0

            for exec_file in exec_files:
                exec_num = exec_file.stem.split('_')[-1].replace('exec', '')
                total_solutions += 1

                result = validate_solution(instance_file, str(exec_file))

                if result['valid']:
                    print(f"  ✅ Exec {exec_num}: VÁLIDA")
                    valid_solutions += 1
                    instance_valid += 1
                else:
                    status_parts = []

                    if not result['clients_ok']:
                        n_missing = len(result['missing_clients'])
                        status_parts.append(
                            f"{n_missing} clientes não visitados: {result['missing_clients'][:5]}...")
                        problems_found.append({
                            'instance': instance,
                            'exec': exec_num,
                            'problem': 'missing_clients',
                            'details': result['missing_clients']
                        })

                    if not result['time_windows_ok']:
                        status_parts.append(f"Violações de janela de tempo")
                        problems_found.append({
                            'instance': instance,
                            'exec': exec_num,
                            'problem': 'time_violations',
                            'details': result['time_violations']
                        })

                    if not result['capacity_ok']:
                        status_parts.append(f"Violação de capacidade")
                        problems_found.append({
                            'instance': instance,
                            'exec': exec_num,
                            'problem': 'capacity',
                            'details': None
                        })

                    print(
                        f"  ❌ Exec {exec_num}: INVÁLIDA - {'; '.join(status_parts)}")
                    invalid_solutions += 1
                    instance_invalid += 1

            # Resumo da instância
            if instance_invalid == 0:
                print(
                    f"\n  ✅ {instance}: TODAS as {instance_valid} execuções são válidas")
            else:
                print(
                    f"\n  ⚠️  {instance}: {instance_valid} válidas, {instance_invalid} inválidas")

    # Relatório final
    print("\n" + "=" * 80)
    print("RESUMO FINAL")
    print("=" * 80)
    print(f"\nTotal de soluções verificadas: {total_solutions}")
    print(
        f"  ✅ Soluções válidas: {valid_solutions} ({valid_solutions*100/total_solutions if total_solutions > 0 else 0:.1f}%)")
    print(
        f"  ❌ Soluções inválidas: {invalid_solutions} ({invalid_solutions*100/total_solutions if total_solutions > 0 else 0:.1f}%)")
    print(f"  ⚠️  Soluções ausentes: {missing_solutions}")

    if problems_found:
        print("\n" + "=" * 80)
        print("PROBLEMAS ENCONTRADOS (Resumo)")
        print("=" * 80)

        # Agrupar por tipo de problema
        by_problem_type = {}
        for p in problems_found:
            ptype = p['problem']
            if ptype not in by_problem_type:
                by_problem_type[ptype] = []
            by_problem_type[ptype].append(p)

        for ptype, problems in by_problem_type.items():
            print(f"\n{ptype.upper().replace('_', ' ')}:")
            instances_affected = set(p['instance'] for p in problems)
            print(f"  Instâncias afetadas: {sorted(instances_affected)}")
            print(f"  Total de execuções: {len(problems)}")

            if ptype == 'missing_clients':
                # Listar quais clientes estão faltando mais frequentemente
                all_missing = []
                for p in problems:
                    all_missing.extend(p['details'])
                from collections import Counter
                most_common = Counter(all_missing).most_common(10)
                print(
                    f"  Clientes mais frequentemente não visitados: {most_common}")
    else:
        print("\n🎉 NENHUM PROBLEMA ENCONTRADO! Todas as soluções são válidas!")


if __name__ == "__main__":
    main()
